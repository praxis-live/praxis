/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008/09 - Neil C Smith. All rights reserved.
 * 
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details.
 * 
 * You should have received a copy of the GNU General Public License version 2
 * along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 */
package net.neilcsmith.ripl.gstreamer;

import java.net.URI;
import java.util.concurrent.TimeUnit;
import org.gstreamer.Bus;
import org.gstreamer.Format;
import org.gstreamer.Gst;
import org.gstreamer.GstObject;
import org.gstreamer.Pipeline;
import org.gstreamer.SeekFlags;
import org.gstreamer.SeekType;
import org.gstreamer.elements.PlayBin;
import org.gstreamer.elements.RGBDataSink;
import org.gstreamer.elements.RGBDataSink.Listener;

/**
 *
 * @author Neil C Smith
 */
public class PlaybinDelegate extends AbstractGstDelegate {

    private URI loc;
    private PlayBin pipe;

    protected PlaybinDelegate(URI loc) {
        this.loc = loc;
    }

    @Override
    protected Pipeline buildPipeline(Listener listener) throws Exception {
        pipe = new PlayBin("PlayBin", loc);
        pipe.setAudioSink(null);
        RGBDataSink sink = new RGBDataSink(("sink"), listener);
        sink.setPassDirectBuffer(true);
        pipe.setVideoSink(sink);
        pipe.getBus().connect(new Bus.SEGMENT_DONE() {

            public void segmentDone(GstObject arg0, Format arg1, long arg2) {
                if (isLooping()) {
                    State s = getState();
                    if (s == State.Playing) {
                        pipe.seek(1.0, Format.TIME, SeekFlags.FLUSH | SeekFlags.SEGMENT,
                                SeekType.SET, 0, SeekType.SET, -1);
                    }
                }
            }
        });
        setLooping(true);
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
    protected void doPlay() throws Exception {
        Gst.getExecutor().execute(new Runnable() {

            public void run() {
                long position = pipe.queryPosition(TimeUnit.NANOSECONDS);
                pipe.seek(1.0, Format.TIME, SeekFlags.FLUSH | SeekFlags.SEGMENT,
                        SeekType.SET, position, SeekType.SET, -1);
                pipe.play();
            }
        });
    }

    @Override
    protected void doPause() throws Exception {
        Gst.getExecutor().execute(new Runnable() {

            public void run() {
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
        });
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
                            SeekType.SET, position, SeekType.SET, position+1);
                }

            }
        });
    }
    
    public static PlaybinDelegate create(URI loc) {
        return new PlaybinDelegate(loc);
    }
}

