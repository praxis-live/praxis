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
package net.neilcsmith.praxis.video.gstreamer.components;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.ExecutionContext;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.core.types.PMap;
import net.neilcsmith.praxis.impl.AbstractExecutionContextComponent;
import net.neilcsmith.praxis.impl.NumberProperty;
import net.neilcsmith.praxis.impl.TriggerControl;
import net.neilcsmith.praxis.video.gstreamer.components.VideoDelegate.StateException;
import net.neilcsmith.praxis.video.impl.DefaultVideoOutputPort;
import net.neilcsmith.praxis.video.pipes.impl.SingleOut;
import net.neilcsmith.praxis.video.render.Surface;

/**
 *
 * @author Neil C Smith
 */
public class VideoPlayer extends AbstractExecutionContextComponent {

    private enum TriggerState {

        Play, Pause, Stop
    }
    private VideoDelegate video;
    private Delegator delegator;
    private VideoDelegateLoader loader;

    public VideoPlayer() {
        delegator = new Delegator();
        registerPort(Port.OUT, new DefaultVideoOutputPort(this, delegator));
        loader = new VideoDelegateLoader(this, new VideoBinding(), false);
        registerControl("video", loader);
        NumberProperty position = NumberProperty.create(new PositionBinding(), 0, 1, 0,
                PMap.create(ControlInfo.KEY_TRANSIENT, true));
        registerControl("position", position);
        registerPort("position", position.createPort());
        TriggerControl play = TriggerControl.create(new TriggerBinding(TriggerState.Play));
        registerControl("play", play);
        registerPort("play", play.createPort());
        TriggerControl pause = TriggerControl.create(new TriggerBinding(TriggerState.Pause));
        registerControl("pause", pause);
        registerPort("pause", pause.createPort());
        TriggerControl stop = TriggerControl.create(new TriggerBinding(TriggerState.Stop));
        registerControl("stop", stop);
        registerPort("stop", stop.createPort());

    }

    private class PositionBinding implements NumberProperty.Binding {

        public void setBoundValue(long time, double value) {
            if (video != null && video.isSeekable()) {
                long duration = video.getDuration();
                if (duration > 0) {
                    long position = (long) (value * duration);
                    video.setPosition(position);
                }
            }
        }

        public double getBoundValue() {
            if (video == null) {
                return 0;
            } else {
                long duration = video.getDuration();
                long position = video.getPosition();
                double value = (double) position / duration;
                if (value < 0 || value > 1) {
                    return 0;
                } else {
                    return value;
                }
            }
        }
    }

    private class TriggerBinding implements TriggerControl.Binding {

        private TriggerState state;

        private TriggerBinding(TriggerState state) {
            this.state = state;
        }

        public void trigger(long time) {
            if (video != null) {
                try {
                    switch (state) {
                        case Play:
                            video.play();
                            break;
                        case Pause:
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

    private class VideoBinding implements VideoDelegateLoader.Listener {

        public void setDelegate(VideoDelegate delegate) {
            if (video != null) {
                video.dispose();
            }
            video = delegate;
        }

        public void delegateLoaded(VideoDelegateLoader source, long time) {
            setDelegate(source.getDelegate());
        }

        public void delegateError(VideoDelegateLoader source, long time) {
        }
    }

    public void stateChanged(ExecutionContext source) {
        if (source.getState() == ExecutionContext.State.IDLE) {
            if (video != null) {
                try {
                    video.stop();
                } catch (StateException ex) {
                    // no op
                }
            }
        } else if (source.getState() == ExecutionContext.State.TERMINATED) {
            if (video != null) {
                video.dispose();
                video = null;

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
