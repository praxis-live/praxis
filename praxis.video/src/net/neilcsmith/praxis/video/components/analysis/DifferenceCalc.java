/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2012 Neil C Smith.
 * 
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 3 only, as
 * published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 3 for more details.
 * 
 * You should have received a copy of the GNU General Public License version 3
 * along with this work; if not, see http://www.gnu.org/licenses/
 * 
 * 
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 */
package net.neilcsmith.praxis.video.components.analysis;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.impl.FloatProperty;
import net.neilcsmith.praxis.impl.StringProperty;
import net.neilcsmith.praxis.video.impl.DefaultVideoInputPort;
import net.neilcsmith.praxis.video.impl.DefaultVideoOutputPort;
import net.neilcsmith.ripl.PixelData;
import net.neilcsmith.ripl.components.Placeholder;
import net.neilcsmith.ripl.SinkIsFullException;
import net.neilcsmith.ripl.SourceIsFullException;
import net.neilcsmith.ripl.Surface;
import net.neilcsmith.ripl.SurfaceOp;
import net.neilcsmith.ripl.impl.MultiInputInOut;
import net.neilcsmith.ripl.ops.DifferenceOp;

/**
 *
 * @author Neil C Smith
 */
public class DifferenceCalc extends AbstractComponent {

    private CalcComponent calc;

    public DifferenceCalc() {
        try {
            calc = new CalcComponent();
            Placeholder pl1 = new Placeholder();
            Placeholder pl2 = new Placeholder();
            calc.addSource(pl1);
            calc.addSource(pl2);
            registerPort(Port.IN + "-1", new DefaultVideoInputPort(this, pl1));
            registerPort(Port.IN + "-2", new DefaultVideoInputPort(this, pl2));
            registerPort(Port.OUT, new DefaultVideoOutputPort(this, calc));
        } catch (SinkIsFullException ex) {
            Logger.getLogger(DifferenceCalc.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SourceIsFullException ex) {
            Logger.getLogger(DifferenceCalc.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    private class CalcComponent extends MultiInputInOut implements SurfaceOp {

        private DifferenceOp op;

        public CalcComponent() {
            super(2, false);
        }


        @Override
        protected void process(Surface surface, boolean rendering) {
            if (rendering) {
                surface.clear();
                Surface input;
                int count = getSourceCount();
                if (count > 0) {
                    input = getInputSurface(0);
                    surface.copy(input);
                    input.release();
                }
                if (count > 1) {
                    input = getInputSurface(1);
                    surface.process(op, input);
                    input.release();
                }
            }
        }

        public void process(PixelData output, PixelData... inputs) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }
}
