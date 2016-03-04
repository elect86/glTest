/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jogl.glsl;

import com.jogamp.opengl.GL3;
import glsl.GLSLProgramObject;

/**
 *
 * @author elect
 */
public class ProgramBase extends GLSLProgramObject {

    private final int modelToClipUL;

    public ProgramBase(GL3 gl3, String shadersFilepath, String vertexShader, String fragmentShader) {
        
        super(gl3, shadersFilepath, vertexShader, fragmentShader);

        modelToClipUL = gl3.glGetUniformLocation(getProgramId(), "modelToClip");
    }

    public int getModelToClipUL() {
        return modelToClipUL;
    }

}
