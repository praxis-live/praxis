/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Neil C Smith.
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
package net.neilcsmith.praxis.video.pgl;

import net.neilcsmith.praxis.video.ClientConfiguration;
import net.neilcsmith.praxis.video.Player;
import net.neilcsmith.praxis.video.PlayerConfiguration;
import net.neilcsmith.praxis.video.PlayerFactory;
import net.neilcsmith.praxis.video.QueueContext;
import net.neilcsmith.praxis.video.WindowHints;

/**
 *
 * @author nsigma
 */
public class PGLPlayerFactoryProvider implements PlayerFactory.Provider {
    
    private final static String LIBRARY_NAME = "OpenGL";
    private final static PlayerFactory factory = new Factory(); 

    @Override
    public PlayerFactory getFactory() {
        return factory;
    }

    @Override
    public String getLibraryName() {
        return LIBRARY_NAME;
    }
    
    private static class Factory implements PlayerFactory {

        @Override
        public Player createPlayer(PlayerConfiguration config, ClientConfiguration[] clients)
                throws Exception {
            if (clients.length != 1 || clients[0].getSourceCount() != 0 || clients[0].getSinkCount() != 1) {
                throw new IllegalArgumentException("Invalid client configuration");
            }

            int width = config.getWidth();
            int height = config.getHeight();
            int outWidth = width;
            int outHeight = height;
            int rotation = 0;
            int device = -1;
            
            ClientConfiguration.Dimension dim
                    = clients[0].getLookup().get(ClientConfiguration.Dimension.class);
            if (dim != null) {
                outWidth = dim.getWidth();
                outHeight = dim.getHeight();
            }

            ClientConfiguration.Rotation rot
                    = clients[0].getLookup().get(ClientConfiguration.Rotation.class);
            if (rot != null) {
                rotation = rot.getAngle();
            }
            switch (rotation) {
                case 0:
                case 90:
                case 180:
                case 270:
                    break;
                default:
                    rotation = 0;
            }

            ClientConfiguration.DeviceIndex dev
                    = clients[0].getLookup().get(ClientConfiguration.DeviceIndex.class);
            if (dev != null) {
                device = dev.getValue();
            }

            WindowHints wHints = clients[0].getLookup().get(WindowHints.class);
            if (wHints == null) {
                wHints = new WindowHints();
            }
            
            QueueContext queue = config.getLookup().get(QueueContext.class);

            return new PGLPlayer(
                    config.getWidth(),
                    config.getHeight(),
                    config.getFPS(),
                    outWidth,
                    outHeight,
                    rotation,
                    device,
                    wHints,
                    queue);

        }
    }
}
