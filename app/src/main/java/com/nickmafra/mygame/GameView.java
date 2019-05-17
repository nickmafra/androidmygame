package com.nickmafra.mygame;

import android.content.Context;
import android.opengl.GLSurfaceView;

public class GameView extends GLSurfaceView {

    public GameView(Context context, GameEngine gameEngine) {
        super(context);
        setOnTouchListener(new GameListener(gameEngine));

        setEGLContextClientVersion(3);
        setRenderer(gameEngine.getRenderer());
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }
}
