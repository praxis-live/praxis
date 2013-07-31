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
package net.neilcsmith.praxis.video.components;

import java.awt.Dimension;
import java.net.URI;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.ControlPort;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.core.interfaces.TaskService;
import net.neilcsmith.praxis.core.types.PReference;
import net.neilcsmith.praxis.core.types.PResource;
import net.neilcsmith.praxis.core.types.PString;
import net.neilcsmith.praxis.impl.AbstractAsyncProperty;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.impl.DefaultControlOutputPort;
import net.neilcsmith.praxis.impl.NumberProperty;
import net.neilcsmith.praxis.impl.StringProperty;
import net.neilcsmith.praxis.video.impl.DefaultVideoInputPort;
import net.neilcsmith.praxis.video.impl.DefaultVideoOutputPort;
import net.neilcsmith.praxis.video.pipes.impl.SingleInOut;
import net.neilcsmith.praxis.video.render.Surface;
import net.neilcsmith.praxis.video.utils.ResizeMode;

/**
 *
 * @author Neil C Smith
 */
public class Still extends AbstractComponent {

    private static final Logger LOG = Logger.getLogger(Still.class.getName());
    private Delegator delegator;
    private ImageDelegate delegate;
    private ResizeMode resizeMode;
    private StringProperty resizeType;
    private NumberProperty alignX;
    private NumberProperty alignY;
    private ControlPort.Output rdyPort;
    private ControlPort.Output errPort;

    public Still() {
        delegator = new Delegator();
        resizeMode = new ResizeMode(ResizeMode.Type.Stretch, 0.5, 0.5);
        resizeType = createTypeControl();
        alignX = NumberProperty.create(new AlignXBinding(), 0, 1, 0.5);
        alignY = NumberProperty.create(new AlignYBinding(), 0, 1, 0.5);
        DelegateLoader loader = new DelegateLoader();
        registerPort(Port.IN, new DefaultVideoInputPort(this, delegator));
        registerPort(Port.OUT, new DefaultVideoOutputPort(this, delegator));
        registerControl("image", loader);
        registerPort("image", loader.createPort());
        registerControl("resize-mode", resizeType);
        registerPort("resize-mode", resizeType.createPort());
        registerControl("align-x", alignX);
        registerPort("align-x", alignX.createPort());
        registerControl("align-y", alignY);
        registerPort("align-y", alignY.createPort());
        rdyPort = new DefaultControlOutputPort();
        registerPort("ready", rdyPort);
        errPort = new DefaultControlOutputPort();
        registerPort("error", errPort);

    }

    private void setDelegate(ImageDelegate delegate) {
        this.delegate = delegate;
        if (delegate != null && !delegate.getResizeMode().equals(resizeMode)) {
            delegate.setResizeMode(resizeMode);
        }
        delegator.setDelegate(delegate);
    }

    private StringProperty createTypeControl() {
        ResizeMode.Type[] types = ResizeMode.Type.values();
        String[] allowed = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            allowed[i] = types[i].name();
        }
        return StringProperty.create(new ResizeTypeBinding(), allowed,
                resizeMode.getType().name());
    }

    private class ResizeTypeBinding implements StringProperty.Binding {

        public void setBoundValue(long time, String value) {
            ResizeMode.Type type = ResizeMode.Type.valueOf(value);
            if (resizeMode.getType() != type) {
                resizeMode = new ResizeMode(type, resizeMode.getHorizontalAlignment(),
                        resizeMode.getVerticalAlignment());
                if (delegate != null) {
                    delegate.setResizeMode(resizeMode);
                }
            }
        }

        public String getBoundValue() {
            return resizeMode.getType().name();
        }
    }

    private class AlignXBinding implements NumberProperty.Binding {

        public void setBoundValue(long time, double value) {
            if (value != resizeMode.getHorizontalAlignment()) {
                resizeMode = new ResizeMode(resizeMode.getType(), value,
                        resizeMode.getVerticalAlignment());
                if (delegate != null) {
                    delegate.setResizeMode(resizeMode);
                }
            }
        }

        public double getBoundValue() {
            return resizeMode.getHorizontalAlignment();
        }
    }

    private class AlignYBinding implements NumberProperty.Binding {

        public void setBoundValue(long time, double value) {
            if (value != resizeMode.getVerticalAlignment()) {
                resizeMode = new ResizeMode(resizeMode.getType(),
                        resizeMode.getHorizontalAlignment(), value);
                if (delegate != null) {
                    delegate.setResizeMode(resizeMode);
                }
            }
        }

        public double getBoundValue() {
            return resizeMode.getVerticalAlignment();
        }
    }

    private class DelegateLoader extends AbstractAsyncProperty<ImageDelegate> {

        DelegateLoader() {
            super(PResource.info(true),
                    ImageDelegate.class,
                    PString.EMPTY);
        }

        @Override
        protected TaskService.Task createTask(CallArguments keys) throws Exception {
            Argument key = keys.get(0);
            if (key.isEmpty()) {
                return null;
            } else {
                return new LoaderTask(PResource.coerce(key),
                        resizeMode, null);//delegator.getCurrentDimensions());
            }
        }

        @Override
        protected void valueChanged(long time) {
            setDelegate(getValue());
            LOG.finest("New image loaded - sending ready message from port.");
            rdyPort.send(time);
        }

        @Override
        protected void taskError(long time) {
            LOG.finest("Error in image loading - sending error message from port.");
            errPort.send(time);
        }
    }

    private class LoaderTask implements TaskService.Task {

        PResource uri;
        ResizeMode mode;
        Dimension guide;

        LoaderTask(PResource uri, ResizeMode mode, Dimension guide) {
            this.uri = uri;
            this.mode = mode;
            this.guide = guide;
        }

        public Argument execute() throws Exception {
            URI loc = uri.value();
            ImageDelegate del = ImageDelegate.create(loc, mode, guide);
            return PReference.wrap(del);
        }
    }

    private class Delegator extends SingleInOut {

        private ImageDelegate delegate;

        @Override
        protected void process(Surface surface, boolean rendering) {
            if (rendering && delegate != null) {
                delegate.process(surface);
            }
        }

        private void setDelegate(ImageDelegate delegate) {
            this.delegate = delegate;
        }

    }
}
