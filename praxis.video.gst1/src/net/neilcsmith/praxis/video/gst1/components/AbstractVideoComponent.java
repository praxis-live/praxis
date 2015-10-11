/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2015 Neil C Smith.
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
package net.neilcsmith.praxis.video.gst1.components;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ExecutionContext;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.core.types.PNumber;
import net.neilcsmith.praxis.core.types.PString;
import net.neilcsmith.praxis.impl.AbstractExecutionContextComponent;
import net.neilcsmith.praxis.impl.ArgumentProperty;
import net.neilcsmith.praxis.impl.NumberProperty;
import net.neilcsmith.praxis.impl.StringProperty;
import net.neilcsmith.praxis.impl.TriggerControl;
import net.neilcsmith.praxis.video.gst1.components.VideoDelegate.StateException;
import net.neilcsmith.praxis.video.impl.DefaultVideoOutputPort;
import net.neilcsmith.praxis.video.pipes.impl.SingleOut;
import net.neilcsmith.praxis.video.render.Surface;
import net.neilcsmith.praxis.video.utils.ResizeMode;

/**
 *
 * @author Neil C Smith
 */
class AbstractVideoComponent extends AbstractExecutionContextComponent {

    enum TriggerState {

        Play, Pause, Stop
    }

    boolean rootActive;
    VideoDelegate video;
    private Delegator container;
    private int srcWidth;
    private int srcHeight;
    private double srcFrameRate;
    private ResizeMode resizeMode;

    protected AbstractVideoComponent() {

        container = new Delegator();
        resizeMode = new ResizeMode(ResizeMode.Type.Stretch, 0.5, 0.5);

        registerPort(Port.OUT, new DefaultVideoOutputPort(container));

    }

    @Override
    public void stateChanged(ExecutionContext source) {
        switch (source.getState()) {
            case ACTIVE:
                rootActive = true;
                break;
            case IDLE:
                rootActive = false;
                if (video != null) {
                    try {
                        video.stop();
                    } catch (StateException ex) {
                        // no op
                    }
                }
                break;
            case TERMINATED:
                rootActive = false;
                if (video != null) {
                    video.dispose();
                    video = null;
                }
                break;
        }
    }

    void createResizeModeControls() {
        StringProperty resizeType = createResizeTypeControl();
        NumberProperty alignX = NumberProperty.builder()
                .minimum(0)
                .maximum(1)
                .defaultValue(0.5)
                .binding(new ResizeAlignBinding(false))
                .build();
        NumberProperty alignY = NumberProperty.builder()
                .minimum(0)
                .maximum(1)
                .defaultValue(0.5)
                .binding(new ResizeAlignBinding(true))
                .build();
        registerControl("resize-mode", resizeType);
        registerControl("align-x", alignX);
        registerControl("align-Y", alignY);
    }

