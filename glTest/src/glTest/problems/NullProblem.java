/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glTest.problems;

import glTest.problems.Problem;
import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;
import com.jogamp.opengl.GL3;
import glm.vec._4.Vec4;

/**
 *
 * @author GBarbieri
 */
public class NullProblem extends Problem {

    @Override
    public boolean init(GL3 gl3) {
        // Nothing to initialize
        return true;
    }

    @Override
    public void render(GL3 gl3) {
    
        // Nothing, because it's the NULL solution.
    }

    @Override
    public void clear(GL3 gl3) {
    
    }

    @Override
    public String getName() {
        return "NullProblem";
    }

    public void clear(GL3 gl3, Vec4 clearColor, float clearDepth) {

        gl3.glClearColor(0.2f, 0.0f, 0.0f, 1.0f);
        gl3.glClearDepth(1.0f);
        gl3.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

}
