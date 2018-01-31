/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2017 Neil C Smith.
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
package org.praxislive.video.pgl;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.praxislive.core.ExecutionContext;
import org.praxislive.core.Port;
import org.praxislive.impl.AbstractExecutionContextComponent;
import org.praxislive.impl.StringProperty;
import org.praxislive.impl.TriggerControl;
import org.praxislive.video.impl.DefaultVideoOutputPort;
import org.praxislive.video.pipes.impl.SingleOut;
import org.praxislive.video.render.Surface;
import processing.core.PGraphics;

/**
 *
 * @author neilcsmith
 */
public final class PGLReceiver extends AbstractExecutionContextComponent {
    
    private final Delegator delegator;
    private final PGLTextureSharer textureSharer;

    private PGLTextureSharer.Receiver receiver;
    private String serverID = "";
    private boolean active;

    public PGLReceiver() {
        delegator = new Delegator();
        textureSharer = PGLTextureSharer.find().orElse(null);
        registerPort(Port.OUT, new DefaultVideoOutputPort(delegator));
        registerControl("server-id", StringProperty.builder().binding(new StringProperty.Binding() {
            @Override
            public void setBoundValue(long time, String value) {
                if (active) {
                    throw new UnsupportedOperationException("Can't set server ID while active");
                }
                serverID = value;
            }

            @Override
            public String getBoundValue() {
                return serverID;
            }
        }).build());
        TriggerControl start = TriggerControl.create(time -> active = true);
        TriggerControl stop = TriggerControl.create((long time) -> {
            PGLReceiver.this.handleDispose();
        });
        registerControl("start", start);
        registerControl("stop", stop);
        registerPort("start", start.createPort());
        registerPort("stop", stop.createPort());
    }

    @Override
    public void stateChanged(ExecutionContext source) {
        if (source.getState() != ExecutionContext.State.ACTIVE) {
            handleDispose();
        }
    }

    @Override
    public void hierarchyChanged() {
        handleDispose();
        super.hierarchyChanged();
    }
    
    private void handleProcess(PGLSurface surface) {
        if (active) {
            if (receiver == null) {
                initReceiver(surface.getContext());
                if (receiver == null) {
                    active = false;
                    return;
                }
            }
            receiver.acquireFrame().ifPresent(f -> {
                PGraphics g = surface.getGraphics();
                g.image(f, 0, 0, g.width, g.height);
            });
        }
    }
    
    private void initReceiver(PGLContext context) {
        if (textureSharer != null) {
            try {
                receiver = textureSharer.createReceiver(context, serverID);
            } catch (Exception ex) {
                Logger.getLogger(PGLReceiver.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void handleDispose() {
        active = false;
        if (receiver != null) {
            receiver.dispose();
            receiver = null;
        }
    }

    private class Delegator extends SingleOut {

        @Override
        protected void process(Surface surface, boolean rendering) {
            if (rendering) {
                surface.clear();
                if (surface instanceof PGLSurface) {
                    handleProcess((PGLSurface) surface);
                }
            }
        }

    }

}
