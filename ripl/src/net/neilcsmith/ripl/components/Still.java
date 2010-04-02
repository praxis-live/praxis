/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 - Neil C Smith. All rights reserved.
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
package net.neilcsmith.ripl.components;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import net.neilcsmith.ripl.core.Surface;
import net.neilcsmith.ripl.core.impl.SingleOut;

/**
 *
 * @author Neil C Smith
 */
public class Still extends SingleOut {

    private BufferedImage image;
    private BufferedImage cache;
    private boolean cacheValid;

    public void setImage(BufferedImage image) {
        this.image = image;
        cache = null;
    }

    @Override
    protected void process(Surface surface, boolean rendering) {
        if (!rendering) {
            return;
        }
        if (image == null) {
            surface.clear();
            return;
        }
        int width = surface.getWidth();
        int height = surface.getHeight();
        if (cache == null || cache.getWidth() != width ||
                cache.getHeight() != height) {
            initCache(width, height);
        }
        if (!cacheValid) {
            renderCache();
        }
        surface.getGraphics().drawImage(cache, 0, 0);
    }

    private void initCache(int width, int height) {
        if (image.getType() == BufferedImage.TYPE_INT_RGB &&
                image.getWidth() == width &&
                image.getHeight() == height) {
            cache = image;
            cacheValid = true;
        } else {
            cache = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            cacheValid = false;
        }
    }

    private void renderCache() {
        Graphics2D g2d = cache.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2d.drawImage(image, 0, 0, cache.getWidth(), cache.getHeight(), null);
        cacheValid = true;
    }
}
