/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Neil C Smith.
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
package net.neilcsmith.praxis.video.java.components;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.logging.Logger;
import net.neilcsmith.praxis.compiler.ClassBodyCompiler;
import net.neilcsmith.praxis.compiler.ClassBodyContext;
import net.neilcsmith.praxis.compiler.CompilationException;
import net.neilcsmith.praxis.core.interfaces.TaskService.Task;
import net.neilcsmith.praxis.java.Output;
import net.neilcsmith.praxis.java.Param;
import net.neilcsmith.praxis.java.Trigger;
import net.neilcsmith.praxis.video.java.PImage;
import net.neilcsmith.praxis.video.java.VideoCodeDelegate;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.info.ArgumentInfo;
import net.neilcsmith.praxis.core.interfaces.TaskService;
import net.neilcsmith.praxis.core.types.PMap;
import net.neilcsmith.praxis.core.types.PReference;
import net.neilcsmith.praxis.core.types.PResource;
import net.neilcsmith.praxis.core.types.PString;
import net.neilcsmith.praxis.impl.AbstractAsyncProperty;
import net.neilcsmith.praxis.java.CodeContext;
import net.neilcsmith.praxis.java.CodeDelegate;
import net.neilcsmith.praxis.java.impl.AbstractJavaComponent;
import net.neilcsmith.praxis.video.java.VideoCodeContext;
import net.neilcsmith.praxis.video.render.Surface;
import net.neilcsmith.praxis.video.render.utils.BufferedImageSurface;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public abstract class AbstractJavaVideoComponent extends AbstractJavaComponent {

    private final static Logger LOG = Logger.getLogger(AbstractJavaVideoComponent.class.getName());

    private static final String[] IMPORTS = {
        "java.util.*",
        "net.neilcsmith.praxis.video.render.*",
        "net.neilcsmith.praxis.video.render.ops.*",
        "net.neilcsmith.praxis.java.*",
        "net.neilcsmith.praxis.video.java.*",
        "static net.neilcsmith.praxis.java.Constants.*",
        "static net.neilcsmith.praxis.video.java.VideoConstants.*"
    };
    
    private final static String TEMPLATE = "\npublic void setup() {\n  \n}\n\npublic void draw() {\n  \n}";
    
    private PImage[] images;

    protected AbstractJavaVideoComponent() {
        images = new PImage[0];
    }

    protected void buildImageControls(String prefix, int count, int ports) {
        if (count < 1) {
            images = new PImage[0];
        } else {
            images = new PImage[count];
            for (int i=0; i < count; i++) {
                images[i] = new PImage(null);
                ImageProperty control = new ImageProperty(i);
                registerControl(prefix + (i + 1), control);
                if (i < ports) {
                    registerPort(prefix + (i + 1), control.createPort());
                }
            }
        }
    }

    protected void setupCodeControl() {
        registerControl("code", new CodeProperty());
    }

    @Override
    protected void installController(CodeDelegate.Controller controller) {
        super.installController(controller);
        if (controller instanceof VideoCodeDelegate.Controller) {
            installToDelegator(new ControllerDelegate((VideoCodeDelegate.Controller) controller));
        } else {
            installToDelegator(null);
        }
    }

    @Override
    protected CodeContext getCodeContext() {
        return new CodeContextImpl(super.getCodeContext());
    }

    
    protected abstract void installToDelegator(ControllerDelegate delegate);
    
    protected static class ControllerDelegate {
        
        private final static Surface[] EMPTY_SOURCES = new Surface[0];
        
        private VideoCodeDelegate.Controller controller;
        
        private ControllerDelegate(VideoCodeDelegate.Controller controller) {
            this.controller = controller;
        }

        public void process(Surface surface) {
            controller.process(surface, EMPTY_SOURCES);
        }

        public void update(long time) {
            // no op
        }

        public boolean forceRender() {
            return false;
        }

        public boolean usesInput() {
            return true;
        }

        public void process(Surface surface, Surface... sources) {
            controller.process(surface, sources == null ? EMPTY_SOURCES : sources);
        }
        
    }

    private class CodeProperty extends AbstractAsyncProperty<VideoCodeDelegate> {

        private CodeProperty() {
            super(ArgumentInfo.create(
                    PString.class, PMap.create(
                    PString.KEY_MIME_TYPE, "text/x-praxis-java",
                    ArgumentInfo.KEY_TEMPLATE, TEMPLATE)), 
                    VideoCodeDelegate.class, PString.EMPTY);
        }

        @Override
        protected TaskService.Task createTask(CallArguments keys) throws Exception {
            Argument code;
            if (keys.getSize() < 1 || (code = keys.get(0)).isEmpty()) {
                return null;
            } else {
                return new CompilerTask(code.toString());
            }

        }

        @Override
        protected void valueChanged(long time) {
//            setDelegate(getValue());
            VideoCodeDelegate delegate = getValue();
            if (delegate != null) {
                installController(new VideoCodeDelegate.Controller(delegate));
            } else {
                installController(null);
            }
        }

        @Override
        protected void taskError(long time) {
            Logger.getLogger(getClass().getName()).warning("Error loading class");
        }
    }

    private class CompilerTask implements TaskService.Task {

        private final String code;

        private CompilerTask(String code) {
            this.code = code;
        }

        public Argument execute() throws Exception {
            Class<VideoCodeDelegate> cls;
            try {
                cls = compile(code);
            } catch (Exception ex) {
                String cc = attemptCorrection(code);
                cls = compile(cc);
            } 
            return PReference.wrap(cls.newInstance());
        }
        
        private Class<VideoCodeDelegate> compile(String code) throws Exception {
            return ClassBodyCompiler.getDefault()
                        .compile(new ContextImpl(), code);
        }
        
        //@TODO move this to ClassBodyCompiler?
        private String attemptCorrection(String code) throws IOException {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(new StringReader(code));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                if (line.startsWith("void")) {
                    line = line.replaceAll("void *draw", "public void draw");
                    line = line.replaceAll("void *setup", "public void setup");
                }
                sb.append(line);
                sb.append("\n");
            }
            return sb.toString();
            
        }
        
    }
    
    private static class ContextImpl extends ClassBodyContext<VideoCodeDelegate> {
        
        private ContextImpl() {
            super(VideoCodeDelegate.class);
        }

        @Override
        public String[] getDefaultImports() {
            return IMPORTS.clone();
        }
    }

    private class ImageProperty extends AbstractAsyncProperty<Surface> {

        private int index;

        private ImageProperty(int index) {
            super(PResource.info(), Surface.class, PString.EMPTY);
            this.index = index;
        }

        @Override
        protected Task createTask(CallArguments keys) throws Exception {
            Argument key = keys.get(0);
            if (key.isEmpty()) {
                return null;
            } else {
                URI uri = PResource.coerce(key).value();
                return new SurfaceLoaderTask(uri);
            }
        }

        @Override
        protected void valueChanged(long time) {
            images[index].setSurface(getValue());
        }

        @Override
        protected void taskError(long time) {
            Logger.getLogger(getClass().getName()).warning("Error loading class");
        }



    }

    private class SurfaceLoaderTask implements TaskService.Task {

        private URI uri;

        private SurfaceLoaderTask(URI uri) {
            this.uri = uri;
        }

        public Argument execute() throws Exception {
            return PReference.wrap(BufferedImageSurface.load(uri));
        }

    }

    private class CodeContextImpl extends VideoCodeContext {

        private CodeContext parent;

        private CodeContextImpl(CodeContext parent) {
            this.parent = parent;
        }

        @Override
        public PImage getImage(int index) {
            return images[index];
        }

        @Override
        public int getImageCount() {
            return images.length;
        }

        @Override
        public long getTime() {
            return parent.getTime();
        }

        @Override
        public Param getParam(int index) {
            return parent.getParam(index);
        }

        @Override
        public int getParamCount() {
            return parent.getParamCount();
        }

        @Override
        public Trigger getTrigger(int index) {
            return parent.getTrigger(index);
        }

        @Override
        public int getTriggerCount() {
            return parent.getTriggerCount();
        }

        @Override
        public Output getOutput(int index) {
            return parent.getOutput(index);
        }

        @Override
        public int getOutputCount() {
            return parent.getOutputCount();
        }

    }
    


}
