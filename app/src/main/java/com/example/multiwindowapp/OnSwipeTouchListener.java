package com.example.multiwindowapp;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
public class OnSwipeTouchListener implements View.OnTouchListener {
    private final GestureDetector gestureDetector;
    public OnSwipeTouchListener(Context context) {
        gestureDetector = new GestureDetector(context, new GestureListener());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 120;
        private static final int SWIPE_VELOCITY_THRESHOLD = 120;

        @Override
        public boolean onDown(MotionEvent e) {
            onTouchStart();
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            onSingleTap();
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            onDoubleTapGesture();
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            onLongPressGesture();
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            onScrollGesture(distanceX, distanceY);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                if (e1 == null || e2 == null) {
                    return false;
                }
                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();

                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD
                            && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight();
                        } else {
                            onSwipeLeft();
                        }
                        result = true;
                    }
                } else {
                    if (Math.abs(diffY) > SWIPE_THRESHOLD
                            && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            onSwipeBottom();
                        } else {
                            onSwipeTop();
                        }
                        result = true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }
    }
    public void onSwipeRight() { }
    public void onSwipeLeft() { }
    public void onSwipeTop() { }
    public void onSwipeBottom() { }

    public void onSingleTap() { }
    public void onDoubleTapGesture() { }
    public void onLongPressGesture() { }
    public void onScrollGesture(float distanceX, float distanceY) { }
    public void onTouchStart() { }
}