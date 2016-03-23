/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glTest.framework;

import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;

/**
 *
 * @author elect
 */
public class GLUtilities {

    public static int createProgram(GL4 gl4, String shaderRoot, String shaderSrc) {

        ShaderCode vs = ShaderCode.create(gl4, GL_VERTEX_SHADER, 1, GLUtilities.class,
                new String[]{shaderRoot + shaderSrc + ".vert"}, true);
        ShaderCode fs = ShaderCode.create(gl4, GL_FRAGMENT_SHADER, 1, GLUtilities.class,
                new String[]{shaderRoot + shaderSrc + ".frag"}, true);

        ShaderProgram shaderProgram = new ShaderProgram();
        shaderProgram.add(vs);
        shaderProgram.add(fs);
        shaderProgram.link(gl4, System.out);

        // Flag these now, they're either attached (linked in) and will be cleaned up with the link, or the
        // link failed and we're about to lose track of them anyways.
        vs.destroy(gl4);
        fs.destroy(gl4);

        return shaderProgram.program();
    }
}
