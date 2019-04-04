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
package org.praxislive.video.gstreamer.components;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.freedesktop.gstreamer.Bus;
import org.freedesktop.gstreamer.ElementFactory;
import org.freedesktop.gstreamer.Format;
import org.freedesktop.gstreamer.Gst;
import org.freedesktop.gstreamer.GstObject;
import org.freedesktop.gstreamer.event.SeekFlags;
import org.freedesktop.gstreamer.event.SeekType;
import org.freedesktop.gstreamer.elements.PlayBin;
import org.praxislive.code.CodeConnector;
import org.praxislive.code.CodeContext;
import org.praxislive.code.ReferenceDescriptor;
import org.praxislive.core.Lookup;
import org.praxislive.core.types.PResource;
import org.praxislive.logging.LogLevel;
import org.praxislive.video.code.userapi.PImage;
import org.praxislive.video.gstreamer.VideoPlayer;

/**
 *
 * @author Neil C Smith - http://www.neilcsmith.net
 */
class GStreamerVideoPlayer implements VideoPlayer {
    
    private volatile State state;

    private final PlayBin playbin;
    private final PImageSink sink;
    private final CodeContext.ClockListener clockListener;
    private final Queue<Runnable> messages;

    private CodeContext<?> context;

    private Runnable onReady;
    private Consumer<String> onError;
    private Runnable onEOS;

    private volatile Optional<PResource> location;
    private volatile String audioSink;
    private volatile boolean looping;
    private volatile double rate;

    private GStreamerVideoPlayer() {
        state = State.Empty;
        playbin = new PlayBin("playbin");
        sink = new PImageSink();
        playbin.setVideoSink(sink.getElement());
        audioSink = "";
        looping = false;
        rate = 1;
        location = Optional.empty();
        clockListener = this::processMessages;
        messages = new ConcurrentLinkedQueue<>();

        Bus bus = playbin.getBus();
        bus.connect((Bus.ERROR) this::handleError);
        bus.connect((Bus.EOS) this::handleEOS);
        configureAudioSink();
    }

    @Override
    public VideoPlayer location(Optional<PResource> location) {
        if (!this.location.equals(Objects.requireNonNull(location))) {
            this.location = location;
            Lookup lkp = context.getLookup();
            async(() -> {
                URI loc = resolve(location, lkp);
                if (loc != null) {
                    playbin.stop();
                    sink.dispose();
                    playbin.setURI(loc);
                    playbin.setState(org.freedesktop.gstreamer.State.READY);
                    if (playbin.getState() == org.freedesktop.gstreamer.State.READY) {
                        state = State.Ready;
                        messages.add(this::messageOnReady);
                    } else {
                        state = State.Error;
                    }
                } else {
                    playbin.stop();
                    state = State.Empty;
                }
            });
        }
        return this;
    }

    public Optional<PResource> location() {
        return location;
    }

    @Override
    public VideoPlayer play() {
        async(() -> {
            if (state != State.Playing && location.isPresent()) {
                state = State.Playing;
                playbin.play();
                double r = rate;
                if (r != 1) {
                    playbin.getState(); // make sure we're playing
                    handleSeek(false, -1);
                }
            }
        });
        return this;
    }

    @Override
    public VideoPlayer pause() {
        async(() -> {
            if (location.isPresent()) {
                state = State.Paused;
                playbin.pause();
            }
        });
        return this;
    }

    @Override
    public VideoPlayer stop() {
        async(() -> {
            if (location.isPresent()) {
                state = State.Empty;
                playbin.stop();
                sink.dispose();
            }
        });
        return this;
    }

    @Override
    public State state() {
        return state;
    }

    @Override
    public VideoPlayer position(double position) {
        async(() -> {
            if (state == State.Playing || state == State.Paused) {
                long dur = playbin.queryDuration(TimeUnit.NANOSECONDS);
                if (dur > 0) {
                    long pos = (long) (position * dur);
                    handleSeek(false, pos);
                }
            }

        });
        return this;
    }

    @Override
    public double position() {
        if (state == State.Playing || state == State.Paused) {
            long pos = playbin.queryPosition(TimeUnit.NANOSECONDS);
            long dur = playbin.queryDuration(TimeUnit.NANOSECONDS);
            double value = dur > 0 ? (double) pos / dur : 0;
            return value < 0 ? 0 : value > 1 ? 0 : value;
        } else {
            return 0;
        }
    }

    @Override
    public double duration() {
        if (state == State.Playing || state == State.Paused) {
            return playbin.queryDuration(TimeUnit.NANOSECONDS) / 1000000000.;
        } else {
            return 0;
        }
    }

    @Override
    public VideoPlayer looping(boolean looping) {
        this.looping = looping;
        return this;
    }

    public boolean looping() {
        return looping;
    }

    @Override
    public VideoPlayer rate(double rate) {
        if (rate != this.rate) {
            this.rate = rate;
            async(() -> handleSeek(false, -1));
        }
        return this;
    }

    public double rate() {
        return rate;
    }

    @Override
    public boolean render(Consumer<PImage> renderer) {
        if (state == State.Playing || state == State.Paused) {
            return sink.render(renderer);
        } else {
            return false;
        }
    }

