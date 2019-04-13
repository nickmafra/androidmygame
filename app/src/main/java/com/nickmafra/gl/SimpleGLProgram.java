package com.nickmafra.gl;

import android.content.Context;

import static android.opengl.GLES20.*;

public class SimpleGLProgram implements GLProgram {

    private static final String TAG = "SimpleGLProgram";

    private int id;
    private int mvpMatrixId, posId, colorId;

    public SimpleGLProgram() {
    }

    @Override
    public void reset() {
        id = 0;
        mvpMatrixId = posId = colorId = 0;
    }

    @Override
    public void load(Context context) {
        reset();
        int vsId = 0, fsId = 0;

        // compila shaders
        String vsCode = GLUtil.loadAsset(context, "shaders/simpleVS.glsl");
        if (vsCode != null) {
            vsId = GLUtil.compileShader(GL_VERTEX_SHADER, vsCode);
        }
        String fsCode = GLUtil.loadAsset(context, "shaders/simpleFS.glsl");
        if (fsCode != null) {
            fsId = GLUtil.compileShader(GL_FRAGMENT_SHADER, fsCode);
        }

        // atrela programa
        if (vsId != 0 && fsId != 0) {
            id = GLUtil.linkProgram(vsId, fsId);
        }
        if (id == 0) {
            throw new RuntimeException("Erro ao compilar " + TAG);
        }

        mvpMatrixId = glGetUniformLocation(id, "uMVPMatrix");
        posId = glGetAttribLocation(id, "aPos");
        colorId = glGetAttribLocation(id, "aColor");
    }

    @Override
    public int getId() {
        return id;
    }

    public int getMvpMatrixId() {
        return mvpMatrixId;
    }

    public int getPosId() {
        return posId;
    }

    public int getColorId() {
        return colorId;
    }
}
