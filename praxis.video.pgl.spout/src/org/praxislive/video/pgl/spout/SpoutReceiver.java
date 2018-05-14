/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2018 Neil C Smith.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * version 3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License version 3
 * along with this work; if not, see http://www.gnu.org/licenses/
 * 
 *
 * Please visit https://www.praxislive.org if you need additional information or
 * have any questions.
 */
package org.praxislive.video.pgl.spout;

import java.util.Optional;
import org.praxislive.video.pgl.PGLContext;
import org.praxislive.video.pgl.PGLTextureSharer;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;
import spout.Spout;

/**
 *
 * @author neilcsmith
 */
class SpoutReceiver implements PGLTextureSharer.Receiver {

    private final Spout client;

    private PImage cache;

    SpoutReceiver(PGLContext context, String serverID) {
        client = new Spout(context.parent());
        cache = context.parent().createImage(context.parent().width, context.parent().height, PConstants.ARGB);
        if (serverID != null && !serverID.isEmpty()) {
            client.createReceiver(serverID);
        }
    }

    @Override
    public boolean hasNewFrame() {
        return true;
    }

    @Override
    public Optional<PImage> acquireFrame() {
        cache = client.receiveTexture(cache);
        return Optional.ofNullable((PImage)cache);
    }

    @Override
    public void dispose() {
//        client.closeReceiver();
        client.dispose();
        if (cache != null) {
//            cache.dispose();
            cache = null;
        }
    }

}