    @Override
    public void audioSink(String audioSink) {
        if (!this.audioSink.equals(Objects.requireNonNull(audioSink))) {
            this.audioSink = audioSink;
            async(this::configureAudioSink);
        }
    }

    public String audioSink() {
        return audioSink;
    }

    @Override
    public VideoPlayer onReady(Runnable ready) {
        this.onReady = ready;
        return this;
    }

    @Override
    public VideoPlayer onError(Consumer<String> error) {
        this.onError = error;
        return this;
    }

    @Override
    public VideoPlayer onEOS(Runnable eos) {
        this.onEOS = eos;
        return this;
    }

    private void handleError(GstObject source, int code, String message) {
        async(() -> {
            state = State.Error;
            playbin.stop();
            messages.add(() -> messageOnError(message));
        });
    }

    private void handleEOS(GstObject source) {
        if (looping) {
            async(() -> handleSeek(true, -1));
        } else {
            stop();
        }
        messages.add(this::messageOnEOS);
    }

    private void handleSeek(boolean eos, long position) {
        State s = state;
        if (s == State.Playing || s == State.Paused) {
            double rate = this.rate;
            if (rate == 0.0) {
                rate = 0.0000001;
            }
            long duration = playbin.queryDuration(TimeUnit.NANOSECONDS);
            if (eos) {
                if (rate > 0) {
                    position = 0;
                } else {
                    position = duration;
                }
            } else if (position < 0) {
                position = playbin.queryPosition(TimeUnit.NANOSECONDS);
            }

            if (rate > 0) {
                playbin.seek(rate, Format.TIME, EnumSet.of(SeekFlags.FLUSH, SeekFlags.ACCURATE), SeekType.SET, position, SeekType.SET, duration);
            } else {
                playbin.seek(rate, Format.TIME, EnumSet.of(SeekFlags.FLUSH, SeekFlags.ACCURATE), SeekType.SET, 0, SeekType.SET, position);
            }
        }
        playbin.getState(10, TimeUnit.MILLISECONDS);
    }

    private URI resolve(Optional<PResource> location, Lookup lookup) {
        if (location.isPresent()) {
            PResource res = location.get();
            List<URI> uris = res.resolve(lookup);
            for (URI uri : uris) {
                if ("file".equals(uri.getScheme())) {
                    try {
                        if (new File(uri).exists()) {
                            return uri;
                        }
                    } catch (Exception ex) {
                    }
                } else {
                    return uri;
                }
            }
        }
        return null;
    }

    private void configureAudioSink() {
        String audio = audioSink.trim();
        int flags = (int) playbin.get("flags");
        if (audio.isEmpty()) {
            flags &= ~(1 << 1);
            playbin.setAudioSink(ElementFactory.make("fakesink", "fakesink"));
        } else {
            flags |= (1 << 1);
            playbin.setAudioSink(Gst.parseBinFromDescription(audio, true));
        }
        playbin.set("flags", flags);
    }

    private void attach(CodeContext<?> context) {
        if (this.context != null) {
            this.context.removeClockListener(clockListener);
        }
        this.context = context;
        this.context.addClockListener(clockListener);
    }

    private void reset(boolean full) {
        onReady = null;
        onError = null;
        onEOS = null;
        if (full) {
            stop();
            messages.clear();
        }
    }

    private void dispose() {
        async(() -> {
            playbin.stop();
            playbin.getBus().dispose();
            playbin.dispose();
        });
        messages.clear();
        if (this.context != null) {
            this.context.removeClockListener(clockListener);
            this.context = null;
        }
    }

    private void async(Runnable task) {
        Gst.getExecutor().execute(task);
    }

    private void processMessages() {
        Runnable message;
        while ((message = messages.poll()) != null) {
            message.run();
        }
    }

    private void messageOnReady() {
        if (onReady != null) {
            onReady.run();
        }
    }

    private void messageOnError(String details) {
        if (onError != null) {
            onError.accept(details);
        }
    }

    private void messageOnEOS() {
        if (onEOS != null) {
            onEOS.run();
        }
    }

    static class Descriptor extends ReferenceDescriptor {

        private final Field field;
        private GStreamerVideoPlayer player;

        private Descriptor(String id, Field field) {
            super(id);
            this.field = field;
        }

        @Override
        public void attach(CodeContext<?> context, ReferenceDescriptor previous) {
            if (previous instanceof Descriptor) {
                Descriptor prevImpl = (Descriptor) previous;
                player = prevImpl.player;
                prevImpl.player = null;
            } else if (previous != null) {
                previous.dispose();
            }

            if (player == null) {
                player = new GStreamerVideoPlayer();
            }

            player.attach(context);

            try {
                field.set(context.getDelegate(), player);
            } catch (Exception ex) {
                context.getLog().log(LogLevel.ERROR, ex);
            }

        }

        @Override
        public void reset(boolean full) {
            player.reset(full);
        }

        @Override
        public void dispose() {
            player.dispose();
        }

        static Descriptor create(CodeConnector<?> connector, Field field) {
            if (field.getType() == VideoPlayer.class) {
                field.setAccessible(true);
                return new Descriptor(field.getName(), field);
            } else {
                return null;
            }
        }

    }

}
