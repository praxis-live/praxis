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
package org.praxislive.video.components.analysis.blobs;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import org.praxislive.core.ControlPort;
import org.praxislive.core.ExecutionContext;
import org.praxislive.core.Port;
import org.praxislive.impl.AbstractClockComponent;
import org.praxislive.impl.BooleanProperty;
import org.praxislive.impl.DefaultControlOutputPort;
import org.praxislive.video.impl.VideoInputPortEx;
import org.praxislive.video.impl.VideoOutputPortEx;
import org.praxislive.video.pipes.impl.SingleInOut;
import org.praxislive.video.render.PixelData;
import org.praxislive.video.render.Surface;
import org.praxislive.video.render.SurfaceOp;
import org.praxislive.video.render.ops.GraphicsOp;
import org.praxislive.video.render.ops.RectFill;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class BlobTracker extends AbstractClockComponent {

    private ImageBlobs imageBlobs;
    private BlobPipe blobPipe;
    private BooleanProperty debug;
    private DefaultControlOutputPort x;
    private DefaultControlOutputPort y;
    private DefaultControlOutputPort width;
    private DefaultControlOutputPort height;
    private double xd;
    private double yd;
    private double widthd;
    private double heightd;

    public BlobTracker() {
        blobPipe = new BlobPipe();
        imageBlobs = new ImageBlobs();
        debug = BooleanProperty.create(false);
        registerControl("debug", debug);
        registerPort(PortEx.IN, new VideoInputPortEx(blobPipe));
        registerPort(PortEx.OUT, new VideoOutputPortEx(blobPipe));
        x = new DefaultControlOutputPort();
        registerPort("x", x);
        y = new DefaultControlOutputPort();
        registerPort("y", y);
        width = new DefaultControlOutputPort();
        registerPort("width", width);
        height = new DefaultControlOutputPort();
        registerPort("height", height);
    }

    public void tick(ExecutionContext source) {
        long time = source.getTime();
        x.send(time, xd);
        y.send(time, yd);
        width.send(time, widthd);
        height.send(time, heightd);
    }

    private class BlobPipe extends SingleInOut implements SurfaceOp {

        public void process(Surface surface, boolean rendering) {

            if (!rendering) {
                return;
            }
            surface.process(this);
            imageBlobs.dotracking();
            if (imageBlobs.trackedblobs.isEmpty()) {
                widthd = 0;
                heightd = 0;
            } else {
                ABlob b = imageBlobs.trackedblobs.get(0);
                xd = (double) b.boxcenterx / surface.getWidth();
                yd = (double) b.boxcentery / surface.getHeight();
                widthd = (double) b.dimx / surface.getWidth();
                heightd = (double) b.dimy / surface.getHeight();
            }
            if (debug.getValue()) {
                RectFill rect = new RectFill();
                for (int i = 0; i < imageBlobs.trackedblobs.size(); i++) {
                    final ABlob b = imageBlobs.trackedblobs.get(i);
                    final int idx = i;

                    rect.setColor(idx == 0 ? Color.MAGENTA : Color.CYAN)
                            .setOpacity(0.4)
                            .setBounds(b.boxminx, b.boxminy, b.boxdimx, b.boxdimy);
                    surface.process(rect);
                    surface.process(new GraphicsOp(new GraphicsOp.Callback() {

                        public void draw(Graphics2D g2d, Image[] images) {
                            g2d.drawString("" + idx, b.boxcenterx, b.boxcentery);
                        }
                    }));
                }
            }
        }

        public void process(PixelData output, PixelData... inputs) {
            imageBlobs.calc(output);
        }

    }
}
