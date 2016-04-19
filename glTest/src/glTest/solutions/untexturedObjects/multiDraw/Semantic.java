/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glTest.solutions.untexturedObjects.multiDraw;

/**
 *
 * @author GBarbieri
 */
public class Semantic {

    public class Attr {

        public static final int POSITION = 0;
        public static final int COLOR = 1;
        public static final int DRAW_ID = 2;
    }

    public class Frag {

        public static final int COLOR = 0;
    }

    public class Vert {

        public static final int POSITION = 0;
        public static final int COLOR = 1;
    }

    public class Storage {

        public static final int TRANSFORM = 0;

    }

    public class Uniform {

        public static final int TRANSFORM0 = 0;
        public static final int TRANSFORM1 = 1;

    }
}
