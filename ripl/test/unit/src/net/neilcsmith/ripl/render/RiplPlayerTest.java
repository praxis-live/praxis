/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.neilcsmith.ripl.render;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.net.URI;
import net.neilcsmith.ripl.FrameRateSource;
import net.neilcsmith.ripl.Surface;
import net.neilcsmith.ripl.FrameRateListener;
import net.neilcsmith.ripl.components.Delegator;
import net.neilcsmith.ripl.delegates.ImageDelegate;
import net.neilcsmith.ripl.impl.SingleInOut;
import net.neilcsmith.ripl.ops.GraphicsOp;
import net.neilcsmith.ripl.utils.ResizeMode;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class RiplPlayerTest {

    public RiplPlayerTest() {
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

    @Test
    public void testPlayer() throws Exception {
        RiplPlayer player = new RiplPlayer("Test", 640, 480, 20, false);
        Delegator d = new Delegator(ImageDelegate.create(new URI("file:///home/nsigma/Pictures/P4060324.JPG"), new ResizeMode(ResizeMode.Type.Stretch, 0, 0), null));
        player.getSink(0).addSource(d);
        player.addFrameRateListener(new Listener());
        player.run();
    }

    private static class GraphicsUG extends SingleInOut implements GraphicsOp.Callback {

        @Override
        protected void process(Surface surface, boolean rendering) {
            if (rendering) {
                surface.process(new GraphicsOp(this));
            }
        }

        public void draw(Graphics2D g2d, Image[] images) {
            try {
                g2d.setComposite(AlphaComposite.SrcOver.derive(0.5f));
                g2d.setColor(Color.RED);
                g2d.fillRect(100, 100, 200, 300);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
    }
    


    private static class Listener implements FrameRateListener {

        private int total, skipped;

        public void nextFrame(FrameRateSource source) {
            total++;
            if (!source.isRendering()) {
                skipped++;
            }
            if (total % 100 == 0) {
                long currentTime = System.nanoTime();
                long sourceTime = source.getTime();
                System.out.println("Frame : " + total + " Last 100 frames - skipped : " + skipped);
                skipped = 0;
                System.out.println("Current system time = " + currentTime);
                System.out.println("Current source time = " + sourceTime);
                System.out.println("Difference = " + (sourceTime - currentTime));
            }
//                if (total % 4 == 0) {
//                    snap.trigger();
//                }

        }
    }
}
