/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.neilcsmith.ripl.render.opengl;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.net.URI;
import net.neilcsmith.ripl.FrameRateSource;
import net.neilcsmith.ripl.Surface;
import net.neilcsmith.ripl.FrameRateListener;
import net.neilcsmith.ripl.Player;
import net.neilcsmith.ripl.components.Delegator;
import net.neilcsmith.ripl.components.Placeholder;
import net.neilcsmith.ripl.components.Snapshot;
import net.neilcsmith.ripl.components.mix.Composite;
import net.neilcsmith.ripl.components.mix.XFader;
import net.neilcsmith.ripl.components.test.Noise;
import net.neilcsmith.ripl.delegates.Delegate;
import net.neilcsmith.ripl.delegates.ImageDelegate;
import net.neilcsmith.ripl.delegates.VideoDelegate;
import net.neilcsmith.ripl.gstreamer.V4LDelegate;
import net.neilcsmith.ripl.ops.GraphicsOp;
import net.neilcsmith.ripl.render.sw.SWPlayer;
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
    private Snapshot snap;
    


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

        System.setProperty("org.lwjgl.librarypath", "/home/nsigma");
        final Player player = GLPlayer.create("Test", 640, 480, 1, false);
        Delegator img = new Delegator(ImageDelegate.create(new URI("file:///home/nsigma/Pictures/P4060324.JPG"), new ResizeMode(ResizeMode.Type.Stretch, 0, 0), null));
        VideoDelegate cam = V4LDelegate.create(new URI("v4l2://0"));
        cam.initialize();
        cam.play();
        Delegator d = new Delegator(cam);

        snap = new Snapshot();
        snap.setFadeTime(0.5);
        snap.addSource(d);
        snap.setMix(1);
        snap.trigger();
        
        Composite cmp = new Composite();
        cmp.addSource(new Placeholder());
//        cmp.addSource(snap);
        player.getSink(0).addSource(snap);
        player.addFrameRateListener(new Listener());
        Thread thr = new Thread(new Runnable() {

            @Override
            public void run() {
                player.run();
            }
        });
        thr.start();
        thr.join();
    }

    private static class GraphicsUG implements Delegate, GraphicsOp.Callback {
        
        int x;

        @Override
        public void process(Surface surface) {
            surface.process(new GraphicsOp(this));

        }

        public void draw(Graphics2D g2d, Image[] images) {
            try {
                g2d.setComposite(AlphaComposite.SrcOver.derive(0.5f));
                g2d.setColor(Color.WHITE);
                g2d.fillRect(x, 100, 200, 400);
                x++;
                if (x > 400) {
                    x = 0;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }

        @Override
        public void update(long time) {
            // no op
        }

        @Override
        public boolean forceRender() {
            return false;
        }

        @Override
        public boolean usesInput() {
            return true;
        }
    }

    private class Listener implements FrameRateListener {

        private int total, skipped;
        private int frames;

        public void nextFrame(FrameRateSource source) {
            
            frames++;
            if (frames % 4 == 0) {
                frames = 0;
                snap.trigger();
            }
            
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
