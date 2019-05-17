package com.nickmafra.mygame;

import android.view.MotionEvent;
import android.view.View;

public class GameListener implements View.OnTouchListener {

    private GameEngine gameEngine;

    public GameListener(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

    private final float TOUCH_SCALE_FACTOR = 0.3f;
    private float previousX;
    private float previousY;
    private float dx;
    private float dy;
    private float a;

    @Override
    public boolean onTouch(View view, MotionEvent e) {
        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:
                dx = (x - previousX);
                dy = (y - previousY);
                a = (float) Math.sqrt(dx*dx + dy*dy) * TOUCH_SCALE_FACTOR;

                gameEngine.setRotationVector(dx, -dy, a);
                break;
            case MotionEvent.ACTION_UP:
                gameEngine.unlock();
        }

        previousX = x;
        previousY = y;
        return true;
    }
}
