/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.neilcsmith.praxis.core.syntax;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class NewEmptyJUnitTest {

    private final Object lock = new Object();

    public NewEmptyJUnitTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        
    }

    @After
    public void tearDown() {

    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
//    @Test
//    public void hello() {
//        try {
//            SwingUtilities.invokeAndWait(new Runnable() {
//
//                public void run() {
//                    JFrame frame = new JFrame("Testing");
//                    frame.setSize(200, 200);
//                    frame.setVisible(true);
//                }
//            });
//        } catch (Exception ex) {
//            fail();
//        }
//
//
//    }

    @Test public void launch() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                public void run() {
                    JFrame frame = new JFrame("Testing");
                    frame.setSize(200, 200);
                    frame.addWindowListener(new WindowAdapter() {

                        @Override
                        public void windowClosing(WindowEvent e) {
                            synchronized (lock) {
                                System.out.println("Window Closing");
                                lock.notify();
                            }
                        }
                    });
                    frame.setVisible(true);
                }
            });
        } catch (Exception ex) {
            fail();
        }
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException ex) {
                fail();
            }
        }
    }

}
