/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Neil C Smith.
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
 *
 */
package net.neilcsmith.praxis.video.pgl.code;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.code.CodeComponent;
import net.neilcsmith.praxis.code.CodeContext;
import net.neilcsmith.praxis.code.PortDescriptor;
import net.neilcsmith.praxis.core.ExecutionContext;
import net.neilcsmith.praxis.video.pgl.PGLGraphics;
import net.neilcsmith.praxis.video.pgl.PGLGraphics3D;
import net.neilcsmith.praxis.video.pgl.PGLSurface;
import net.neilcsmith.praxis.video.pgl.code.userapi.PGraphics3D;
import net.neilcsmith.praxis.video.pgl.code.userapi.PImage;
import net.neilcsmith.praxis.video.pipes.impl.MultiInOut;
import net.neilcsmith.praxis.video.render.Surface;
import processing.core.PConstants;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class P3DCodeContext extends CodeContext<P3DCodeDelegate> {

    private final StateListener stateListener;

    private final PGLVideoOutputPort.Descriptor output;
    private final PGLVideoInputPort.Descriptor[] inputs;
    private final Processor processor;

    private ExecutionContext execCtxt;
    private boolean setupRequired;

    public P3DCodeContext(P3DCodeConnector connector) {
        super(connector);
        stateListener = new StateListener();
        setupRequired = true;
        output = connector.extractOutput();

        List<PGLVideoInputPort.Descriptor> ins = new ArrayList<>();

        for (String id : getPortIDs()) {
            PortDescriptor pd = getPortDescriptor(id);
            if (pd instanceof PGLVideoInputPort.Descriptor) {
                ins.add((PGLVideoInputPort.Descriptor) pd);
            }
        }

        inputs = ins.toArray(new PGLVideoInputPort.Descriptor[ins.size()]);
        processor = new Processor(inputs.length);
    }

    @Override
    protected void configure(CodeComponent<P3DCodeDelegate> cmp, CodeContext<P3DCodeDelegate> oldCtxt) {
        super.configure(cmp, oldCtxt);
        output.getPort().getPipe().addSource(processor);
        for (PGLVideoInputPort.Descriptor vidp : inputs) {
            processor.addSource(vidp.getPort().getPipe());
        }
    }

    @Override
    protected void hierarchyChanged() {
        super.hierarchyChanged();
        ExecutionContext ctxt = getLookup().get(ExecutionContext.class);
        if (execCtxt != ctxt) {
            if (execCtxt != null) {
                execCtxt.removeStateListener(stateListener);
            }
            if (ctxt != null) {
                ctxt.addStateListener(stateListener);
                stateListener.stateChanged(ctxt);
            }
            execCtxt = ctxt;
        }
    }

    @Override
    public long getTime() {
        return execCtxt.getTime();
    }

    private class StateListener implements ExecutionContext.StateListener {

        @Override
        public void stateChanged(ExecutionContext source) {
            setupRequired = true;
            processor.dispose3D();
        }

    }

    private class Processor extends MultiInOut {

        private final PGraphics pg;
        private PGLImage[] images;
        private processing.core.PApplet applet;
        private PGLGraphics3D p3d;

        private Processor(int inputs) {
            super(inputs, 1);
            images = new PGLImage[inputs];
            pg = new PGraphics();
        }

        @Override
        protected void process(Surface[] in, Surface output, int index, boolean rendering) {
            PGLSurface pglOut = output instanceof PGLSurface ? (PGLSurface) output : null;
            output.clear();

            if (pglOut == null) {
                return;
            }

            P3DCodeDelegate del = getDelegate();

            for (int i = 0; i < in.length; i++) {
                PGLImage img = images[i];
                processing.core.PImage inImg = ((PGLSurface) in[i]).getGraphics();
                if (img == null || img.img != inImg) {
                    img = new PGLImage(inImg);
                    setImageField(del, inputs[i].getField(), img);
                }
            }

            processing.core.PApplet curApp = pglOut.getGraphics().parent;
            if (curApp != applet) {
                applet = curApp;
                p3d = (PGLGraphics3D) curApp.createGraphics(
                        output.getWidth(), output.getHeight(),
                        PGLGraphics3D.ID);
            }

            p3d.beginDraw();
            p3d.clear();
            pg.setGraphics(p3d);
            del.setupGraphics(pg, output.getWidth(), output.getHeight());
            processClock();
//            pg.resetMatrix();
            if (setupRequired) {
                try {
                    del.setup();
                } catch (Exception ex) {
                    Logger.getLogger(P3DCodeContext.class.getName()).log(Level.SEVERE, null, ex);
                }
                setupRequired = false;
            }
            try {
                del.draw();
            } catch (Exception ex) {
                Logger.getLogger(P3DCodeContext.class.getName()).log(Level.SEVERE, null, ex);
            }
            p3d.endDraw();
            PGLGraphics g = pglOut.getGraphics();
            g.beginDraw();
            g.blendMode(PConstants.REPLACE);
            g.image(p3d, 0, 0);
        }

        private void setImageField(P3DCodeDelegate delegate, Field field, PImage image) {
            try {
                field.set(delegate, image);
            } catch (Exception ex) {
                Logger.getLogger(P3DCodeContext.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        private void dispose3D() {
            if (p3d != null) {
                p3d.dispose();
            }
            p3d = null;
            applet = null;
        }

    }

    private static class PGraphics extends PGraphics3D {

        private void setGraphics(processing.core.PGraphics graphics) {
            g = graphics;
        }

    }

    private static class PGLImage extends PImage {

        private final processing.core.PImage img;

        private PGLImage(processing.core.PImage img) {
            super(img.width, img.height);
            this.img = img;
        }

        @Override
        protected processing.core.PImage unwrap() {
            return img;
        }

    }

}
