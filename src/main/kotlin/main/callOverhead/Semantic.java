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
public class Semantic {

    public class Attr {

        public static final int POSITION = 0;
    }

    public class Sampler {

        public static final int TEXTURE0 = 0;
    }

    public class Uniform {

        public static final int TRANSFORM0 = 0;
    }

    public class Mode {

        public static final int FRAMEBUFFER = 0;
        public static final int PROGRAM = 1;
        public static final int ROP = 2;
        public static final int TEXTURE = 3;
        public static final int VERTEX_FORMAT = 4;
        public static final int VERTEX_BINDING = 5;
        public static final int UBO = 6;
        public static final int UNIFORM = 7;
        public static final int MAX = 8;

    }
}
