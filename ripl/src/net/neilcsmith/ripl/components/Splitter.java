package net.neilcsmith.ripl.components;


import net.neilcsmith.ripl.Surface;
import net.neilcsmith.ripl.impl.CachedInOut;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Neil C Smith
 */
public class Splitter extends CachedInOut {

    public Splitter() {
        super(1,2,false);
    }

    @Override
    protected void process(Surface surface, boolean rendering) {
        if (rendering) {
            if (getSourceCount() == 0) {
                surface.clear();
            } else {
                Surface input = getInputSurface(0);
                if (surface == input) {
                    return;
                }
                if (surface.hasAlpha()) {
                    surface.clear();
                }
                surface.copy(getInputSurface(0));
            }
        }
    }
    
}
