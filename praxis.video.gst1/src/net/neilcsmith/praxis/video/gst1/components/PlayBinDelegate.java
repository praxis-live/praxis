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

import java.net.URI;
import java.util.concurrent.TimeUnit;
import org.freedesktop.gstreamer.Bus;
import org.freedesktop.gstreamer.Element;
import org.freedesktop.gstreamer.Format;
import org.freedesktop.gstreamer.Gst;
import org.freedesktop.gstreamer.GstObject;
import org.freedesktop.gstreamer.Pipeline;
import org.freedesktop.gstreamer.SeekFlags;
import org.freedesktop.gstreamer.SeekType;
import org.freedesktop.gstreamer.elements.PlayBin;

/**
 *
 * @author Neil C Smith
 */
public class PlayBinDelegate extends AbstractGstDelegate {

    private URI loc;
    private PlayBin pipe;

    protected PlayBinDelegate(URI loc) {
        this.loc = loc;
    }

    @Override
    protected Pipeline buildPipeline(Element sink) throws Exception {
        pipe = new PlayBin("PlayBin", loc);
        pipe.setAudioSink(null);
        pipe.setVideoSink(sink);
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
                State s = getState();
                if (s == State.Playing) {
                    pipe.seek(1, Format.TIME, SeekFlags.FLUSH | SeekFlags.KEY_UNIT,
                            SeekType.SET, position, SeekType.NONE, -1);
                } else if (s == State.Paused) {
                    pipe.play();
                    pipe.seek(1, Format.TIME, SeekFlags.FLUSH | SeekFlags.KEY_UNIT,
                            SeekType.SET, position, SeekType.NONE, -1);
                    pipe.pause();
                }
            }
        });
    }

    public static PlayBinDelegate create(URI loc) {
        return new PlayBinDelegate(loc);
    }
}
