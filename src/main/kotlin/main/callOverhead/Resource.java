/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package callOverhead;

/**
 *
 * @author GBarbieri
 */
public class Resource {

    public final static String[] VERT_SHADERS_SOURCE
            = new String[]{"standard", "standard", "standard", "uniform-buffer", "uniform"};
    public final static String[] FRAG_SHADERS_SOURCE
            = new String[]{"standard", "standard", "texture", "standard", "standard"};
    public final static String SHADERS_ROOT = "src/callOverhead/gl3/shaders";

    public class Program {

        public static final int A = 0;
        public static final int B = 1;
        public static final int TEXTURE = 2;
        public static final int UBO = 3;
        public static final int UNIFORM = 4;
        public static final int MAX = 5;
    }

    public class Framebuffer {

        public static final int A = 0;
        public static final int B = 1;
        public static final int MAX = 2;
    }

    public class Buffer {

        public static final int A = 0;
        public static final int B = 1;
        public static final int ARRAY = 2;
        public static final int TRANSFORM = 3;
        public static final int MAX = 4;
    }

    public class Texture {

        public static final int A = 0;
        public static final int B = 1;
        public static final int COLOR = 2;
        public static final int DEPTH = 3;
        public static final int MAX = 4;
    }

    public static void printStateChange(int mode) {
        switch (mode) {
            case Semantic.Mode.FRAMEBUFFER:
                System.out.println("State change: Framebuffer");
                break;
            case Semantic.Mode.PROGRAM:
                System.out.println("State change: Program");
                break;
            case Semantic.Mode.TEXTURE:
                System.out.println("State change: Texture Bindings");
                break;
            case Semantic.Mode.VERTEX_FORMAT:
                System.out.println("State change: Vertex Format");
                break;
            case Semantic.Mode.UBO:
                System.out.println("State change: UBO Bindings");
                break;
            case Semantic.Mode.VERTEX_BINDING:
                System.out.println("State change: Vertex Bindings");
                break;
            case Semantic.Mode.UNIFORM:
                System.out.println("State change: Uniform Updates");
                break;
        }
    }

    public static void printHelp() {
        System.out.println("1 - Render Target");
        System.out.println("2 - Program");
        System.out.println("3 - Texture Bindings");
        System.out.println("4 - Vertex Format");
        System.out.println("5 - UBO Bindings");
        System.out.println("6 - Vertex Bindings");
        System.out.println("7 - Uniform Updates");
    }
}
