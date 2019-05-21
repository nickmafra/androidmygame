package com.nickmafra.util;

public class Vetor {

    private Vetor() {
    }

    public static float[] add(float[] a, float[] b) {
        float[] c = new float[Math.min(a.length, b.length)];
        for (int i = 0; i < c.length; i++) {
            c[i] = a[i] + b[i];
        }
        return c;
    }

    public static float[] subtract(float[] a, float[] b) {
        float[] c = new float[Math.min(a.length, b.length)];
        for (int i = 0; i < c.length; i++) {
            c[i] = a[i] - b[i];
        }
        return c;
    }

    public static float[] scale(float k, float[] v) {
        float[] w = new float[v.length];
        for (int i = 0; i < 3; i++) {
            w[i] = v[i] * k;
        }
        return w;
    }

    public static float[] oposite(float[] v) {
        float[] w = new float[v.length];
        for (int i = 0; i < 3; i++) {
            w[i] = -v[i];
        }
        return w;
    }

    public static float sqrMagnitude(float[] v) {
        float m2 = 0;
        for (float c : v) {
            m2 += c * c;
        }
        return m2;
    }

    public static float magnitude(float[] v) {
        return (float) Math.sqrt(sqrMagnitude(v));
    }

    public static float[] normalize(float[] v) {
        float m = magnitude(v);
        float[] n = new float[v.length];
        for (int i = 0; i < v.length; i++) {
            n[i] = v[i] / m;
        }
        return n;
    }

    public static float sqrDistance(float[] a, float[] b) {
        return sqrMagnitude(subtract(b, a));
    }

    public static float distance(float[] a, float[] b) {
        return magnitude(subtract(b, a));
    }

    public static float dotProduct(float[] a, float[] b) {
        int length = Math.min(a.length, b.length);
        float dp = 0;
        for (int i = 0; i < length; i++) {
            dp += a[i]*b[i];
        }
        return dp;
    }

    public static float[] crossProduct(float[] a, float[] b) {
        float[] cp = new float[3];
        for (int i = 0; i < 3; i++) {
            cp[i] += a[(i + 1) % 3] * b[(i + 2) % 3] - a[(i + 2) % 3] * b[(i + 1) % 3];
        }
        return cp;
    }

    /**
     * Calcula a projeção do vetor <b>a</b> na direção do vetor <b>b</b>.
     *
     * @param a o vetor a ser projetado.
     * @param b o vetor que possui a direção da projeção. Não precisa ser normalizado.
     * @return  a projeção
     */
    public static float[] projection(float[] a, float[] b) {
        return scale(dotProduct(a, b) / sqrMagnitude(b), b);
    }

    public static float[] loop(float[] v, float[] c, float loopDist) {
        int length = Math.min(v.length, c.length);
        float[] v2 = new float[length];
        for (int i = 0; i < length; i++) {
            v2[i] = v[1];
            while (v2[i] - c[i] < -loopDist / 2) {
                v2[i] += loopDist;
            }
            while (v2[i] - c[i] < loopDist / 2) {
                v2[i] -= loopDist;
            }
        }
        return v2;
    }

    public static float[] loop(float[] v, float loopDist) {
        return loop(v, new float[v.length], loopDist);
    }

    public static float[] interpolate(float[] a, float[] b, float ratio) {
        int length = Math.min(a.length, b.length);
        float[] result = new float[length];
        for (int i = 0; i < length; i++) {
            result[i] = (1 - ratio) * a[i] + ratio * b[i];
        }
        return result;
    }

    public static float[] interpolate(float[] a, float[] b, float[] c, float ratio) {
        int length = Math.min(a.length, b.length);
        float[] result = new float[length];
        for (int i = 0; i < length; i++) {
            if (ratio == 0.5f) {
                result[i] = b[i];
            } else if (ratio < 0.5f) {
                result[i] = 2 * ((0.5f - ratio) * a[i] + ratio * b[i]);
            } else if (ratio > 0.5f) {
                result[i] = 2 * ((1f - ratio) * b[i] + (0.5f + ratio) * c[i]);
            }
        }
        return result;
    }
}
