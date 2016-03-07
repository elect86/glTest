/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glTest.problems;

import glTest.problems.Problem;
import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;
import com.jogamp.opengl.GL4;
import glm.vec._4.Vec4;

/**
 *
 * @author GBarbieri
 */
public class NullProblem extends Problem {

    @Override
    public boolean init(GL4 gl4) {
        // Nothing to initialize
        return true;
    }

    @Override
    public void render(GL4 gl4) {
    
        // Nothing, because it's the NULL solution.
    }

    @Override
    public void clear(GL4 gl4) {
    
    }

    @Override
    public String getName() {
        return "NullProblem";
    }

    public void clear(GL4 gl4, Vec4 clearColor, float clearDepth) {

        gl4.glClearColor(0.2f, 0.0f, 0.0f, 1.0f);
        gl4.glClearDepth(1.0f);
        gl4.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

}
