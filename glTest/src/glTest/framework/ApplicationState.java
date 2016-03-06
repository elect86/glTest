/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glTest.framework;

import glm.vec._2.i.Vec2i;
import java.util.ArrayList;
import glTest.problems.Problem;
import glTest.solutions.Solution;

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
    private Problem activeProblem;
    private Solution activeSolution;

    public ApplicationState() {

        createGLApis();

        problems = factory.getProblems();
        assert (problems.size() > 0);
        
        activeProblem = problems.get(0);
        activeSolution = factory.getSolutions().get(activeProblem.getName());
    }

    private void createGLApis() {

        glApis[GLApi.OpenGLGeneric] = new OpenGLGeneric("gltest - OpenGL (compatibility)", position.x, position.y,
                resolution.x, resolution.y);

        glApis[GLApi.OpenGLCore] = new OpenGLCore("gltest - OpenGL (core)", position.x, position.y,
                resolution.x, resolution.y);
    }

}
