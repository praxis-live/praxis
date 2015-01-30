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
 *
 * Parts of the API of this package, as well as some of the code, is derived from
 * the Processing project (http://processing.org)
 *
 * Copyright (c) 2004-09 Ben Fry and Casey Reas
 * Copyright (c) 2001-04 Massachusetts Institute of Technology
 *
 */
package net.neilcsmith.praxis.video.code;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import net.neilcsmith.praxis.code.AbstractAsyncProperty;
import net.neilcsmith.praxis.code.CodeConnector;
import net.neilcsmith.praxis.code.CodeContext;
import net.neilcsmith.praxis.code.ControlDescriptor;
import net.neilcsmith.praxis.code.ControlInput;
import net.neilcsmith.praxis.code.PortDescriptor;
import net.neilcsmith.praxis.code.userapi.OnChange;
import net.neilcsmith.praxis.code.userapi.OnError;
import net.neilcsmith.praxis.code.userapi.P;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.Control;
import net.neilcsmith.praxis.core.Lookup;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.core.info.ArgumentInfo;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.core.info.PortInfo;
import net.neilcsmith.praxis.core.interfaces.TaskService;
import net.neilcsmith.praxis.core.types.PError;
import net.neilcsmith.praxis.core.types.PMap;
import net.neilcsmith.praxis.core.types.PNumber;
import net.neilcsmith.praxis.core.types.PReference;
import net.neilcsmith.praxis.core.types.PResource;
import net.neilcsmith.praxis.core.types.PString;
import net.neilcsmith.praxis.logging.LogLevel;
import net.neilcsmith.praxis.video.code.userapi.PImage;
import net.neilcsmith.praxis.video.render.Surface;
import net.neilcsmith.praxis.video.render.utils.BufferedImageSurface;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
class ImageProperty extends AbstractAsyncProperty<PImage> {

    private final static ControlInfo INFO = ControlInfo.createPropertyInfo(
            new ArgumentInfo[]{PResource.info(true)},
            new Argument[]{PString.EMPTY},
            PMap.EMPTY);

    private Field field;
    private Method onChange;
    private Method onError;
    private CodeContext<?> context;

    private ImageProperty() {
        super(PString.EMPTY, PImage.class, null);
    }

    private void attach(CodeContext<?> context,
            Field field, Method onChange, Method onError) {
        super.attach(context);
        this.context = context;
        this.field = field;
        try {
            field.set(context.getDelegate(), getValue());
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            context.getLog().log(LogLevel.WARNING, ex);
        }
        this.onChange = onChange;
        this.onError = onError;
    }

    @Override
    protected TaskService.Task createTask(CallArguments keys) throws Exception {
        Argument arg = keys.get(0);
        if (arg.isEmpty()) {
            return null;
        }
        Lookup lkp = context.getLookup();
        return new Task(lkp, PResource.coerce(arg));
    }

    @Override
    public ControlInfo getInfo() {
        return INFO;
    }

    @Override
    protected void valueChanged(long time) {
        try {
            field.set(context.getDelegate(), getValue());
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            context.getLog().log(LogLevel.ERROR, ex);
        }
        if (onChange != null) {
            context.invoke(time, new CodeContext.Invoker() {

                @Override
                public void invoke() {
                    try {
                        onChange.invoke(context.getDelegate());
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        context.getLog().log(LogLevel.ERROR, ex);
                    }
                }
            });
        }
    }

    @Override
    protected void taskError(long time, PError error) {
        if (onError != null) {
            context.invoke(time, new CodeContext.Invoker() {

                @Override
                public void invoke() {
                    try {
                        onError.invoke(context.getDelegate());
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        context.getLog().log(LogLevel.ERROR, ex);
                    }
                }
            });
        }
    }
    
    private static class Task implements TaskService.Task {

        private final PResource imageFile;
        private final Lookup lookup;

        private Task(Lookup lookup, PResource imageFile) {
            this.lookup = lookup;
            this.imageFile = imageFile;
        }

        @Override
        public Argument execute() throws Exception {
            Surface s = BufferedImageSurface.load(imageFile.value());
            return PReference.wrap(new PImageImpl(s));
        }

    }

    private static class PImageImpl extends PImage {

        private final Surface surface;

        private PImageImpl(Surface surface) {
            super(surface.getWidth(), surface.getHeight());
            this.surface = surface;
        }

        @Override
        protected Surface getSurface() {
            return surface;
        }

    }

    static class Descriptor extends ControlDescriptor {

        private final Field field;
        private final Method onChange, onError;
        
        private ImageProperty control;
        
        private Descriptor(
                String id,
                int index,
                Field field,
                Method onChange,
                Method onError
        ) {
            super(id, Category.Property, index);
            this.field = field;
            this.onChange = onChange;
            this.onError = onError;
        }

        @Override
        public ControlInfo getInfo() {
            return INFO;
        }

        @Override
        public void attach(CodeContext<?> context, Control previous) {
            if (previous instanceof ImageProperty) {
                control = (ImageProperty) previous;
            } else {
                control = new ImageProperty();
            }
            control.attach(context, field, onChange, onError);
        }

        @Override
        public Control getControl() {
            return control;
        }

        public PortDescriptor createPortDescriptor() {
            return new PortDescImpl(getID(), getIndex(), control);
        }
        
        public static Descriptor create(CodeConnector<?> connector, P ann,
                Field field) {
            if (field.getType() != PImage.class) {
                return null;
            }
            field.setAccessible(true);
            String id = connector.findID(field);
            int index = ann.value();
            Method onChange = null;
            Method onError = null;
            OnChange onChangeAnn = field.getAnnotation(OnChange.class);
            if (onChangeAnn != null) {
                onChange = extractMethod(connector, onChangeAnn.value());
            }
            OnError onErrorAnn = field.getAnnotation(OnError.class);
            if (onErrorAnn != null) {
                onError = extractMethod(connector, onErrorAnn.value());
            }
            return new Descriptor(id, index, field, onChange, onError);
        }

        private static Method extractMethod(CodeConnector<?> connector, String methodName) {
            try {
                Method m = connector.getDelegate().getClass().getDeclaredMethod(methodName);
                m.setAccessible(true);
                return m;
            } catch (NoSuchMethodException | SecurityException ex) {
                connector.getLog().log(LogLevel.WARNING, ex);
                return null;
            }
        }

    }
    
    private static class PortDescImpl extends PortDescriptor implements ControlInput.Link {

        private final ImageProperty control;

        private ControlInput port;

        private PortDescImpl(String id, int index, ImageProperty control) {
            super(id, PortDescriptor.Category.Property, index);
            this.control = control;
        }

        @Override
        public void attach(CodeContext<?> context, Port previous) {
            if (previous instanceof ControlInput) {
                port = (ControlInput) previous;
                port.setLink(this);
            } else {
                if (previous != null) {
                    previous.disconnectAll();
                }
                port = new ControlInput(this);
            }
        }

        @Override
        public Port getPort() {
            assert port != null;
            return port;
        }

        @Override
        public PortInfo getInfo() {
            return ControlInput.INFO;
        }

        @Override
        public void receive(long time, double value) {
            receive(time, PNumber.valueOf(value));
        }

        @Override
        public void receive(long time, Argument value) {
            control.portInvoke(time, value);
        }

    }

}
