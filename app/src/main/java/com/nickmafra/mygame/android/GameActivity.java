package com.nickmafra.mygame.android;

import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.nickmafra.mygame.GameEngine;

public class GameActivity extends AppCompatActivity {

    private GameEngine gameEngine;
    private GLRendererView glRendererView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game);

        gameEngine = new GameEngine(this);
        glRendererView = new GLRendererView(this, gameEngine);

        ConstraintLayout layout = findViewById(R.id.game_layout);
        layout.addView(glRendererView);
        gameEngine.setGlRendererView(glRendererView);

        gameEngine.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameEngine.resume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        gameEngine.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gameEngine.stop();
    }
}
