package net.neilcsmith.ripl.components.test;


import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import net.neilcsmith.ripl.Surface;
import net.neilcsmith.ripl.impl.SingleInOut;
import net.neilcsmith.ripl.ops.GraphicsOp;
/**
 *
 * @author Neil C Smith
 */
public class Hypnosis extends SingleInOut {

    public static final int DEFAULT_NUMBER_OF_SEGMENTS = 4;
    private int[] coordinates;
    private int[] deltas;
    private Paint paint;
    private int width = 0;
    private int height = 0;
//    private RGBCompositeAdaptor cmp = new RGBCompositeAdaptor(new RGBComposite.Difference(0.9f));

    public Hypnosis() {
        this(DEFAULT_NUMBER_OF_SEGMENTS, Color.BLUE, Color.RED);
    }

    public Hypnosis(int segments, Color grad1, Color grad2) {
        int coords = segments * 4 + 2;
        coordinates = new int[coords];
        deltas = new int[coords];
        for (int i = 0; i < coords; i++) {
            coordinates[i] = (int) (Math.random() * 300);
            deltas[i] = (int) (Math.random() * 4 + 3);
            if (deltas[i] > 4) {
                deltas[i] = -(deltas[i] - 3);
            }
        }
        paint = new GradientPaint(0, 0, grad1, 20, 10, grad2, true);
    }

    @Override
    protected void process(Surface surface, boolean render) {
        update();
        if (render) {
            render(surface);
        }
    }

    private void update() {
        if (width == 0 || height == 0) {
            return;
        }
        for (int i = 0; i < coordinates.length; i++) {
            coordinates[i] += deltas[i];
            int limit = (i % 2 == 0) ? width : height;
            if (coordinates[i] < 0) {
                coordinates[i] = 0;
                deltas[i] = -deltas[i];
            } else if (coordinates[i] > limit) {
                coordinates[i] = limit - 1;
                deltas[i] = -deltas[i];
            }
        }
    }

    protected void render(Surface surface) {
        width = surface.getWidth();
        height = surface.getHeight();
//        SurfaceGraphics g = surface.getGraphics();
////        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//        Shape s = createShape();
////        g.setComposite(cmp);
//        g.setPaint(paint);
//        g.fill(s);
////        g.setComposite(AlphaComposite.SrcOver);
//        g.setPaint(Color.WHITE);
//        g.draw(s);
        surface.process(new GraphicsOp(new GraphicsOp.Callback() {

            public void draw(Graphics2D g2d, Image[] images) {
                g2d.setComposite(AlphaComposite.SrcOver.derive(0.5f));
                Shape s = createShape();
                g2d.setPaint(paint);
                g2d.fill(s);
                g2d.setPaint(Color.WHITE);
                g2d.draw(s);
            }
        }));
    }

    private Shape createShape() {
        GeneralPath path = new GeneralPath();
        path.moveTo(coordinates[0], coordinates[1]);
        for (int i = 2; i < coordinates.length; i += 4) {
            path.quadTo(coordinates[i], coordinates[i + 1], coordinates[i + 2], coordinates[i + 3]);
        }
        path.closePath();
        return path;
    }
}
