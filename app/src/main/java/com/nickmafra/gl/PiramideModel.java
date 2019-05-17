package com.nickmafra.gl;

import android.content.Context;

import java.nio.Buffer;

import static android.opengl.GLES20.*;

public class PiramideModel implements GLModel {

            // position          // color
    private static final float verticesData[] = {
            -1.0f, +1.0f, -1.0f, 0.0f, 0.0f, 1.0f, // 0 left blue
            +1.0f, -1.0f, -1.0f, 1.0f, 0.0f, 0.0f, // 1 right red
            +1.0f, +1.0f, +1.0f, 0.0f, 1.0f, 0.0f, // 2 top green
            -1.0f, -1.0f, +1.0f, 1.0f, 1.0f, 1.0f, // 3 back white
    };
    private static final int stride = 24; // = 6 * Float.BYTES;

    private static final short elementsData[] = {
            3, 0, 2,    // left T: back, left, top
            1, 3, 2,    // right T: right, back, top
            0, 1, 2,    // front T: left, right, top
            0, 3, 1,    // bottom T: left, back, right
    };

    private Buffer vertexBuffer;
    private Buffer elementBuffer;

    private int[] bufferIds = new int[2];
    private SimpleGLProgram program;

    private float[] mvpMatrix;

    public PiramideModel() {
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

        GLUtil.checkGlError("PiramideModel.draw");
    }
}
