/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glTest.problems;

import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import static com.jogamp.opengl.GL2ES3.GL_DEPTH;
import com.jogamp.opengl.GL4;
import glTest.framework.ApplicationState;

/**
 *
 * @author GBarbieri
 */
public class NullProblem extends Problem {

    @Override
    public boolean init(GL4 gl4) {

        super.init(gl4);

        clearColor.put(new float[]{0.2f, 0.0f, 0.0f, 1.0f}).rewind();
        clearDepth.put(new float[]{1.0f}).rewind();

        ApplicationState.animator.setUpdateFPSFrames(10_000, System.out);

        return true;
    }

    @Override
    public void render(GL4 gl4) {

        gl4.glClearBufferfv(GL_COLOR, 0, clearColor);
        gl4.glClearBufferfv(GL_DEPTH, 0, clearDepth);

        // Nothing, because it's the NULL solution.
    }

    @Override
    public String getName() {
        return "NullProblem";
    }

}
