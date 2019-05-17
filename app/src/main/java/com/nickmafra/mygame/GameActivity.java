package com.nickmafra.mygame;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class GameActivity extends AppCompatActivity {

    private GameEngine gameEngine;
    private GameView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        gameEngine = new GameEngine(this);
        view = new GameView(this, gameEngine);

        gameEngine.setGlView(view);
        setContentView(view);

        gameEngine.start();
    }
}
