package com.nickmafra.mygame.gl;

import android.content.Context;

public interface GLProgram {

    void reset();

    void load(Context context);

    int getId();

}
