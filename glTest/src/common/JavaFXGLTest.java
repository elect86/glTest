package common;

import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;
import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.Animator;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javafx.application.Application;
import javafx.embed.swing.SwingNode;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Minimal JOGL in JavaFX example.
 *
 * @author Mac70
 */
public class JavaFXGLTest extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("JOGL Test");

        StackPane root = new StackPane();
        SwingNode swingNode = new SwingNode();
        root.getChildren().add(swingNode);

        GljPanel joglExamplePanel = new GljPanel();
        joglExamplePanel.setup();
        swingNode.setContent(joglExamplePanel);

        Scene scene = new Scene(root, 800, 600);
        
        scene.setOnKeyPressed((javafx.scene.input.KeyEvent event) -> {
            System.out.println(event.toString());
            switch(event.getCode()) {
                case ESCAPE:
                    GljPanel.animator.remove(joglExamplePanel);
                    joglExamplePanel.destroy();
            }
        });

        
        primaryStage.setScene(scene);

        primaryStage.show();
    }

}

class GljPanel extends GLJPanel implements GLEventListener {

    private static final long serialVersionUID = 1L;

    private static GLCapabilities prepareGLCapabilities() {
        GLProfile profile = GLProfile.getDefault();
        GLCapabilities caps = new GLCapabilities(profile);
//        caps.setHardwareAccelerated(true);
//        caps.setDoubleBuffered(true);
        return caps;
    }

    static Animator animator;
    private double tick;

    public GljPanel() {
        super(prepareGLCapabilities());
    }

    public void setup() {
        addGLEventListener(this);
//        addKeyListener(this);
        animator = new Animator(this);
        animator.start();
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        System.out.println("init");
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        System.out.println("dispose");
        System.exit(0);
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL2GL3 g = drawable.getGL().getGL2GL3();
        g.glClearColor(0, (float) Math.sin(tick * Math.PI), 1, 1);
        g.glClear(GL_COLOR_BUFFER_BIT);

        tick = (tick + 0.01) % 1;
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        System.out.println("reshape");
    }

//    @Override
//    public void keyPressed(KeyEvent e) {
//        switch(e.getKeyCode()) {
//            case KeyEvent.VK_ESCAPE:
//                animator.remove(this);
//        }
//    }
//
//    @Override
//    public void keyReleased(KeyEvent e) {
//    
//    }
//    @Override
//    public void keyTyped(KeyEvent e) {
//        System.out.println("typed");
//        System.out.println(e.toString());
//        System.out.println(""+e.getExtendedKeyCode());
//        System.out.println(""+e.getID());
//        System.out.println(""+e.getKeyChar());
//        System.out.println(""+e.getKeyCode());
//        System.out.println(""+e.getKeyLocation());
//        char esc = 'E';
//        switch (e.getKeyCode()) {
//            case KeyEvent.VK_ESCAPE:
//                animator.remove(this);
//                destroy();
//        }
//    }
//
//    @Override
//    public void keyPressed(KeyEvent e) {
//        System.out.println("pressed");
//    }
//
//    @Override
//    public void keyReleased(KeyEvent e) {
//        System.out.println("released");
//    }
}
