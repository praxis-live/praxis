package net.neilcsmith.ripl.components.test;


import java.awt.Color;
import java.awt.Font;
import net.neilcsmith.ripl.core.Surface;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import net.neilcsmith.ripl.core.SurfaceGraphics;
import net.neilcsmith.ripl.core.impl.SingleInOut;

/**
 *
 * @author Neil C Smith
 */
public class FrameTime extends SingleInOut {

    private Font font = new Font("Monospaced", Font.PLAIN, 36);

    @Override
    protected void process(Surface surface, boolean render) {
        if (render) {
            SurfaceGraphics g = surface.getGraphics();
            g.setPaint(Color.BLUE);
            g.setFont(font);
            g.drawString("Frame: " + getTime(), 50, 50);
        }
        
//                Graphics2D g2d = image.createGraphics();
//        g2d.setColor(Color.BLUE);
//        Font oldFont = g2d.getFont();
//        g2d.setFont(font);
//        g2d.drawString("Frame: " + getTime(), 50, 50);
//        g2d.setColor(Color.BLACK);
//        g2d.setFont(oldFont);
    }
    
}
