/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2012 Neil C Smith.
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
package net.neilcsmith.praxis.video.components;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ArgumentFormatException;
import net.neilcsmith.praxis.core.Lookup;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.core.types.PNumber;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.impl.ArgumentProperty;
import net.neilcsmith.praxis.impl.InstanceLookup;
import net.neilcsmith.praxis.impl.IntProperty;
import net.neilcsmith.praxis.video.ClientConfiguration;
import net.neilcsmith.praxis.video.ClientRegistrationException;
import net.neilcsmith.praxis.video.VideoContext;
import net.neilcsmith.praxis.video.impl.DefaultVideoInputPort;
import net.neilcsmith.praxis.video.pipes.VideoPipe;
import net.neilcsmith.praxis.video.pipes.impl.Placeholder;

/**
 *
 * @author Neil C Smith
 */
public class VideoOutput extends AbstractComponent {

    private Placeholder placeholder;
    private VideoContext context;
    private VideoContext.OutputClient client;
    private ArgumentProperty width;
    private ArgumentProperty height;
    private ArgumentProperty rotation;
    private ArgumentProperty device;

    public VideoOutput() {
        placeholder = new Placeholder();
        registerPort(Port.IN, new DefaultVideoInputPort(placeholder));
        client = new OutputClientImpl();
        device = ArgumentProperty.builder()
                .suggestedValues(
                    PNumber.valueOf(1),
                    PNumber.valueOf(2),
                    PNumber.valueOf(3),
                    PNumber.valueOf(4))
                .build();
        width = ArgumentProperty.create();
        height = ArgumentProperty.create();
        rotation = ArgumentProperty.builder()
                .suggestedValues(
                    PNumber.valueOf(0),
                    PNumber.valueOf(90),
                    PNumber.valueOf(180),
                    PNumber.valueOf(270))
                .build();
        registerControl("device", device);
        registerControl("width", width);
        registerControl("height", height);
        registerControl("rotation", rotation);
    }

    @Override
    public void hierarchyChanged() {
        super.hierarchyChanged();
        VideoContext ctxt = getLookup().get(VideoContext.class);
        if (ctxt != context) {
            if (context != null) {
                context.unregisterVideoOutputClient(client);
                context = null;
            }
            if (ctxt == null) {
                return;
            }
            try {
                ctxt.registerVideoOutputClient(client);
                context = ctxt;
            } catch (ClientRegistrationException ex) {
                Logger.getLogger(VideoOutput.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    private class OutputClientImpl extends VideoContext.OutputClient {

        public int getOutputCount() {
            return 1;
        }

        public VideoPipe getOutputSource(int index) {
            if (index == 0) {
                return placeholder;
            } else {
                throw new IndexOutOfBoundsException();
            }
        }

        @Override
        public Lookup getLookup() {
            Integer w = getInteger(width.getValue());
            Integer h = getInteger(height.getValue());
            List<Object> items = new ArrayList<Object>();
            if (w != null && h != null) {
                items.add(new ClientConfiguration.Dimension(w, h));
            }
            Integer rot = getInteger(rotation.getValue());
            if (rot != null) {
                switch (rot) {
                    case 90:
                        items.add(ClientConfiguration.Rotation.DEG_90);
                        break;
                    case 180:
                        items.add(ClientConfiguration.Rotation.DEG_180);
                        break;
                    case 270:
                        items.add(ClientConfiguration.Rotation.DEG_270);
                        break;
                }
            }
            Integer dev = getInteger(device.getValue());
            if (dev != null) {
                items.add(new ClientConfiguration.DeviceIndex(dev - 1));
            }
            return InstanceLookup.create(items.toArray());
        }

        private Integer getInteger(Argument val) {
            if (val.isEmpty()) {
                return null;
            } else {
                try {
                    return PNumber.coerce(val).toIntValue();
                } catch (ArgumentFormatException ex) {
                    return null;
                }
            }
        }
    }
}
