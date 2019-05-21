package com.nickmafra.mygame.gl;

import android.content.Context;

public interface GLModel {

    void load(Context context);

    void setMvpMatrix(float[] mvpMatrix);

    void draw();
}
