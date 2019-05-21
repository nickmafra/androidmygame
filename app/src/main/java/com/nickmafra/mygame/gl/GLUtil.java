package com.nickmafra.mygame.gl;

import static android.opengl.GLES30.*;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class GLUtil {

    private static final String TAG = "GLUtil";

    public static ByteBuffer makeByteBuffer(int arrayLenght, int bytesPerType) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(arrayLenght * bytesPerType);
        buffer.order(ByteOrder.nativeOrder());
        return buffer;
    }

    public static FloatBuffer makeFloatBuffer(float[] array) {
        FloatBuffer buffer = makeByteBuffer(array.length, 4).asFloatBuffer();
        buffer.put(array);
        buffer.position(0);
        return buffer;
    }

    public static ShortBuffer makeShortBuffer(short[] array) {
        ShortBuffer buffer = makeByteBuffer(array.length, 2).asShortBuffer();
        buffer.put(array);
        buffer.position(0);
        return buffer;
    }

    public static int compileShader(int type, String shaderCode) {
        int shaderID = glCreateShader(type);
        checkGlError("glCreateShader");
        glShaderSource(shaderID, shaderCode);
        glCompileShader(shaderID);
        // verifica erros
        int[] status = new int[1];
        glGetShaderiv(shaderID, GL_COMPILE_STATUS, status, 0);
        if (status[0] == 0) {
            Log.e(TAG, "Could not compile shader:" + glGetShaderInfoLog(shaderID));
            glDeleteShader(shaderID);
            shaderID = 0;
        }
        return shaderID;
    }

    public static int linkProgram(int... shaderIDs) {
        int programID = glCreateProgram();
        for (int shaderID : shaderIDs) {
            glAttachShader(programID, shaderID);
        }
        glLinkProgram(programID);
        // verifica erros
        int[] status = new int[1];
        glGetProgramiv(programID, GL_LINK_STATUS, status, 0);
        if (status[0] == 0) {
            Log.e(TAG, "Could not link program:");
            Log.e(TAG, glGetProgramInfoLog(programID));
            glDeleteProgram(programID);
            programID = 0;
        }
        return programID;
    }

    public static InputStream loadAsset(Context context, String path) {
        InputStream in = null;
        try {
            in = context.getAssets().open(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (in == null) {
            throw new RuntimeException("Recurso não encontrado");
        }
        return in;
    }

    public static String loadShaderCode(Context context, String path) {
        InputStream in = loadAsset(context, path);
        String shaderCode;
        try {
            StringBuilder buffer = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null)
                buffer.append(line);
            shaderCode = buffer.toString();
        } catch (IOException e) {
            e.printStackTrace();
            shaderCode = null;
        }
        return shaderCode;
    }

    public static void checkGlError(String glOperation) {
        int error;
        while ((error = glGetError()) != GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
        }
    }
}
