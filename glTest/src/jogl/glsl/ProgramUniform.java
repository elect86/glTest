/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jogl.glsl;

import com.jogamp.opengl.GL3;

/**
 *
 * @author gbarbieri
 */
public class ProgramUniform extends ProgramBase {

    private int uUL;

    public ProgramUniform(GL3 gl3, String shadersFilepath, String vertexShader, String fragmentShader) {

        super(gl3, shadersFilepath, vertexShader, fragmentShader);

        uUL = gl3.glGetUniformLocation(getProgramId(), "u");
    }

    public int getuUL() {
        return uUL;
    }

}
