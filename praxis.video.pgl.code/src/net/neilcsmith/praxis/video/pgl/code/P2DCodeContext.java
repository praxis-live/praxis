/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Neil C Smith.
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
import net.neilcsmith.praxis.code.CodeComponent;
import net.neilcsmith.praxis.code.CodeContext;
import net.neilcsmith.praxis.code.PortDescriptor;
import net.neilcsmith.praxis.core.ExecutionContext;
import net.neilcsmith.praxis.logging.LogLevel;
import net.neilcsmith.praxis.video.pgl.PGLContext;
import net.neilcsmith.praxis.video.pgl.PGLGraphics;
import net.neilcsmith.praxis.video.pgl.PGLSurface;
import net.neilcsmith.praxis.video.pgl.code.userapi.PGraphics2D;
import net.neilcsmith.praxis.video.pgl.code.userapi.PImage;
import net.neilcsmith.praxis.video.render.Surface;
import processing.core.PStyle;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class P2DCodeContext extends CodeContext<P2DCodeDelegate> {

    private final StateListener stateListener;

    private final PGLVideoOutputPort.Descriptor output;
    private final PGLVideoInputPort.Descriptor[] inputs;
    private final Processor processor;

    private ExecutionContext execCtxt;
    private boolean setupRequired;

    public P2DCodeContext(P2DCodeConnector connector) {
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
    protected void configure(CodeComponent<P2DCodeDelegate> cmp, CodeContext<P2DCodeDelegate> oldCtxt) {
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

    private class StateListener implements ExecutionContext.StateListener {

        @Override
        public void stateChanged(ExecutionContext source) {
            setupRequired = true;
        }

    }

    private class Processor extends AbstractProcessPipe {

        private final PGraphics pg;
        private PGLImage[] images;

        private Processor(int inputs) {
            super(inputs);
            images = new PGLImage[inputs];
            pg = new PGraphics();
        }

        @Override
        protected void update(long time) {
            P2DCodeContext.this.update(time);
        }

        @Override
        protected void callSources(Surface output, long time) {
            validateImages(output);
            int count = getSourceCount();
            for (int i=0; i < count; i++) {
                callSource(getSource(i), images[i].surface, time);
            }
        }
        
        @Override
        protected void render(Surface output, long time) {
            PGLSurface pglOut = output instanceof PGLSurface ? (PGLSurface) output : null;
            output.clear();

            if (pglOut == null) {
                return;
            }

            P2DCodeDelegate del = getDelegate();

            pg.init(pglOut.getGraphics(), setupRequired);
            del.setupGraphics(pg, output.getWidth(), output.getHeight());
            if (setupRequired) {
                try {
                    del.setup();
                } catch (Exception ex) {
                    getLog().log(LogLevel.ERROR, ex);
                }
                setupRequired = false;
            }
            try {
                del.draw();
            } catch (Exception ex) {
                getLog().log(LogLevel.ERROR, ex);
            }
            pg.release();
            flush();
        }
        
        private void validateImages(Surface output) {
            P2DCodeDelegate del = getDelegate();
            for (int i=0; i<images.length; i++) {
                PGLImage img = images[i];
                Surface s = img == null ? null : img.surface;
                if (s == null || !output.checkCompatible(s, true, true)) {
                    if (s != null) {
                        s.release();
                    }
                    s = output.createSurface();
                    img = new PGLImage(s);
                    images[i] = img;
                    setImageField(del, inputs[i].getField(), img);
                }
            }
        }

        private void setImageField(P2DCodeDelegate delegate, Field field, PImage image) {
            try {
                field.set(delegate, image);
            } catch (Exception ex) {
                getLog().log(LogLevel.ERROR, ex);
            }
        }

    }

    private class PGraphics extends PGraphics2D {

        private int matrixStackDepth;
        private PStyle styles;
        
        private void init(PGLGraphics pgl, boolean setupRequired) {
            pgl.beginDraw();
            pgl.resetMatrix();
            pgl.pushStyle();
            pgl.style(setupRequired ? null : styles);
            initGraphics(pgl);
        }

        private void release() {
            PGLGraphics pgl = releaseGraphics();
            if (matrixStackDepth != 0) {
                getLog().log(LogLevel.ERROR, "Mismatched matrix push / pop");
                while (matrixStackDepth > 0) {
                    pgl.popMatrix();
                    matrixStackDepth--;
                }
            }
            styles = pgl.getStyle(styles);
            pgl.popStyle();
            pgl.resetMatrix();
            pgl.resetShader();
        }

        @Override
        public void pushMatrix() {
            if (matrixStackDepth == 32) {
                getLog().log(LogLevel.ERROR, "Matrix stack full in popMatrix()");
                return;
            }
            matrixStackDepth++;
            super.pushMatrix();
        }

        @Override
        public void popMatrix() {
            if (matrixStackDepth == 0) {
                getLog().log(LogLevel.ERROR, "Matrix stack empty in popMatrix()");
                return;
            }
            matrixStackDepth--;
            super.popMatrix();
        }

    }

    private static class PGLImage extends PImage {

        private final Surface surface;

        private PGLImage(Surface s) {
            super(s.getWidth(), s.getHeight());
            this.surface = s;
        }

        @Override
        protected processing.core.PImage unwrap(PGLContext context) {
            return context.asImage(surface);
        }

    }

}
