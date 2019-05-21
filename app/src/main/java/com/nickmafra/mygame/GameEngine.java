package com.nickmafra.mygame;

import android.content.Context;
import android.opengl.Matrix;

import com.nickmafra.mygame.gl.Camera;
import com.nickmafra.mygame.gl.Object3D;
import com.nickmafra.mygame.android.GLRendererView;
import com.nickmafra.mygame.gl.PiramideModel;
import com.nickmafra.util.Vetor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameEngine {

    private long tickPeriod = 15; // milissegundos entre cada tick
    private long framePeriod = 20; // milissegundos entre cada frame

    private volatile Thread thread;

    private GLRendererView glRendererView;
    private GameContext gc;

    public GameEngine(Context context) {
        gc = new GameContext1(this);
    }

    public GLRendererView getGlRendererView() {
        return glRendererView;
    }

    public void setGlRendererView(GLRendererView glRendererView) {
        this.glRendererView = glRendererView;
    }

    public GameContext getGameContext() {
        return gc;
    }

    // time

    private int ticks;
    private long startTime;
    private long timeNow;
    private long lastTime;
    private long lastFrameTime;
    private long lastTickTime;

    public int getTicks() {
        return ticks;
    }

    private long clockTime() {
        return System.currentTimeMillis();
    }

    public void calcTimeStart() {
        ticks = 0;
        startTime = clockTime();
        timeNow = 0;
        lastFrameTime = -framePeriod; // força primeiro frame
        lastTickTime = -tickPeriod; // força primeiro tick
    }

    public void calcTimeNow() {
        lastTime = timeNow;
        timeNow = clockTime() - startTime;
    }

    public synchronized void start() {
        if (thread != null) {
            throw new RuntimeException("Thread is running.");
        }
        if (glRendererView == null) {
            throw new RuntimeException("GLRendererView is not defined.");
        }

        glRendererView.getModels().clear();
        glRendererView.getObjetos().clear();
        gc.start();
        glRendererView.requestLoadModels();

        thread = new GameThread();
        thread.start();
    }

    public synchronized void stop() {
        if (thread == null) {
            return;
        }
        thread.interrupt();
    }

    private volatile boolean pause;

    public synchronized void pause() {
        if (thread == null) {
            return;
        }
        pause = true;
    }

    public synchronized void resume() {
        if (thread == null || !pause) {
            return;
        }
        pause = false;
    }

    private class GameThread extends Thread {

        public GameThread() {
            super("GameThread");
        }

        @Override
        public void run() {
            calcTimeStart();
            while (!isInterrupted()) {
                try {
                    if (pause) {
                        Thread.sleep(50);
                        continue;
                    }
                } catch (InterruptedException e) {
                    break;
                }

                calcTimeNow();
                if (timeNow - lastTickTime > tickPeriod) {
                    lastTickTime = timeNow;
                    ticks++;
                    for (Object3D obj : glRendererView.getObjetos()) {
                        obj.setTimeNow(ticks);
                    }
                    gc.run();
                    continue;
                }
                if (timeNow - lastFrameTime > framePeriod) {
                    lastFrameTime = timeNow;
                    glRendererView.requestRender();
                }

                long rTick = tickPeriod - (timeNow - lastTickTime);
                long rRender = framePeriod - (timeNow - lastFrameTime);
                long rTime = Math.min(rTick, rRender);
                if (rTime > 0) {
                    try {
                        Thread.sleep(rTime);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
            thread = null;
        }
    }
}
