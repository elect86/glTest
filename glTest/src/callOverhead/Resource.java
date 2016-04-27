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

}
