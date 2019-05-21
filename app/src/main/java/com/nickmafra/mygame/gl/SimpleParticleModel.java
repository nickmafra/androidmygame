package com.nickmafra.mygame.gl;

import android.content.Context;

import com.nickmafra.util.Rdm;
import com.nickmafra.util.Vetor;

import java.nio.Buffer;

import static android.opengl.GLES20.*;

public class SimpleParticleModel implements GLModel {

    // position
    private static final float verticesData[] = {
            0.0f, +1.0f,  0.0f,
            -1.0f, -0.5f,  0.0f,
            +1.0f, -0.5f,  0.0f,
            0.0f, -1.0f,  0.0f,
            -1.0f, +0.5f,  0.0f,
            +1.0f, +0.5f,  0.0f,
    };
    private static final int stride = 28; // = 7 * Float.BYTES;

    private ColorGLProgram program;
    private Buffer vBuffer, aBuffer;
    private int[] bufferIds = new int[2];
    private float[] mvpMatrix;

    private int maxParticles = 500;
    private float[] p, v; // initial position, initial velocity
    private float[] iniColor = new float[] { 1, 1, 1, 1}; // rgba
    private float[] midColor = new float[] { 1, 1, 0, 1}; // rgba
    private float[] endColor = new float[] { 1, 0, 0, 1}; // rgba
    private float maxLife = 50;
    private float pSpread = 0.001f, vSpread = 0.001f, lifeSpread = 0.8f;

    private int qtParticles;
    private float[] arrayGPU; // contém posição xyz e cor rgba
    private float[] arrayCPU; // contém velocidade xyz e vida

    public SimpleParticleModel() {
        program = new ColorGLProgram();
        vBuffer = GLUtil.makeFloatBuffer(verticesData);
    }

    @Override
    public void load(Context context) {
        program.load(context);

        glGenBuffers(bufferIds.length, bufferIds, 0);
        glBindBuffer(GL_ARRAY_BUFFER, bufferIds[0]);
        glBufferData(GL_ARRAY_BUFFER, vBuffer.capacity() * 4, null, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, bufferIds[1]);
        glBufferData(GL_ARRAY_BUFFER, maxParticles * 7 * 4, null, GL_STREAM_DRAW);
    }

    @Override
    public void setMvpMatrix(float[] mvpMatrix) {
        this.mvpMatrix = mvpMatrix;
    }

    public void start() {
        arrayGPU = new float[maxParticles * 7];
        arrayCPU = new float[maxParticles * 4];
    }

    private void create(int i) {
        // position
        arrayGPU[i * 7    ] = p[0] + Rdm.nextRandom(-pSpread, pSpread);
        arrayGPU[i * 7 + 1] = p[1] + Rdm.nextRandom(-pSpread, pSpread);
        arrayGPU[i * 7 + 2] = p[2] + Rdm.nextRandom(-pSpread, pSpread);
        // velocity
        arrayCPU[i * 4    ] = v[0] + Rdm.nextRandom(-vSpread, vSpread);
        arrayCPU[i * 4 + 1] = v[1] + Rdm.nextRandom(-vSpread, vSpread);
        arrayCPU[i * 4 + 2] = v[2] + Rdm.nextRandom(-vSpread, vSpread);

        arrayCPU[i * 4 + 3] = maxLife - Rdm.nextRandom(0, maxLife * lifeSpread);
    }

    public void update() {
        int creations = 50;
        float dt = 1;
        for (int i = 0; i < maxParticles; i++) {
            float life = arrayCPU[i * 4 + 3];
            if (life <= 0) {
                if (creations > 0) {
                    create(i);
                    creations--;
                } else {
                    continue;
                }
            } else {
                arrayCPU[i * 4 + 3]--;
            }
            // p = v*dt
            arrayGPU[i * 7    ] += arrayCPU[i * 4    ] * dt;
            arrayGPU[i * 7 + 1] += arrayCPU[i * 4 + 1] * dt;
            arrayGPU[i * 7 + 2] += arrayCPU[i * 4 + 2] * dt;
            // color
            float[] inter = Vetor.interpolate(iniColor, midColor, endColor, life / maxLife);
            System.arraycopy(inter, 0, arrayGPU, i * 7 + 3, 4);
        }
    }

    private void loadGPUBuffer() {
        if (arrayGPU != null) {
            aBuffer = GLUtil.makeFloatBuffer(arrayGPU);
            glBufferSubData(GL_ARRAY_BUFFER, 0, aBuffer.capacity() * 4, aBuffer);
        }
    }

    @Override
    public void draw() {
        if (program.getId() == 0) {
            throw new RuntimeException("GLProgram não carregado.");
        }
        glUseProgram(program.getId());

        loadGPUBuffer();

        glEnableVertexAttribArray(program.getPosId());
        glEnableVertexAttribArray(program.getColorId());
        glBindBuffer(GL_ARRAY_BUFFER, bufferIds[0]);
        glVertexAttribPointer(program.getPosId(), 3, GL_FLOAT, false, stride, 0);
        glVertexAttribPointer(program.getColorId(), 3, GL_FLOAT, false, stride, 12);
    }

}
