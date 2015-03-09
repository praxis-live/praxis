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
import java.util.Map;
import org.gstreamer.Caps;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.Pipeline;

/**
 *
 * @author Neil C Smith
 */
public class V4LDelegate extends AbstractGstDelegate {

    private final static String V4L2SRC = "v4l2src";
    private final static String DEFAULT_CAPS = "video/x-raw-rgb";
    private String capsString;
    private String sourceType;
    private String srcDevice;

    private V4LDelegate(String srcType, String srcDevice, String caps) {
        this.sourceType = srcType;
        this.capsString = caps;
        this.srcDevice = srcDevice;
    }

    @Override
    protected Pipeline buildPipeline(Element sink) {
        Pipeline pipe = new Pipeline();
        Element src = ElementFactory.make(sourceType, "source");
        src.set("device", srcDevice);
        Element caps = ElementFactory.make("capsfilter", "caps");
        Element fcs = ElementFactory.make("ffmpegcolorspace", "fcs");
        caps.setCaps(Caps.fromString(capsString));
        pipe.addMany(src, fcs, caps, sink);
        Pipeline.linkMany(src, fcs, caps, sink);
        return pipe;
    }

    public static V4LDelegate create(URI resource) {
        if (!resource.isAbsolute()) {
            throw new IllegalArgumentException();
        }
        String srcType = getSrcType(resource);
        String srcDevice = getSrcDevice(resource);
        String caps = getCapsString(resource);
        return new V4LDelegate(srcType, srcDevice, caps);
    }

    private static String getSrcType(URI resource) {
        String scheme = resource.getScheme();
        String srcType;
        if (scheme.equals("v4l2")) {
            srcType = V4L2SRC;
        } else {
            throw new IllegalArgumentException();
        }
        return srcType;
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

    private static String getSrcDevice(URI resource) {
        String auth = resource.getAuthority();
        String srcDevice = "/dev/video0";
        if (auth != null && !auth.isEmpty()) {
            try {
                int i = Integer.parseInt(auth);
                if (i >= 0) {
                    srcDevice = "/dev/video" + i;
                }
            } catch (Exception ex) {
            }
        }
        return srcDevice;
    }
}
