/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2018 Neil C Smith.
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
package net.neilcsmith.praxis.video.code;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.neilcsmith.praxis.code.CodeComponent;
import net.neilcsmith.praxis.code.CodeContext;
import net.neilcsmith.praxis.code.PortDescriptor;
import net.neilcsmith.praxis.core.ExecutionContext;
import net.neilcsmith.praxis.logging.LogLevel;
import net.neilcsmith.praxis.video.code.userapi.PGraphics;
import net.neilcsmith.praxis.video.code.userapi.PImage;
import net.neilcsmith.praxis.video.render.Surface;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class VideoCodeContext<D extends VideoCodeDelegate> extends CodeContext<D> {

    private final VideoOutputPort.Descriptor output;
    private final VideoInputPort.Descriptor[] inputs;
    private final Map<String, OffScreenGraphicsInfo> offscreen;
    private final Processor processor;

    private boolean setupRequired;

    public VideoCodeContext(VideoCodeConnector<D> connector) {
        super(connector, false);
        setupRequired = true;
        output = connector.extractOutput();

        List<VideoInputPort.Descriptor> ins = new ArrayList<>();

        for (String id : getPortIDs()) {
            PortDescriptor pd = getPortDescriptor(id);
            if (pd instanceof VideoInputPort.Descriptor) {
                ins.add((VideoInputPort.Descriptor) pd);
            }
        }

        inputs = ins.toArray(new VideoInputPort.Descriptor[ins.size()]);
        
        offscreen = connector.extractOffScreenInfo();
        
        processor = new Processor(inputs.length);
    }

    @Override
    protected void configure(CodeComponent<D> cmp, CodeContext<D> oldCtxt) {
        output.getPort().getPipe().addSource(processor);
        for (VideoInputPort.Descriptor vidp : inputs) {
            processor.addSource(vidp.getPort().getPipe());
        }
        configureOffScreen((VideoCodeContext<D>) oldCtxt);
    }
    
    private void configureOffScreen(VideoCodeContext<D> oldCtxt) {
        Map<String, OffScreenGraphicsInfo> oldOffscreen = oldCtxt == null
                ? Collections.EMPTY_MAP : oldCtxt.offscreen;
        offscreen.forEach( (id, osgi) -> osgi.attach(this, oldOffscreen.remove(id)));
        oldOffscreen.forEach( (id, osgi) -> osgi.release());
    }

    @Override
    protected void starting(ExecutionContext state) {
        setupRequired = true;
    }

    @Override
    protected void stopping(ExecutionContext source, boolean fullStop) {
        if (fullStop) {
            offscreen.forEach((id, osgi) -> osgi.release());
        }
    }
    
    
    
    
    private class Processor extends AbstractProcessPipe {
        
        private SurfacePGraphics pg;
        private SurfacePImage[] images;

        private Processor(int inputs) {
            super(inputs);
            images = new SurfacePImage[inputs];
        }

        @Override
        protected void update(long time) {
            VideoCodeContext.this.update(time);
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
            output.clear();
            if (pg == null || pg.surface != output) {
                pg = new SurfacePGraphics(output);
                setupRequired = true;
            }
            VideoCodeDelegate del = getDelegate();
            del.setupGraphics(pg, output.getWidth(), output.getHeight());
            pg.beginDraw();
            validateOffscreen(output);
            if (setupRequired) {
                invokeSetup(del);
                setupRequired = false;
            }
            invokeDraw(del);
            pg.endDraw();
            endOffscreen();
            flush();
        }
        
        private void validateImages(Surface output) {
            VideoCodeDelegate del = getDelegate();
            for (int i=0; i<images.length; i++) {
                SurfacePImage img = images[i];
                Surface s = img == null ? null : img.surface;
                if (s == null || !output.checkCompatible(s, true, true)) {
                    if (s != null) {
                        s.release();
                    }
                    s = output.createSurface();
                    img = new SurfacePImage(s);
                    images[i] = img;
                    setImageField(del, inputs[i].getField(), img);
                }
            }
        }
        
        private void setImageField(VideoCodeDelegate delegate, Field field, PImage image) {
            try {
                field.set(delegate, image);
            } catch (Exception ex) {
                getLog().log(LogLevel.ERROR, ex);
            }
        }
        
        private void validateOffscreen(Surface output) {
            offscreen.forEach((id, osgi) -> osgi.validate(output));
        }
        
        private void endOffscreen() {
            offscreen.forEach((id, osgi) -> osgi.endFrame());
        }
        
        private void invokeSetup(VideoCodeDelegate delegate) {
            reset();
            try {
                delegate.setup();
            } catch (Exception ex) {
                getLog().log(LogLevel.ERROR, ex, "Exception thrown from setup()");
            }
        }
        
        private void invokeDraw(VideoCodeDelegate delegate) {
            try {
                delegate.draw();
            } catch (Exception ex) {
                getLog().log(LogLevel.ERROR, ex, "Exception thrown from draw()");
            }
        }

        

    }

    private static class SurfacePGraphics extends PGraphics {

        private final Surface surface;

        SurfacePGraphics(Surface surface) {
            super(surface.getWidth(), surface.getHeight());
            this.surface = surface;
        }

        @Override
        protected Surface getSurface() {
            return surface;
        }

    }

    private static class SurfacePImage extends PImage {

        private final Surface surface;

        public SurfacePImage(Surface surface) {
            super(surface.getWidth(), surface.getHeight());
            this.surface = surface;
        }

        @Override
        protected Surface getSurface() {
            return surface;
        }

    }

}
