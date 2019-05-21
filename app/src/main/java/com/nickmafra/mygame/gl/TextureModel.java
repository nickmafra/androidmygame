package com.nickmafra.mygame.gl;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLUtils;

import java.nio.Buffer;

import static android.opengl.GLES20.*;

public class TextureModel implements GLModel {

    private final String TAG = this.getClass().getSimpleName();

    private static final int stride = 20; // = 5 * Float.BYTES;

    private String modelAsset;
    private String textureAsset;

    private Buffer buffer;

    private int[] bufferIds = new int[1];
    private int[] texIds = new int[1];
    private TextureGLProgram program;

    private float[] mvpMatrix;

    public TextureModel() {
        program = new TextureGLProgram();
    }

    public TextureModel(String modelAsset, String textureAsset) {
        this();
        this.modelAsset = modelAsset;
        this.textureAsset = textureAsset;
    }

    public void setModelAsset(String modelAsset) {
        this.modelAsset = modelAsset;
    }

    public void setTextureAsset(String textureAsset) {
        this.textureAsset = textureAsset;
    }

    @Override
    public void load(Context context) {
        glGenBuffers(bufferIds.length, bufferIds, 0);
        loadModel(context);

        glGenTextures(texIds.length, texIds, 0);
        if (textureAsset != null) {
            loadTexture(context);
        }

        program.load(context);
    }

    private void loadModel(Context context) {
        if (modelAsset == null) {
            throw new RuntimeException("Model não definido.");
        }
        buffer = GLUtil.loadModelAsset(context, modelAsset);
        glBindBuffer(GL_ARRAY_BUFFER, bufferIds[0]);
        glBufferData(GL_ARRAY_BUFFER, buffer.capacity() * 4, buffer, GL_STATIC_DRAW);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        GLUtil.checkGlError(TAG + ".loadModel");
    }

    public void loadTexture(Context context) {
        Bitmap bitmap = GLUtil.loadTexture(context, textureAsset);

        glBindTexture(GL_TEXTURE_2D, texIds[0]);
        // Set filtering
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);

        glBindTexture(GL_TEXTURE_2D, 0);
        bitmap.recycle();
        GLUtil.checkGlError(TAG + ".loadTexture");
    }

    @Override
    public void setMvpMatrix(float[] mvpMatrix) {
        this.mvpMatrix = mvpMatrix;
    }

    @Override
    public void draw() {
        if (program.getId() == 0) {
            throw new RuntimeException("GLProgram não carregado.");
        }
        glUseProgram(program.getId());

        glEnableVertexAttribArray(program.getPosId());
        glEnableVertexAttribArray(program.getTexCoordId());

        glUniformMatrix4fv(program.getMvpMatrixId(), 1, false, mvpMatrix, 0);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texIds[0]);
        glUniform1i(program.getTexId(), 0);

        glBindBuffer(GL_ARRAY_BUFFER, bufferIds[0]);
        glVertexAttribPointer(program.getPosId(), 3, GL_FLOAT, false, stride, 0);
        glVertexAttribPointer(program.getTexCoordId(), 3, GL_FLOAT, false, stride, 12);

        glDrawArrays(GL_TRIANGLES, 0, buffer.capacity());

        glBindTexture(GL_TEXTURE_2D, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDisableVertexAttribArray(program.getPosId());
        glDisableVertexAttribArray(program.getTexCoordId());
        glUseProgram(0);

        GLUtil.checkGlError(TAG + ".draw");
    }
}
