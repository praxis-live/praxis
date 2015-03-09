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

import org.gstreamer.Bin;
import org.gstreamer.Caps;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.Pipeline;

/**
 *
 * @author Neil C Smith
 */
public class BinDelegate extends AbstractGstDelegate {

    private final static String DEFAULT_CAPS = "video/x-raw-rgb";
    private String binDescription;

    private Bin bin;
    private Element caps;
    private int requestWidth;
    private int requestHeight;
    private int requestRate;

    private BinDelegate(String binDescription) {
        this.binDescription = binDescription;
    }

    @Override
    protected Pipeline buildPipeline(Element sink) {
        Pipeline pipe = new Pipeline();
        bin = Bin.launch(binDescription, true);
        Element videorate = ElementFactory.make("videorate", "rate");
        Element videoscale = ElementFactory.make("videoscale", "scale");
        videoscale.set("add-borders", true);
        Element colorspace = ElementFactory.make("ffmpegcolorspace", "colorspace");
        caps = ElementFactory.make("capsfilter", "caps");
        caps.setCaps(Caps.fromString(DEFAULT_CAPS));
        pipe.addMany(bin, videorate, videoscale, colorspace, caps, sink);
        Pipeline.linkMany(bin, videorate, videoscale, colorspace, caps, sink);
        return pipe;
    }

    @Override
    protected void doPlay() {
        caps.setCaps(Caps.fromString(buildCapsString()));
        super.doPlay();
    }

    @Override
    protected void doPause() {
        caps.setCaps(Caps.fromString(buildCapsString()));
        super.doPause();
    }

    @Override
    public boolean supportsFrameSizeRequest() {
        return true;
    }
    
    @Override
    public void requestFrameWidth(int width) {
        requestWidth = width;
    }
    
    @Override
    public void requestFrameHeight(int height) {
        requestHeight = height;
    }

    @Override
    public boolean supportsFrameRateRequest() {
        return true;
    }

    @Override
    public void requestFrameRate(double rate) {
        // @TODO support fractions.
        requestRate = (int) Math.round(rate);
    }
    
    private String buildCapsString() {
        StringBuilder sb = new StringBuilder(DEFAULT_CAPS);
        if (requestWidth > 0) {
            sb.append(",width=");
            sb.append(requestWidth);
        }
        if (requestHeight > 0) {
            sb.append(",height=");
            sb.append(requestHeight);
        }
        if (requestRate > 0) {
            sb.append(",framerate=");
            sb.append(requestRate);
            sb.append("/1");
        }
        return sb.toString();
    }
    
    

    public static BinDelegate create(String binDescription) {
        return new BinDelegate(binDescription);
    }

}
