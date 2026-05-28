package com.example.multiwindowapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

public class MainActivity extends AppCompatActivity {

    private TextView tvPrefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvPrefs = findViewById(R.id.tvPrefs);
        Button btnGame = findViewById(R.id.btnGame);
        Button btnSettings = findViewById(R.id.btnSettings);
        Button btnExit = findViewById(R.id.btnExit);

        btnGame.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            startActivity(intent);
        });

        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        btnExit.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        showCurrentSettings();
    }

    private void showCurrentSettings() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        String playerName = prefs.getString("player_name", "Игрок");
        String difficulty = prefs.getString("difficulty", "medium");
        boolean soundEnabled = prefs.getBoolean("sound_enabled", true);
        boolean playerStarts = prefs.getBoolean("player_starts", true);

        String difficultyText;
        switch (difficulty) {
            case "easy":
                difficultyText = "Лёгкая";
                break;
            case "hard":
                difficultyText = "Сложная";
                break;
            default:
                difficultyText = "Средняя";
                break;
        }

        String summary = "Текущие настройки:\n" +
                "• Игрок: " + playerName + "\n" +
                "• Сложность: " + difficultyText + "\n" +
                "• Звук: " + (soundEnabled ? "включён" : "выключен") + "\n" +
                "• Первым ходит: " + (playerStarts ? playerName : "Компьютер");

        tvPrefs.setText(summary);
    }
}