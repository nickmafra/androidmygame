package com.nickmafra.mygame;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.view.View;

import com.nickmafra.gl.MainGLRenderer;

public class MainGLView extends GLSurfaceView implements View.OnTouchListener {

    private MainGLRenderer renderer;

    public MainGLView(Context context) {
        super(context);

        setEGLContextClientVersion(3);

        setRenderer(renderer = new MainGLRenderer(context));
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        requestRender();
    }

    private final float TOUCH_SCALE_FACTOR = 9.0f / 16;
    private float previousX;
    private float previousY;

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:

                float dx = (x - previousX) * TOUCH_SCALE_FACTOR;
                float dy = (y - previousY) * TOUCH_SCALE_FACTOR;

                renderer.setRotationVector(dx, dy);
                requestRender();
        }

        previousX = x;
        previousY = y;
        return true;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }
}
