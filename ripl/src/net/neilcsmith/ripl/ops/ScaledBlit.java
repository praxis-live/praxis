/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 - Neil C Smith. All rights reserved.
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

package net.neilcsmith.ripl.ops;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import net.neilcsmith.ripl.PixelData;
import net.neilcsmith.ripl.SurfaceOp;
import net.neilcsmith.ripl.utils.ImageUtils;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class ScaledBlit implements SurfaceOp {

    private BlendFunction blend;
    private Bounds srcBnds;
    private Bounds dstBnds;
    private TempData tmp;

    private ScaledBlit(BlendFunction blend, Bounds srcBnds, Bounds dstBnds) {
        this.blend = blend;
        this.srcBnds = srcBnds;
        this.dstBnds = dstBnds;
    }

    public void process(PixelData output, PixelData... inputs) {
        if (inputs.length < 1) {
            return;
        }
        AlphaComposite cmp = compositeFromBlend(blend);
        if (cmp != null) {
            processDirect(cmp, output, inputs[0]);
        } else {
            processIndirect(output, inputs[0]);
        }
        
        
    }
    
    private void processDirect(AlphaComposite cmp, PixelData output, PixelData input) {
        BufferedImage out = ImageUtils.toImage(output);
        BufferedImage in = ImageUtils.toImage(input);
        Graphics2D g2d = out.createGraphics();
        g2d.setComposite(cmp);
        int sx1 = srcBnds == null ? 0 : srcBnds.getX();
        int sy1 = srcBnds == null ? 0 : srcBnds.getY();
        int sx2 = srcBnds == null ? in.getWidth() : sx1 + srcBnds.getWidth();
        int sy2 = srcBnds == null ? in.getHeight() : sy1 + srcBnds.getHeight();
        int dx1 = dstBnds == null ? 0 : dstBnds.getX();
        int dy1 = dstBnds == null ? 0 : dstBnds.getY();
        int dx2 = dstBnds == null ? out.getWidth() : dx1 + dstBnds.getWidth();
        int dy2 = dstBnds == null ? out.getHeight() : dy1 + dstBnds.getHeight();
        g2d.drawImage(in, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
    }

    private AlphaComposite compositeFromBlend(BlendFunction blend) {
        if (blend instanceof Blend) {
            Blend b = (Blend) blend;
            if (b.getType() == Blend.Type.Normal) {
                return AlphaComposite.SrcOver.derive((float) b.getExtraAlpha());
            }
        }
        return null;
    }
    
    private void processIndirect(PixelData output, PixelData input) {
        int sx1 = srcBnds == null ? 0 : srcBnds.getX();
        int sy1 = srcBnds == null ? 0 : srcBnds.getY();
        int sx2 = srcBnds == null ? input.getWidth() : sx1 + srcBnds.getWidth();
        int sy2 = srcBnds == null ? input.getHeight() : sy1 + srcBnds.getHeight();
        int dx = dstBnds == null ? 0 : dstBnds.getX();
        int dy = dstBnds == null ? 0 : dstBnds.getY();
        int dw = dstBnds == null ? output.getWidth() : dstBnds.getWidth();
        int dh = dstBnds == null ? output.getHeight() : dstBnds.getHeight();
        
        // get temp data
        if (tmp == null) {
            tmp = TempData.create(dw, dh, input.hasAlpha());
        } else if (tmp.width != dw || tmp.height != dh) {
            tmp = TempData.create(dh, dh, input.hasAlpha(), tmp.data);
        } else {
            tmp.alpha = input.hasAlpha();
        }

        // draw to temp
        BufferedImage tmpIm = ImageUtils.toImage(tmp);
        BufferedImage in = ImageUtils.toImage(input);
        Graphics2D g2d = tmpIm.createGraphics();
        g2d.setComposite(AlphaComposite.Src);
        g2d.drawImage(in, 0, 0, dw, dh, sx1, sy1, sx2, sy2, null);

        Blit.op(blend, dx, dy).process(output, tmp);

        
    }

    
    

    public static SurfaceOp op(BlendFunction blend, Bounds srcBnds, Bounds dstBnds) {
        if (blend == null) {
            throw new NullPointerException();
        }
        return new ScaledBlit(blend, srcBnds, dstBnds);
    }

}
