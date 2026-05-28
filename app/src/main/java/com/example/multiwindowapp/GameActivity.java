package com.example.multiwindowapp;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameActivity extends AppCompatActivity {

    private final Button[] cells = new Button[9];
    private final char[] board = new char[9];

    private TextView tvStatus;
    private boolean gameOver = false;
    private boolean playerTurn = true;

    // ====
    private int selectedIndex = 4;

    private float scrollAccumX = 0f;
    private float scrollAccumY = 0f;
    private static final float SCROLL_STEP = 180f;

    private String playerName;
    private String difficulty;
    private boolean soundEnabled;
    private boolean playerStarts;

    private final Random random = new Random();
    private ToneGenerator toneGenerator;

    // ====
    private static final float SCALE_SELECTED = 1.15f;
    private static final float SCALE_NORMAL   = 1.0f;
    private static final float ELEVATION_SELECTED = 18f;

    private static final int COLOR_NORMAL   = Color.parseColor("#FFFFFF");
    private static final int COLOR_SELECTED = Color.parseColor("#8FCDEF");
    private static final int COLOR_HINT     = Color.parseColor("#FFD86B");

    private static final int[][] WIN_LINES = {
            {0, 1, 2}, {3, 4, 5}, {6, 7, 8},
            {0, 3, 6}, {1, 4, 7}, {2, 5, 8},
            {0, 4, 8}, {2, 4, 6}
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        tvStatus = findViewById(R.id.tvStatus);

        int[] ids = {R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8};
        for (int i = 0; i < 9; i++) {
            cells[i] = findViewById(ids[i]);
            cells[i].setClickable(false);
            cells[i].setFocusable(false);
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        playerName = prefs.getString("player_name", "Игрок");
        difficulty = prefs.getString("difficulty", "medium");
        soundEnabled = prefs.getBoolean("sound_enabled", true);
        playerStarts = prefs.getBoolean("player_starts", true);

        if (soundEnabled) {
            try {
                toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 80);
            } catch (RuntimeException e) {
                toneGenerator = null;
            }
        }

        GridLayout grid = findViewById(R.id.grid);
        grid.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeRight() { moveCursor(0, 1); }

            @Override
            public void onSwipeLeft() { moveCursor(0, -1); }

            @Override
            public void onSwipeTop() { moveCursor(-1, 0); }

            @Override
            public void onSwipeBottom() { moveCursor(1, 0); }

            @Override
            public void onSingleTap() {
                handlePlace(selectedIndex);
            }

            @Override
            public void onDoubleTapGesture() {
                startNewGame();
            }

            @Override
            public void onLongPressGesture() {
                showHint();
            }

            @Override
            public void onScrollGesture(float distanceX, float distanceY) {
                handleScroll(distanceX, distanceY);
            }

            @Override
            public void onTouchStart() {
                scrollAccumX = 0f;
                scrollAccumY = 0f;
            }
        });

        Button btnRestart = findViewById(R.id.btnRestart);
        Button btnBack = findViewById(R.id.btnBack);
        btnRestart.setOnClickListener(v -> startNewGame());
        btnBack.setOnClickListener(v -> finish());

        startNewGame();
    }

    private void startNewGame() {
        gameOver = false;
        for (int i = 0; i < 9; i++) {
            board[i] = ' ';
            cells[i].setText("");
            cells[i].setScaleX(SCALE_NORMAL);
            cells[i].setScaleY(SCALE_NORMAL);
            cells[i].setElevation(0f);
        }
        selectedIndex = 4;
        playerTurn = playerStarts;
        refreshHighlight();

        if (playerTurn) {
            tvStatus.setText("Ход: " + playerName + " (X)");
        } else {
            tvStatus.setText("Ход компьютера (O)");
            new Handler(Looper.getMainLooper()).postDelayed(this::computerMove, 400);
        }
    }

    // ====
    private void moveCursor(int dRow, int dCol) {
        int row = clamp(selectedIndex / 3 + dRow);
        int col = clamp(selectedIndex % 3 + dCol);
        selectedIndex = row * 3 + col;
        refreshHighlight();
    }

    private int clamp(int v) {
        return Math.max(0, Math.min(2, v));
    }

    private void handleScroll(float distanceX, float distanceY) {
        scrollAccumX -= distanceX;
        scrollAccumY -= distanceY;

        if (Math.abs(scrollAccumX) >= Math.abs(scrollAccumY)) {
            if (scrollAccumX >= SCROLL_STEP) {
                moveCursor(0, 1);
                scrollAccumX -= SCROLL_STEP;
                scrollAccumY = 0f;
            } else if (scrollAccumX <= -SCROLL_STEP) {
                moveCursor(0, -1);
                scrollAccumX += SCROLL_STEP;
                scrollAccumY = 0f;
            }
        } else {
            if (scrollAccumY >= SCROLL_STEP) {
                moveCursor(1, 0);
                scrollAccumY -= SCROLL_STEP;
                scrollAccumX = 0f;
            } else if (scrollAccumY <= -SCROLL_STEP) {
                moveCursor(-1, 0);
                scrollAccumY += SCROLL_STEP;
                scrollAccumX = 0f;
            }
        }
    }

    private void handlePlace(int index) {
        if (gameOver || !playerTurn || index < 0 || board[index] != ' ') {
            return;
        }
        selectedIndex = index;
        placeMark(index, 'X');
        refreshHighlight();
        popCell(index);
        if (checkEnd()) {
            return;
        }
        playerTurn = false;
        tvStatus.setText("Ход компьютера (O)");
        new Handler(Looper.getMainLooper()).postDelayed(this::computerMove, 500);
    }

    private void showHint() {
        if (gameOver || !playerTurn) {
            return;
        }
        int idx = hintMove();
        if (idx >= 0) {
            selectedIndex = idx;
            refreshHighlight();
            flashHint(idx);
            Toast.makeText(this, "Подсказка: рекомендуемый ход выделен", Toast.LENGTH_SHORT).show();
        }
    }

    private int hintMove() {
        for (int i : freeCells(board)) {
            board[i] = 'X';
            boolean win = getWinner(board) == 'X';
            board[i] = ' ';
            if (win) return i;
        }
        for (int i : freeCells(board)) {
            board[i] = 'O';
            boolean win = getWinner(board) == 'O';
            board[i] = ' ';
            if (win) return i;
        }
        if (board[4] == ' ') return 4;
        for (int c : new int[]{0, 2, 6, 8}) {
            if (board[c] == ' ') return c;
        }
        // 5) Любая свободная
        List<Integer> free = freeCells(board);
        return free.isEmpty() ? -1 : free.get(0);
    }

    // ===== Визуальная обратная связь =====

    private void refreshHighlight() {
        for (int i = 0; i < 9; i++) {
            boolean sel = (i == selectedIndex);
            cells[i].setBackgroundColor(sel ? COLOR_SELECTED : COLOR_NORMAL);
            cells[i].setElevation(sel ? ELEVATION_SELECTED : 0f);
            cells[i].animate()
                    .scaleX(sel ? SCALE_SELECTED : SCALE_NORMAL)
                    .scaleY(sel ? SCALE_SELECTED : SCALE_NORMAL)
                    .setDuration(140)
                    .start();
        }
    }

    private void popCell(int index) {
        float rest = (index == selectedIndex) ? SCALE_SELECTED : SCALE_NORMAL;
        cells[index].animate().scaleX(1.3f).scaleY(1.3f).setDuration(100)
                .withEndAction(() -> cells[index].animate()
                        .scaleX(rest).scaleY(rest).setDuration(100).start())
                .start();
    }

    private void flashHint(int index) {
        cells[index].setBackgroundColor(COLOR_HINT);
        popCell(index);
        new Handler(Looper.getMainLooper()).postDelayed(this::refreshHighlight, 700);
    }

    // ===== Логика игры =====

    private void computerMove() {
        if (gameOver) {
            return;
        }
        int move;
        switch (difficulty) {
            case "easy":
                move = randomMove();
                break;
            case "hard":
                move = bestMove();
                break;
            default:
                move = mediumMove();
                break;
        }
        if (move != -1) {
            placeMark(move, 'O');
            refreshHighlight();
            popCell(move);
        }
        if (checkEnd()) {
            return;
        }
        playerTurn = true;
        tvStatus.setText("Ход: " + playerName + " (X)");
    }

    private void placeMark(int index, char mark) {
        board[index] = mark;
        cells[index].setText(String.valueOf(mark));
        playSound();
    }

    private void playSound() {
        if (soundEnabled && toneGenerator != null) {
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 120);
        }
    }

    private boolean checkEnd() {
        char winner = getWinner(board);
        if (winner != ' ') {
            gameOver = true;
            if (winner == 'X') {
                tvStatus.setText("Победил " + playerName + "!");
                Toast.makeText(this, "Вы выиграли!", Toast.LENGTH_SHORT).show();
            } else {
                tvStatus.setText("Победил компьютер");
                Toast.makeText(this, "Компьютер выиграл", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        if (isFull(board)) {
            gameOver = true;
            tvStatus.setText("Ничья!");
            Toast.makeText(this, "Ничья", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    private int randomMove() {
        List<Integer> free = freeCells(board);
        if (free.isEmpty()) return -1;
        return free.get(random.nextInt(free.size()));
    }

    private int mediumMove() {
        for (int i : freeCells(board)) {
            board[i] = 'O';
            boolean win = getWinner(board) == 'O';
            board[i] = ' ';
            if (win) return i;
        }
        for (int i : freeCells(board)) {
            board[i] = 'X';
            boolean playerWin = getWinner(board) == 'X';
            board[i] = ' ';
            if (playerWin) return i;
        }
        if (board[4] == ' ') return 4;
        return randomMove();
    }

    private int bestMove() {
        int bestScore = Integer.MIN_VALUE;
        int move = -1;
        for (int i : freeCells(board)) {
            board[i] = 'O';
            int score = minimax(board, 0, false);
            board[i] = ' ';
            if (score > bestScore) {
                bestScore = score;
                move = i;
            }
        }
        return move;
    }

    private int minimax(char[] b, int depth, boolean isMaximizing) {
        char winner = getWinner(b);
        if (winner == 'O') return 10 - depth;
        if (winner == 'X') return depth - 10;
        if (isFull(b)) return 0;

        if (isMaximizing) {
            int best = Integer.MIN_VALUE;
            for (int i : freeCells(b)) {
                b[i] = 'O';
                best = Math.max(best, minimax(b, depth + 1, false));
                b[i] = ' ';
            }
            return best;
        } else {
            int best = Integer.MAX_VALUE;
            for (int i : freeCells(b)) {
                b[i] = 'X';
                best = Math.min(best, minimax(b, depth + 1, true));
                b[i] = ' ';
            }
            return best;
        }
    }

    private List<Integer> freeCells(char[] b) {
        List<Integer> free = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            if (b[i] == ' ') free.add(i);
        }
        return free;
    }

    private boolean isFull(char[] b) {
        for (char c : b) {
            if (c == ' ') return false;
        }
        return true;
    }

    private char getWinner(char[] b) {
        for (int[] line : WIN_LINES) {
            if (b[line[0]] != ' '
                    && b[line[0]] == b[line[1]]
                    && b[line[1]] == b[line[2]]) {
                return b[line[0]];
            }
        }
        return ' ';
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (toneGenerator != null) {
            toneGenerator.release();
        }
    }
}