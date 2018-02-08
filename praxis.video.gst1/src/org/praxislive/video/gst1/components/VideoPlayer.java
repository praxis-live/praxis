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
 */
package org.praxislive.video.gst1.components;

import java.io.File;
import java.net.URI;
import java.util.List;
import org.praxislive.core.Argument;
import org.praxislive.core.CallArguments;
import org.praxislive.core.ControlPort;
import org.praxislive.core.Lookup;
import org.praxislive.core.ArgumentInfo;
import org.praxislive.core.interfaces.TaskService;
import org.praxislive.core.types.PMap;
import org.praxislive.core.types.PReference;
import org.praxislive.core.types.PResource;
import org.praxislive.core.types.PString;
import org.praxislive.impl.AbstractAsyncProperty;
import org.praxislive.impl.BooleanProperty;
import org.praxislive.impl.DefaultControlOutputPort;
import org.praxislive.impl.NumberProperty;
import org.praxislive.impl.StringProperty;
import org.praxislive.impl.TriggerControl;
import org.praxislive.video.InvalidVideoResourceException;

/**
 *
 * @author Neil C Smith
 */
public class VideoPlayer extends AbstractVideoComponent {

    private final ControlPort.Output readyPort;
    private final ControlPort.Output errorPort;
    
    private final StringProperty audioSink;
    private final DelegateLoader loader;
    private boolean loop = true;

    public VideoPlayer() {
        
        audioSink = StringProperty.builder()
                .suggestedValues(PlayBinDelegate.DEFAULT_AUDIO_SINK)
                .build();
        registerControl("audio-sink", audioSink);
        
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
        
        NumberProperty rate = NumberProperty.builder()
                .binding(new RateBinding())
                .defaultValue(1)
                .markTransient()
                .build();
        registerControl("rate", rate);
        registerPort("rate", rate.createPort());
        
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
    
    private class RateBinding implements NumberProperty.Binding {

        @Override
        public void setBoundValue(long time, double value) {
            if (video instanceof PlayBinDelegate) {
                ((PlayBinDelegate) video).setRate(value);
            }
        }

        @Override
        public double getBoundValue() {
            if (video instanceof PlayBinDelegate) {
                return ((PlayBinDelegate) video).getRate();
            } else {
                return 1;
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
                return new LoadTask(getLookup(), PResource.coerce(key), audioSink.getValue());
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
        private final String audioSink;

        private LoadTask(Lookup lookup, PResource videoSource, String audioSink) {
            this.lookup = lookup;
            this.videoSource = videoSource;
            this.audioSink = audioSink;
        }

        public Argument execute() throws Exception {
            List<URI> uris = videoSource.resolve(lookup);
            URI video = null;
            for (URI uri : uris) {
                if ("file".equals(uri.getScheme())) {
                    try {
                        File file = new File(uri);
                        if (file.exists()) {
                            video = uri;
                        }
                    } catch (Exception ex) {}
                } else {
                    video = uri;
                }
            }
            video = video == null ? videoSource.value() : video;
            VideoDelegate delegate = new PlayBinDelegate(video, audioSink);
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
