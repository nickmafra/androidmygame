package com.nickmafra.gl;

import android.content.Context;

import java.nio.Buffer;

import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_ELEMENT_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_STATIC_DRAW;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_SHORT;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBufferData;
import static android.opengl.GLES20.glDisableVertexAttribArray;
import static android.opengl.GLES20.glDrawElements;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGenBuffers;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;

public class NaveModel implements GLModel {

    private final String TAG = this.getClass().getSimpleName();

            // position          // color
    private static final float verticesData[] = {
            -1.0f, -0.3f, +1.0f, 0.5f, 0.5f, 0.8f, // 0 left blue
            +1.0f, -0.3f, +1.0f, 0.5f, 0.5f, 0.8f, // 1 right blue
             0.0f, +1.3f, +1.0f, 0.5f, 0.5f, 0.8f, // 2 top blue
             0.0f, -0.3f, +0.7f, 0.5f, 0.5f, 0.5f, // 3 bottom grey
             0.0f,  0.0f, -1.0f, 1.0f, 1.0f, 1.0f, // 4 front white
            -0.3f, +0.4f, +1.0f, 0.5f, 0.5f, 0.8f, // 5 left2 blue
            +0.3f, +0.4f, +1.0f, 0.5f, 0.5f, 0.8f, // 6 right2 blue
    };
    private static final int stride = 24; // = 6 * Float.BYTES;

    private static final short elementsData[] = {
            0, 3, 5,    // left back
            3, 1, 6,    // right back
            5, 3, 6,    // center back
            2, 5, 6,    // top back
            4, 0, 5,    // left front
            4, 5, 2,    // left2 front
            4, 2, 6,    // right2 front
            4, 6, 1,    // right front
            4, 1, 3,    // bottom left
            4, 3, 0,    // bottom right
    };

    private Buffer vertexBuffer;
    private Buffer elementBuffer;

    private int[] bufferIds = new int[2];
    private SimpleGLProgram program;

    private float[] mvpMatrix;

    public NaveModel() {
        vertexBuffer = GLUtil.makeFloatBuffer(verticesData);
        elementBuffer = GLUtil.makeShortBuffer(elementsData);
        program = new SimpleGLProgram();
    }

    @Override
    public void load(Context context) {
        // gera e carrega buffers
        glGenBuffers(bufferIds.length, bufferIds, 0);
        glBindBuffer(GL_ARRAY_BUFFER, bufferIds[0]);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer.capacity() * 4, vertexBuffer, GL_STATIC_DRAW);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferIds[1]);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementBuffer.capacity() * 2, elementBuffer, GL_STATIC_DRAW);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        program.load(context);
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
        glEnableVertexAttribArray(program.getColorId());

        glUniformMatrix4fv(program.getMvpMatrixId(), 1, false, mvpMatrix, 0);

        glBindBuffer(GL_ARRAY_BUFFER, bufferIds[0]);
        glVertexAttribPointer(program.getPosId(), 3, GL_FLOAT, false, stride, 0);
        glVertexAttribPointer(program.getColorId(), 3, GL_FLOAT, false, stride, 12);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferIds[1]);
        glDrawElements(GL_TRIANGLES, elementsData.length, GL_UNSIGNED_SHORT, 0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glDisableVertexAttribArray(program.getPosId());
        glDisableVertexAttribArray(program.getColorId());
        glUseProgram(0);

        GLUtil.checkGlError(TAG + ".draw");
    }
}