    private StringProperty createResizeTypeControl() {
        ResizeMode.Type[] types = ResizeMode.Type.values();
        String[] allowed = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            allowed[i] = types[i].name();
        }
        return StringProperty.builder()
                .allowedValues(allowed)
                .binding(new ResizeTypeBinding())
                .defaultValue(resizeMode.getType().name())
                .build();
    }

    void createSourceCapsControls() {
        // Source Caps controls
        ArgumentProperty srcW = ArgumentProperty.builder()
                .binding(new DimBinding(false))
                .emptyIsDefault()
                .build();
        ArgumentProperty srcH = ArgumentProperty.builder()
                .binding(new DimBinding(true))
                .emptyIsDefault()
                .build();
        ArgumentProperty srcR = ArgumentProperty.builder()
                .binding(new RateBinding())
                .emptyIsDefault()
                .build();
        registerControl("source-width", srcW);
        registerControl("source-height", srcH);
        registerControl("source-fps", srcR);
    }

    void setDelegate(VideoDelegate delegate) {
        if (video != null) {
            video.dispose();
        }
        video = delegate;
        if (video != null) {
            video.setResizeMode(resizeMode);
        }
    }

    TriggerBinding createTriggerBinding(TriggerState state) {
        return new TriggerBinding(state);
    }

    private void prepareVideo() {
        if (video.getState() != VideoDelegate.State.Ready) {
            return;
        }
        if (video.supportsFrameSizeRequest()) {
            if (srcWidth > 0) {
                video.requestFrameWidth(srcWidth);
            } else {
                video.defaultFrameWidth();
            }
            if (srcHeight > 0) {
                video.requestFrameHeight(srcHeight);
            } else {
                video.defaultFrameHeight();
            }
        }
        if (video.supportsFrameRateRequest()) {
            if (srcFrameRate >= 1) {
                video.requestFrameRate(srcFrameRate);
            } else {
                video.defaultFrameRate();
            }
        }
    }

    private class TriggerBinding implements TriggerControl.Binding {

        private final TriggerState state;

        private TriggerBinding(TriggerState state) {
            this.state = state;
        }

        public void trigger(long time) {
            if (video != null) {
                try {
                    switch (state) {
                        case Play:
                            prepareVideo();
                            video.play();
                            break;
                        case Pause:
                            prepareVideo();
                            video.pause();
                            break;
                        case Stop:
                            video.stop();
                            break;
                    }
                } catch (StateException ex) {
                    Logger.getLogger(VideoPlayer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private class ResizeTypeBinding implements StringProperty.Binding {

        @Override
        public void setBoundValue(long time, String value) {
            ResizeMode.Type type = ResizeMode.Type.valueOf(value);
            if (resizeMode.getType() != type) {
                resizeMode = new ResizeMode(type,
                        resizeMode.getHorizontalAlignment(),
                        resizeMode.getVerticalAlignment());
                if (video != null) {
                    video.setResizeMode(resizeMode);
                }
            }
        }

        @Override
        public String getBoundValue() {
            return resizeMode.getType().name();
        }
    }

    private class ResizeAlignBinding implements NumberProperty.Binding {

        private final boolean Y;

        private ResizeAlignBinding(boolean Y) {
            this.Y = Y;
        }

        @Override
        public void setBoundValue(long time, double value) {
            ResizeMode cur = resizeMode;
            if (Y) {
                if (value != resizeMode.getVerticalAlignment()) {
                    resizeMode = new ResizeMode(resizeMode.getType(),
                            resizeMode.getHorizontalAlignment(),
                            value);
                }
            } else {
                if (value != resizeMode.getHorizontalAlignment()) {
                    resizeMode = new ResizeMode(resizeMode.getType(),
                            value,
                            resizeMode.getVerticalAlignment());
                }
            }
            if (cur != resizeMode && video != null) {
                video.setResizeMode(resizeMode);
            }
        }

        @Override
        public double getBoundValue() {
            return Y ? resizeMode.getVerticalAlignment()
                    : resizeMode.getHorizontalAlignment();
        }

    }

    private class DimBinding implements ArgumentProperty.Binding {

        private final boolean height;

        private DimBinding(boolean height) {
            this.height = height;
        }

        @Override
        public void setBoundValue(long time, Argument value) throws Exception {
            int val;
            if (value.isEmpty()) {
                val = 0;
            } else {
                val = PNumber.coerce(value).toIntValue();
                if (val < 1 || val > 4096) {
                    throw new IllegalArgumentException("Out of range");
                }
            }
            if (height) {
                srcHeight = val;
            } else {
                srcWidth = val;
            }
        }

        @Override
        public Argument getBoundValue() {
            int val = height ? srcHeight : srcWidth;
            if (val <= 0) {
                return PString.EMPTY;
            } else {
                return PNumber.valueOf(val);
            }
        }

    }

    private class RateBinding implements ArgumentProperty.Binding {

        @Override
        public void setBoundValue(long time, Argument value) throws Exception {
            if (value.isEmpty()) {
                srcFrameRate = 0;
            } else {
                double val = PNumber.coerce(value).value();
                if (val < 1) {
                    throw new IllegalArgumentException();
                }
                srcFrameRate = val;
            }
        }

        @Override
        public Argument getBoundValue() {
            if (srcFrameRate < 0.5) {
                return PString.EMPTY;
            } else {
                return PNumber.valueOf(srcFrameRate);
            }
        }

    }

    private class Delegator extends SingleOut {

        @Override
        protected void process(Surface surface, boolean rendering) {
            if (rendering) {
                surface.clear();
                if (video != null) {
                    video.process(surface);
                }
            }

        }
    }

}
