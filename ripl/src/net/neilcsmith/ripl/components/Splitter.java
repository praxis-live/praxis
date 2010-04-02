package net.neilcsmith.ripl.components;


import net.neilcsmith.ripl.core.Surface;
import net.neilcsmith.ripl.core.impl.MultiOutputInOut;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Neil C Smith
 */
public class Splitter extends MultiOutputInOut {

    public Splitter() {
        super(2);
    }

    @Override
    protected void process(Surface surface, boolean rendering) {
        if (rendering) {
            if (getSourceCount() == 0) {
                surface.clear();
            } else {
                if (surface.hasAlpha()) {
                    surface.clear();
                }
                surface.getGraphics().drawSurface(getInputSurface(0), 0, 0);
            }
        }
    }
    
}
