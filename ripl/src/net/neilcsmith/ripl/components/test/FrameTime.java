package net.neilcsmith.ripl.components.test;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import net.neilcsmith.ripl.Surface;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import net.neilcsmith.ripl.SurfaceGraphics;
import net.neilcsmith.ripl.impl.SingleInOut;
import net.neilcsmith.ripl.ops.GraphicsOp;

/**
 *
 * @author Neil C Smith
 */
public class FrameTime extends SingleInOut {

    private Font font = new Font("Monospaced", Font.PLAIN, 24);

    @Override
    protected void process(Surface surface, boolean render) {
        if (render) {

            surface.process(new GraphicsOp(new GraphicsOp.Callback() {

                public void draw(Graphics2D g, Image[] images) {
                    g.setPaint(Color.BLUE);
                    g.setFont(font);
                    g.drawString("Frame: " + getTime(), 50, 50);
                }
            }));
        }


    }
}
