package com.nickmafra.mygame.gl;

import android.content.Context;

import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;

public class TextureGLProgram implements GLProgram {

    private final String TAG = this.getClass().getSimpleName();

    private int id;
    private int mvpMatrixId, texId, posId, texCoordId;

    public TextureGLProgram() {
    }

    @Override
    public void reset() {
        id = 0;
        mvpMatrixId = posId = texId = 0;
    }

    @Override
    public void load(Context context) {
        reset();

        // carrega e compila shaders
        String vsCode = GLUtil.loadShaderCode(context, "shaders/textureVS.glsl");
        String fsCode = GLUtil.loadShaderCode(context, "shaders/textureFS.glsl");
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
        texId = glGetUniformLocation(id, "tex");
        posId = glGetAttribLocation(id, "aPos");
        texCoordId = glGetAttribLocation(id, "aTexCoord");
        GLUtil.checkGlError(TAG + ".load");
    }

    @Override
    public int getId() {
        return id;
    }

    public int getMvpMatrixId() {
        return mvpMatrixId;
    }

    public int getTexId() {
        return texId;
    }

    public int getPosId() {
        return posId;
    }

    public int getTexCoordId() {
        return texCoordId;
    }
}
