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
package net.neilcsmith.praxis.video.gstreamer.delegates;

import java.net.URI;
import java.util.Map;
import org.gstreamer.Caps;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.Pipeline;
import org.gstreamer.elements.RGBDataSink;
import org.gstreamer.elements.RGBDataSink.Listener;

/**
 *
 * @author Neil C Smith
 */
public class KSDelegate extends AbstractGstDelegate {

    private final static String SRC_TYPE = "ksvideosrc";
    private final static String DEFAULT_CAPS = "video/x-raw-rgb";
    private String capsString;
    private int deviceIdx;

    private KSDelegate(int deviceIdx, String caps) {
        this.deviceIdx = deviceIdx;
        this.capsString = caps;
    }
    

    @Override
    protected Pipeline buildPipeline(Element sink) {
        Pipeline pipe = new Pipeline();
        Element src = ElementFactory.make(SRC_TYPE, "source");
        src.set("device-index", deviceIdx);
        Element caps = ElementFactory.make("capsfilter", "caps");
        Element fcs = ElementFactory.make("ffmpegcolorspace", "fcs");
        caps.setCaps(Caps.fromString(capsString));
//        RGBDataSink sink = new RGBDataSink("sink", listener);
//        sink.setPassDirectBuffer(true);
        pipe.addMany(src, fcs, caps, sink);
        Pipeline.linkMany(src, fcs, caps, sink);
        return pipe;
    }

    public static KSDelegate create(URI resource) {
        if (!resource.isAbsolute()) {
            throw new IllegalArgumentException();
        }
        if (!"ks".equals(resource.getScheme())) {
            throw new IllegalArgumentException();
        }
        int idx = getSrcIndex(resource);
        String caps = getCapsString(resource);
        return new KSDelegate(idx, caps);
    }


    private static String getCapsString(URI resource) {
        String query = resource.getQuery();
        String caps = DEFAULT_CAPS;
        if (query != null && !query.isEmpty()) {
            Map<String, String> map = GstUtils.parseQueryString(query);
            if (!map.isEmpty()) {
                StringBuilder bld = new StringBuilder(caps);
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    bld.append(", ");
                    bld.append(entry.getKey());
                    bld.append("=");
                    bld.append(entry.getValue());
                }
                caps = bld.toString();
            }
        }
        return caps;
    }

    private static int getSrcIndex(URI resource) {
        String auth = resource.getAuthority();
        if (auth != null && !auth.isEmpty()) {
            try {
                int i = Integer.parseInt(auth);
                if (i >= 0) {
                    return i;
                }
            } catch (Exception ex) {
            }
        }
        return 0;
    }
}
