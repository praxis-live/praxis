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
package net.neilcsmith.praxis.video.gstreamer.components;

import java.net.URI;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.ControlPort;
import net.neilcsmith.praxis.core.Lookup;
import net.neilcsmith.praxis.core.info.ArgumentInfo;
import net.neilcsmith.praxis.core.interfaces.TaskService;
import net.neilcsmith.praxis.core.types.PMap;
import net.neilcsmith.praxis.core.types.PReference;
import net.neilcsmith.praxis.core.types.PResource;
import net.neilcsmith.praxis.core.types.PString;
import net.neilcsmith.praxis.impl.AbstractAsyncProperty;
import net.neilcsmith.praxis.impl.BooleanProperty;
import net.neilcsmith.praxis.impl.DefaultControlOutputPort;
import net.neilcsmith.praxis.impl.NumberProperty;
import net.neilcsmith.praxis.impl.TriggerControl;
import net.neilcsmith.praxis.video.InvalidVideoResourceException;

/**
 *
 * @author Neil C Smith
 */
public class VideoPlayer extends AbstractVideoComponent {

    private final ControlPort.Output readyPort;
    private final ControlPort.Output errorPort;
    
    private DelegateLoader loader;
    private boolean loop = true;

    public VideoPlayer() {
        loader = new DelegateLoader();
        registerControl("video", loader);
        NumberProperty position = NumberProperty.builder()
                .binding(new PositionBinding())
                .minimum(0)
                .maximum(1)
                .defaultValue(0)
                .markTransient()
                .build();
        registerControl("position", position);
        registerPort("position", position.createPort());
        
        BooleanProperty lp = BooleanProperty.create(new LoopBinding(), loop);
        registerControl("loop", lp);
        
        createResizeModeControls();
        
        TriggerControl play = TriggerControl.create(createTriggerBinding(TriggerState.Play));
        registerControl("play", play);
        registerPort("play", play.createPort());
        TriggerControl pause = TriggerControl.create(createTriggerBinding(TriggerState.Pause));
        registerControl("pause", pause);
        registerPort("pause", pause.createPort());
        TriggerControl stop = TriggerControl.create(createTriggerBinding(TriggerState.Stop));
        registerControl("stop", stop);
        registerPort("stop", stop.createPort());

        readyPort = new DefaultControlOutputPort();
        registerPort("ready", readyPort);
        errorPort = new DefaultControlOutputPort();
        registerPort("error", errorPort);
        
    }

    @Override
    void setDelegate(VideoDelegate delegate) {
        super.setDelegate(delegate);
        if (delegate != null && delegate.isLoopable()) {
            delegate.setLooping(loop);
        }
    }
    
    

    private class PositionBinding implements NumberProperty.Binding {

        @Override
        public void setBoundValue(long time, double value) {
            if (video != null && video.isSeekable()) {
                long duration = video.getDuration();
                if (duration > 0) {
                    long position = (long) (value * duration);
                    video.setPosition(position);
                }
            }
        }

        @Override
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

    private class LoopBinding implements BooleanProperty.Binding {
        
        @Override
        public void setBoundValue(long time, boolean value) {
            loop = value;
            if (video != null && video.isLoopable()) {
                video.setLooping(loop);
            }
        }

        @Override
        public boolean getBoundValue() {
            return loop;
        }
        
    }
    
    class DelegateLoader extends AbstractAsyncProperty<VideoDelegate> {

        DelegateLoader() {
            super(ArgumentInfo.create(PResource.class,
                    PMap.create(ArgumentInfo.KEY_ALLOW_EMPTY, true)),
                    VideoDelegate.class, PString.EMPTY);
        }

        @Override
        protected TaskService.Task createTask(CallArguments keys) throws Exception {
            Argument key;
            if (keys.getSize() < 1 || (key = keys.get(0)).isEmpty()) {
                return null;
            } else {
                return new LoadTask(getLookup(), PResource.coerce(key));
            }
        }

        @Override
        protected void valueChanged(long time) {
            setDelegate(getValue());
            if (rootActive) {
                readyPort.send(time);
            }
        }

        @Override
        protected void taskError(long time) {
            if (rootActive) {
                errorPort.send(time);
            }
        }

    }

    private class LoadTask implements TaskService.Task {

        private final Lookup lookup;
        private final PResource videoSource;

        private LoadTask(Lookup lookup, PResource videoSource) {
            this.lookup = lookup;
            this.videoSource = videoSource;
        }

        public Argument execute() throws Exception {
            URI uri = videoSource.value();
            VideoDelegate delegate = VideoDelegateFactory.getInstance().createPlayBinDelegate(uri);
            if (delegate == null) {
                throw new InvalidVideoResourceException();
            }
            try {
                VideoDelegate.State state = delegate.initialize();
                if (state == VideoDelegate.State.Ready) {
                    return PReference.wrap(delegate);
                }
            } catch (Exception ex) {
                delegate.dispose();
                throw new InvalidVideoResourceException(ex);
            }
            delegate.dispose();
            throw new InvalidVideoResourceException();
        }

    }
}
