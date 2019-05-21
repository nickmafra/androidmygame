package com.nickmafra.mygame.gl;

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

        // carrega e compila shaders
        String vsCode = GLUtil.loadShaderCode(context, "simpleVS.glsl");
        String fsCode = GLUtil.loadShaderCode(context, "simpleFS.glsl");
        if (vsCode == null || fsCode == null) {
            throw new RuntimeException("Erro ao carregar shaders.");
        }
        int vsId = GLUtil.compileShader(GL_VERTEX_SHADER, vsCode);
        int fsId = GLUtil.compileShader(GL_FRAGMENT_SHADER, fsCode);
        if (vsId == 0 || fsId == 0) {
            throw new RuntimeException("Erro ao compilar shaders.");
        }

        // atrela programa
        id = GLUtil.linkProgram(vsId, fsId);
        if (id == 0) {
            throw new RuntimeException("Erro ao carregar " + TAG);
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
