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
package net.neilcsmith.ripl.gstreamer;

import java.net.URI;
import org.gstreamer.Caps;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.Pad;
import org.gstreamer.Pipeline;
import org.gstreamer.Structure;
import org.gstreamer.elements.DecodeBin;
import org.gstreamer.elements.RGBDataSink;
import org.gstreamer.elements.RGBDataSink.Listener;

/**
 *
 * @author Neil C Smith
 */
public class DV1394Delegate extends AbstractGstDelegate {

    private DV1394Delegate() {

    }

    @Override
    protected Pipeline buildPipeline(Listener listener) {
        Pipeline pipe = new Pipeline();
        Element src = ElementFactory.make("dv1394src", "source");
        DecodeBin decoder = new DecodeBin("decoder");
        final Element color = ElementFactory.make("ffmpegcolorspace", "color");
        final Element deint = ElementFactory.make("deinterlace", "deint");
        final RGBDataSink sink = new RGBDataSink("sink", listener);
        sink.getSinkElement().set("sync", "false");
        sink.setPassDirectBuffer(true);
        pipe.addMany(src, decoder, color, deint, sink);
//        pipe.addMany(src, decoder, color, sink);
        Pipeline.linkMany(src, decoder);
        Pipeline.linkMany(color, sink);
//        Pipeline.linkMany(color, sink);
        
        decoder.connect(new DecodeBin.NEW_DECODED_PAD() {

            public void newDecodedPad(Element elem, Pad pad, boolean last) {
                /* only link once */
                if (pad.isLinked()) {
                    return;
                }
                /* check media type */
                Caps caps = pad.getCaps();
                Structure struct = caps.getStructure(0);
                if (struct.getName().startsWith("video/")) {
                    pad.link(color.getStaticPad("sink"));
                } //else {
            //  System.out.println("Unknown pad [" + struct.getName() + "]");
            //}
            }
        });

        return pipe;
    }

    public static DV1394Delegate create(URI resource) {
        return new DV1394Delegate();
    }
}
