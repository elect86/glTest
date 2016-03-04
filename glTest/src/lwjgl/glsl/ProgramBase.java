/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lwjgl.glsl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import org.lwjgl.opengl.GL20;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;

/**
 *
 * @author elect
 */
public class ProgramBase {

    private final int modelToClipUL;
    private final int id;

    public ProgramBase(String shadersFilepath, String vertexShader, String fragmentShader) {

        int vs = loadShader(shadersFilepath + "VS.glsl", GL_VERTEX_SHADER);
        int fs = loadShader(shadersFilepath + "FS.glsl", GL_FRAGMENT_SHADER);
        id = GL20.glCreateProgram();
        GL20.glAttachShader(id, vs);
        GL20.glAttachShader(id, fs);
        
        GL20.glLinkProgram(id);
        GL20.glValidateProgram(id);

        modelToClipUL = GL20.glGetUniformLocation(id, "modelToClip");
    }

    private int loadShader(String filename, int type) {
        StringBuilder shaderSource = new StringBuilder();
        int shaderID;

        try {
            try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    shaderSource.append(line).append("\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Could not read file.");
            System.exit(-1);
        }

        shaderID = GL20.glCreateShader(type);
        GL20.glShaderSource(shaderID, shaderSource);
        GL20.glCompileShader(shaderID);

        return shaderID;
    }

    public int getModelToClipUL() {
        return modelToClipUL;
    }

    public int getId() {
        return id;
    }

}
