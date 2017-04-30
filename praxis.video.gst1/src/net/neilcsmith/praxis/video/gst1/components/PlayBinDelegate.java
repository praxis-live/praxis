/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2017 Neil C Smith.
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

import java.net.URI;
import java.util.concurrent.TimeUnit;
import org.freedesktop.gstreamer.Bin;
import org.freedesktop.gstreamer.Element;
import org.freedesktop.gstreamer.Format;
import org.freedesktop.gstreamer.Gst;
import org.freedesktop.gstreamer.Pipeline;
import org.freedesktop.gstreamer.SeekFlags;
import org.freedesktop.gstreamer.SeekType;
import org.freedesktop.gstreamer.elements.PlayBin;

/**
 *
 * @author Neil C Smith
 */
public class PlayBinDelegate extends AbstractGstDelegate {

    final static String DEFAULT_AUDIO_SINK = "autoaudiosink";

    private final URI loc;
    private final String audioSink;

    private PlayBin pipe;
    private volatile double rate;

    public PlayBinDelegate(URI loc, String audioSink) {
        this.loc = loc;
        this.audioSink = audioSink.trim();
        this.rate = 1;
    }

    @Override
    protected Pipeline buildPipeline(Element sink) throws Exception {
        pipe = new PlayBin("PlayBin", loc);
        pipe.setVideoSink(sink);
        if (!DEFAULT_AUDIO_SINK.equals(audioSink)) {
            if (audioSink.isEmpty()) {
                int flags = (int) pipe.get("flags");
                flags &= ~(1 << 1); // cancel out audio flag
                pipe.set("flags", flags);
            } else {
                pipe.setAudioSink(Bin.launch(audioSink, true));
            }
        }

        return pipe;
    }

    @Override
    public boolean isLoopable() {
        return true;
    }

    @Override
    public boolean isSeekable() {
        return true;
    }

    @Override
    public void setPosition(final long position) {
        Gst.getExecutor().execute(new Runnable() {

            public void run() {
                doSeek(false, position);
            }
        });
    }

    public void setRate(double rate) {
        this.rate = rate;
        Gst.getExecutor().execute(new Runnable() {

            @Override
            public void run() {
                doSeek(false, -1);
            }
        });
    }

    public double getRate() {
        return rate;
    }

    @Override
    protected void doStop() {
        super.doStop();
        this.rate = 1;
    }

    @Override
    protected void doEOS() {
        try {
            if (isLooping()) {
                doSeek(true, -1);
            } else {
                stop();
            }
        } catch (Exception ex) {
            error("", ex);
        }
    }

    private void doSeek(boolean eos, long position) {
        State s = getState();
        if (s == State.Playing || s == State.Paused) {
            double rate = this.rate;
            if (rate == 0.0) {
                rate = 0.0000001;
            }
            long duration = pipe.queryDuration(TimeUnit.NANOSECONDS);
            if (eos) {
                if (rate > 0) {
                    position = 0;
                } else {
                    position = duration;
                }
            } else if (position < 0) {
                position = pipe.queryPosition(TimeUnit.NANOSECONDS);
            }

            if (rate > 0) {
                pipe.seek(rate, Format.TIME, SeekFlags.FLUSH | SeekFlags.ACCURATE, SeekType.SET, position, SeekType.SET, duration);
            } else {
                pipe.seek(rate, Format.TIME, SeekFlags.FLUSH | SeekFlags.ACCURATE, SeekType.SET, 0, SeekType.SET, position);
            }
        }
        pipe.getState(10, TimeUnit.MILLISECONDS);
    }

}
