package com.nickmafra.mygame;

import android.view.MotionEvent;
import android.view.View;

public class GameListener implements View.OnTouchListener {

    private GameEngine gameEngine;

    public GameListener(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

    private boolean isTouchingCenter(View view, MotionEvent e) {
        float x = (e.getX() / view.getWidth()) - 0.5f;
        float y = (e.getY() / view.getHeight()) - 0.5f;
        float d = 0.12f;

        float r2 = x * x + y * y;
        return r2 < d * d;
    }

    private volatile int acao; // 1 = girar, 2 = acelerar

    @Override
    public boolean onTouch(View v, MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            if (isTouchingCenter(v, e)) {
                acao = 2;
            } else {
                acao = 1;
            }
        }
        switch (acao) {
            case 1:
                girar(e);
                break;
            case 2:
                acelerar(e);
                break;
        }
        return true;
    }

    private final float TOUCH_SCALE_FACTOR = 0.1f;
    private float previousX;
    private float previousY;
    private float dx;
    private float dy;
    private float a;

    private void girar(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();
        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:
                dx = (x - previousX);
                dy = (y - previousY);
                a = (float) Math.sqrt(dx*dx + dy*dy) * TOUCH_SCALE_FACTOR;

                gameEngine.setPlayerRotation(dx, -dy, a);
                break;
            case MotionEvent.ACTION_UP:
                gameEngine.unlockRotation();
                break;
        }
        previousX = x;
        previousY = y;
    }

    private void acelerar(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                gameEngine.setIsAccelerating(true);
                break;
            case MotionEvent.ACTION_UP:
                gameEngine.setIsAccelerating(false);
                break;
        }
    }
}
