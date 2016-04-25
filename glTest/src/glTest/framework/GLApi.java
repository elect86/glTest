/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glTest.framework;

import com.jogamp.opengl.GL4;

/**
 *
 * @author GBarbieri
 */
public class GLApi {

    public static final int OpenGLGeneric = 0;
    public static final int OpenGLCore = 1;
    public static final int MAX = 2;

    public static final int doubleBuffer = 2;
    public static final int tripleBuffer = 3;

    public static final boolean DEBUG = false;

    public static int getError(GL4 gl4) {
        return DEBUG ? gl4.glGetError() : GL4.GL_NO_ERROR;
    }
}
