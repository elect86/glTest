/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package framework;

import glm.vec._2.i.Vec2i;
import java.util.ArrayList;
import problems.Problem;

/**
 *
 * @author GBarbieri
 */
public class ApplicationState {

    private OpenGLBase[] glApis = new OpenGLBase[GLApi.MAX];
    private Vec2i position = new Vec2i(0, 0);
    private Vec2i resolution = new Vec2i(1024, 768);
    private ProblemFactory factory;
    private ArrayList<Problem> problems;

    public ApplicationState() {
        
        createGLApis();
        
        problems = factory.getProblems();
    }

    private void createGLApis() {

        glApis[GLApi.OpenGLGeneric] = new OpenGLGeneric("gltest - OpenGL (compatibility)", position.x, position.y,
                resolution.x, resolution.y);

        glApis[GLApi.OpenGLCore] = new OpenGLCore("gltest - OpenGL (core)", position.x, position.y,
                resolution.x, resolution.y);
    }

}
