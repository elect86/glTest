/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package callOverhead.gl4.lwjgl.glsl;

import com.jogamp.opengl.GL3;

/**
 *
 * @author gbarbieri
 */
public class ProgramUniform extends glsl.GLSLProgramObject {

    private int uUL;

    public ProgramUniform(GL3 gl3, String shadersFilepath, String vertexShader, String fragmentShader) {

        super(gl3, shadersFilepath, vertexShader, fragmentShader);

        uUL = gl3.glGetUniformLocation(getProgramId(), "u");
    }

    public int getuUL() {
        return uUL;
    }

}
