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
package net.neilcsmith.praxis.video.gstreamer.delegates;

import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gstreamer.Bus;
import org.gstreamer.Element;
import org.gstreamer.Format;
import org.gstreamer.Gst;
import org.gstreamer.GstObject;
import org.gstreamer.Pipeline;
import org.gstreamer.SeekFlags;
import org.gstreamer.SeekType;
import org.gstreamer.elements.PlayBin2;

/**
 *
 * @author Neil C Smith
 */
public class PlayBinDelegate extends AbstractGstDelegate {

    private URI loc;
    private PlayBin2 pipe;

    protected PlayBinDelegate(URI loc) {
        this.loc = loc;
    }

    @Override
    protected Pipeline buildPipeline(Element sink) throws Exception {
        pipe = new PlayBin2("PlayBin2", loc);
        pipe.setAudioSink(null);
        pipe.setVideoSink(sink);
        pipe.getBus().connect(new Bus.SEGMENT_DONE() {

            @Override
            public void segmentDone(GstObject arg0, Format arg1, long arg2) {
                State s = getState();
                if (s == State.Playing) {
                    if (isLooping()) {
                        pipe.seek(1.0, Format.TIME, SeekFlags.FLUSH | SeekFlags.SEGMENT,
                                SeekType.SET, 0, SeekType.SET, -1);
                    } else {
                        try {
                            stop();
                        } catch (StateException ex) {
                        }
                    }
                }
            }
        });
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
    protected void doPlay() {

        long position = pipe.queryPosition(TimeUnit.NANOSECONDS);
        pipe.seek(1.0, Format.TIME, SeekFlags.FLUSH | SeekFlags.SEGMENT,
                SeekType.SET, position, SeekType.SET, -1);
        pipe.play();

    }

    @Override
    protected void doPause() {

        if (!pipe.isPlaying()) {
            pipe.play();
            pipe.getState();
        }
        long position = pipe.queryPosition(TimeUnit.NANOSECONDS);
        if (position < 0) {
            position = 100;
        }
        pipe.seek(1.0, Format.TIME, SeekFlags.FLUSH | SeekFlags.SEGMENT,
                SeekType.SET, position, SeekType.SET, position);

    }

    @Override
    public void setPosition(final long position) {
        Gst.getExecutor().execute(new Runnable() {

            public void run() {
                State s = getState();
                if (s == State.Playing) {
                    pipe.seek(1, Format.TIME, SeekFlags.FLUSH | SeekFlags.SEGMENT,
                            SeekType.SET, position, SeekType.SET, -1);
                } else {
                    pipe.seek(1, Format.TIME, SeekFlags.FLUSH | SeekFlags.SEGMENT,
                            SeekType.SET, position, SeekType.SET, position + 1);
                }

            }
        });
    }

    public static PlayBinDelegate create(URI loc) {
        return new PlayBinDelegate(loc);
    }
}
