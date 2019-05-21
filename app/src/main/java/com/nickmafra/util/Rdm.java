package com.nickmafra.util;

import java.util.Random;

public class Rdm {

    public static final Random random = new Random();

    public static float nextRandom(float min, float max) {
        return min + (max - min) * random.nextFloat();
    }

    public static float[] nextSpherePosition() {
        float a1 = nextRandom(0, 2 * (float) Math.PI);
        float a2 = nextRandom(0, 2 * (float) Math.PI);
        return new float[] {
                (float) (Math.sin(a1) * Math.cos(a2)),
                (float) (Math.sin(a1) * Math.sin(a2)),
                (float) Math.cos(a1)
        };
    }
}
