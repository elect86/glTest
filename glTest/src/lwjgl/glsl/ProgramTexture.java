/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lwjgl.glsl;

import com.jogamp.opengl.GL4;

/**
 *
 * @author gbarbieri
 */
public class ProgramTexture extends glsl.GLSLProgramObject {

    private int texture0UL;

    public ProgramTexture(GL4 gl4, String shadersFilepath, String vertexShader, String fragmentShader) {

        super(gl4, shadersFilepath, vertexShader, fragmentShader);

        texture0UL = gl4.glGetUniformLocation(getProgramId(), "tetxure0");
    }

    public int getTexture0UL() {
        return texture0UL;
    }

}
