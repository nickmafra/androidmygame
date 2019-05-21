package com.nickmafra.mygame.android;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.nickmafra.mygame.GameContext1;
import com.nickmafra.mygame.GameEngine;
import java.util.LinkedHashMap;
import java.util.Map;

public class GameListener implements View.OnTouchListener {

    private final String TAG = this.getClass().getSimpleName();

    private GameContext1 gc;

    private static class PointerVars {
        int id;
        int lastIndex;
        View view;
        float previousX;
        float previousY;
        int key; // 1 = girar, 2 = acelerar
    }

    private Map<Integer, PointerVars> pVars;

    public GameListener(GameEngine gameEngine) {
        this.gc = (GameContext1) gameEngine.getGameContext();
        pVars = new LinkedHashMap<>();
    }

    private PointerVars getVars(View view, MotionEvent ev, int i) {
        int id = ev.getPointerId(i);
        PointerVars vars = pVars.get(id);
        if (vars == null) {
            vars = new PointerVars();
            vars.id = id;
            pVars.put(id, vars);
            vars.view = view;
        }
        vars.lastIndex = i;
        return vars;
    }

    private float normX(View view, float x) {
        return 2 * (x - view.getX()) / view.getWidth() - 1f;
    }

    private float normY(View view, float y) {
        return 2 * (y - view.getY()) / view.getHeight() - 1f;
    }

    private int touchRegion(MotionEvent ev, PointerVars vars) {
        float x = normX(vars.view, ev.getX(vars.lastIndex));
        float d = 0.6f;

        return x < -d ? -1 : x > d ? 1 : 0;
    }

    private final float TOUCH_SCALE_FACTOR = 0.1f;

    @Override
    public boolean onTouch(View v, MotionEvent ev) {
        int action = ev.getActionMasked();
        if (action == MotionEvent.ACTION_MOVE) {
            for (int i = 0; i < ev.getPointerCount(); i++) {
                doKey(v, ev, i);
            }
        } else {
            int i = ev.getActionIndex();
            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
                setKey(v, ev);
                doKey(v, ev, i);
            } else if (action == MotionEvent.ACTION_UP
                    || action == MotionEvent.ACTION_POINTER_UP
                    || action == MotionEvent.ACTION_CANCEL) {
                doKey(v, ev, i);
                pVars.remove(ev.getPointerId(i));
            }
        }
        return true;
    }

    private void setKey(View v, MotionEvent ev) {
        PointerVars vars = getVars(v, ev, ev.getActionIndex());
        int tr = touchRegion(ev, vars);
        if (tr == 0) {
            vars.key = 1;
        } else {
            vars.key = 2;
        }
    }

    private void doKey(View v, MotionEvent ev, int i) {
        PointerVars vars = getVars(v, ev, i);
        switch (vars.key) {
            case 1:
                girar(ev, vars);
                break;
            case 2:
                acelerar(ev, vars);
                break;
        }
        vars.previousX = ev.getX(i);
        vars.previousY = ev.getY(i);
    }

    private void girar(MotionEvent ev, PointerVars vars) {
        float x = ev.getX(vars.lastIndex);
        float y = ev.getY(vars.lastIndex);
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_MOVE:
                float dx = (x - vars.previousX);
                float dy = (y - vars.previousY);
                float a = (float) Math.sqrt(dx*dx + dy*dy) * TOUCH_SCALE_FACTOR;

                gc.setPlayerRotation(dx, -dy, a);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                gc.unlockRotation();
                break;
        }
    }

    private void acelerar(MotionEvent ev, PointerVars vars) {
        float y = normY(vars.view, ev.getY(vars.lastIndex));
        int tr = touchRegion(ev, vars);
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                if (tr == -1) {
                    gc.setIsAccelerating1(true);
                } else {
                    gc.setIsAccelerating2(true);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (tr == -1) {
                    gc.setYProp1(y);
                } else {
                    gc.setYProp2(y);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                if (tr == -1) {
                    gc.setIsAccelerating1(false);
                } else {
                    gc.setIsAccelerating2(false);
                }
                break;
        }
    }
}
