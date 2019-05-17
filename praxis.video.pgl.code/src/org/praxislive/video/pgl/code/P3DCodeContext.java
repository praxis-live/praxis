/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2019 Neil C Smith.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * version 3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License version 3
 * along with this work; if not, see http://www.gnu.org/licenses/
 *
 *
 * Please visit https://www.praxislive.org if you need additional information or
 * have any questions.
 *
 */
package org.praxislive.video.pgl.code;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.praxislive.code.CodeComponent;
import org.praxislive.code.CodeContext;
import org.praxislive.code.PortDescriptor;
import org.praxislive.core.ExecutionContext;
import org.praxislive.logging.LogLevel;
import org.praxislive.video.pgl.PGLContext;
import org.praxislive.video.pgl.PGLGraphics;
import org.praxislive.video.pgl.PGLGraphics3D;
import org.praxislive.video.pgl.PGLSurface;
import org.praxislive.video.pgl.code.userapi.PGraphics3D;
import org.praxislive.video.pgl.code.userapi.PImage;
import org.praxislive.video.render.Surface;
import processing.core.PConstants;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class P3DCodeContext extends CodeContext<P3DCodeDelegate> {

    private final PGLVideoOutputPort.Descriptor output;
    private final PGLVideoInputPort.Descriptor[] inputs;
    private final Map<String, P3DOffScreenGraphicsInfo> offscreen;
    private final Processor processor;
    private final boolean resetOnSetup;

    private boolean setupRequired;

    public P3DCodeContext(P3DCodeConnector connector) {
        super(connector, connector.hasUpdate());
        setupRequired = true;
        output = connector.extractOutput();
        resetOnSetup = !connector.hasInit();

        List<PGLVideoInputPort.Descriptor> ins = new ArrayList<>();

        for (String id : getPortIDs()) {
            PortDescriptor pd = getPortDescriptor(id);
            if (pd instanceof PGLVideoInputPort.Descriptor) {
                ins.add((PGLVideoInputPort.Descriptor) pd);
            }
        }

        inputs = ins.toArray(new PGLVideoInputPort.Descriptor[ins.size()]);

        offscreen = connector.extractOffScreenInfo();

        processor = new Processor(inputs.length);
    }

    @Override
    protected void configure(CodeComponent<P3DCodeDelegate> cmp, CodeContext<P3DCodeDelegate> oldCtxt) {
        super.configure(cmp, oldCtxt);
        output.getPort().getPipe().addSource(processor);
        for (PGLVideoInputPort.Descriptor vidp : inputs) {
            processor.addSource(vidp.getPort().getPipe());
        }
        P3DCodeContext oldP3DCtxt = (P3DCodeContext) oldCtxt;
        configureOffScreen(oldP3DCtxt);
        configureProcessor(oldP3DCtxt);
    }

    private void configureOffScreen(P3DCodeContext oldCtxt) {
        Map<String, P3DOffScreenGraphicsInfo> oldOffscreen = oldCtxt == null
                ? Collections.EMPTY_MAP : oldCtxt.offscreen;
        offscreen.forEach( (id, osgi) -> osgi.attach(this, oldOffscreen.remove(id)));
        oldOffscreen.forEach( (id, osgi) -> osgi.release());
    }

    private void configureProcessor(P3DCodeContext oldP3DCtxt) {
        if (oldP3DCtxt != null) {
            processor.context = oldP3DCtxt.processor.context;
            processor.p3d = oldP3DCtxt.processor.p3d;
        }
    }

    @Override
    public void starting(ExecutionContext source) {
        setupRequired = true;
//        processor.dispose3D();
        try {
            getDelegate().init();
        } catch (Exception e) {
            getLog().log(LogLevel.ERROR, e, "Exception thrown during init()");
        }
    }

    @Override
    protected void stopping(ExecutionContext source, boolean fullStop) {
        processor.dispose3D();
        offscreen.forEach((id, osgi) -> osgi.release());
    }

    @Override
    protected void tick(ExecutionContext source) {
        try {
            getDelegate().update();
        } catch (Exception e) {
            getLog().log(LogLevel.ERROR, e, "Exception thrown during update()");
        }
    }

    void beginOffscreen() {
        processor.pg.pushMatrix();
    }

    void endOffscreen() {
        processor.pg.beginDraw();
        processor.pg.popMatrix();
    }

    private class Processor extends AbstractProcessPipe {

        private PGraphics pg;
        private PGLImage[] images;
        private PGLContext context;
        private PGLGraphics3D p3d;

        private Processor(int inputs) {
            super(inputs);
            images = new PGLImage[inputs];
        }

        @Override
        protected void update(long time) {
            P3DCodeContext.this.update(time);
        }

        @Override
        protected void callSources(Surface output, long time) {
            validateImages(output);
            int count = getSourceCount();
            for (int i = 0; i < count; i++) {
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

            P3DCodeDelegate del = getDelegate();

            PGLContext curCtxt = pglOut.getContext();
            if (curCtxt != context) {
                context = curCtxt;
                p3d = context.create3DGraphics(output.getWidth(), output.getHeight());
            }

            validateOffscreen(pglOut);

            p3d.beginDraw();
            p3d.clear();

            if (pg == null || pg.width != p3d.width || pg.height != p3d.height) {
                pg = new PGraphics(P3DCodeContext.this, p3d.width, p3d.height);
            }

            pg.init(p3d);
            del.configure(pglOut.getContext().parent(), pg, output.getWidth(), output.getHeight());
//            pg.resetMatrix();
            if (setupRequired) {
                if (resetOnSetup) {
                    reset(false);
                }
                p3d.style(null);
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
            p3d.endDraw();
            PGLGraphics g = pglOut.getGraphics();
            g.beginDraw();
            g.blendMode(PConstants.BLEND);
            g.tint(255.0f);
            g.image(p3d, 0, 0);
            releaseOffscreen();
            flush();
        }

        private void validateImages(Surface output) {
            P3DCodeDelegate del = getDelegate();
            for (int i = 0; i < images.length; i++) {
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

        private void setImageField(P3DCodeDelegate delegate, Field field, PImage image) {
            try {
                field.set(delegate, image);
            } catch (Exception ex) {
                getLog().log(LogLevel.ERROR, ex);
            }
        }

        private void validateOffscreen(PGLSurface output) {
            offscreen.forEach((id, osgi) -> osgi.validate(output));
        }

        private void releaseOffscreen() {
            offscreen.forEach((id, osgi) -> osgi.endFrame());
        }

        private void dispose3D() {
            if (p3d != null) {
//                p3d.dispose();
            }
            p3d = null;
            context = null;
        }

    }

    static class PGraphics extends PGraphics3D {

        private final P3DCodeContext context;

        PGLGraphics3D pgl;
        private int matrixStackDepth;

        PGraphics(P3DCodeContext context, int width, int height) {
            super(width, height);
            this.context = context;
        }

        void init(PGLGraphics3D g) {
            pgl = g;
            initGraphics(g);
            g.pushMatrix();
        }

        void release() {
            pgl = null;
            PGLGraphics3D g = releaseGraphics();
            if (matrixStackDepth != 0) {
                context.getLog().log(LogLevel.ERROR, "Mismatched matrix push / pop");
                while (matrixStackDepth > 0) {
                    g.popMatrix();
                    matrixStackDepth--;
                }
            }
            g.popMatrix();
        }

        @Override
        public void pushMatrix() {
            if (matrixStackDepth == 31) {
                context.getLog().log(LogLevel.ERROR, "Matrix stack full in popMatrix()");
                return;
            }
            matrixStackDepth++;
            super.pushMatrix();
        }

        @Override
        public void popMatrix() {
            if (matrixStackDepth == 0) {
                context.getLog().log(LogLevel.ERROR, "Matrix stack empty in popMatrix()");
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

        @Override
        public <T> Optional<T> find(Class<T> type) {
            if (processing.core.PImage.class.isAssignableFrom(type) &&
                    surface instanceof PGLSurface) {
                PGLContext ctxt = ((PGLSurface) surface).getContext();
                return Optional.of(type.cast(unwrap(ctxt)));
            } else {
                return super.find(type);
            }
        }

    }

}
