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
package net.neilcsmith.praxis.video.components;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ArgumentFormatException;
import net.neilcsmith.praxis.core.ComponentAddress;
import net.neilcsmith.praxis.core.Lookup;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.core.types.PNumber;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.impl.ArgumentProperty;
import net.neilcsmith.praxis.impl.BooleanProperty;
import net.neilcsmith.praxis.impl.InstanceLookup;
import net.neilcsmith.praxis.impl.StringProperty;
import net.neilcsmith.praxis.video.ClientConfiguration;
import net.neilcsmith.praxis.video.ClientRegistrationException;
import net.neilcsmith.praxis.video.VideoContext;
import net.neilcsmith.praxis.video.WindowHints;
import net.neilcsmith.praxis.video.impl.DefaultVideoInputPort;
import net.neilcsmith.praxis.video.pipes.VideoPipe;
import net.neilcsmith.praxis.video.pipes.impl.Placeholder;

/**
 *
 * @author Neil C Smith
 */
public class VideoOutput extends AbstractComponent {

    private final static String DEF_TITLE_PREFIX = "PRAXIS : ";
    
    private final Placeholder placeholder;
    private final VideoContext.OutputClient client;
    private final ArgumentProperty width;
    private final ArgumentProperty height;
    private final ArgumentProperty rotation;
    private final ArgumentProperty device;
    private final WindowHints wHints;
    private VideoContext context;
    private String title = "";
    private String defaultTitle = "";

    public VideoOutput() {
        placeholder = new Placeholder();
        registerPort(Port.IN, new DefaultVideoInputPort(placeholder));
        client = new OutputClientImpl();
        wHints = new WindowHints();

        registerControl("title", StringProperty.builder()
                .binding(new TitleBinding())
                .emptyIsDefault()
                .build());
        
        device = ArgumentProperty.builder()
                .emptyIsDefault()
                .suggestedValues(
                        PNumber.valueOf(1),
                        PNumber.valueOf(2),
                        PNumber.valueOf(3),
                        PNumber.valueOf(4))
                .build();
        width = ArgumentProperty.builder().emptyIsDefault().build();
        height = ArgumentProperty.builder().emptyIsDefault().build();
        rotation = ArgumentProperty.builder()
                .emptyIsDefault()
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
        
        registerControl("full-screen",
                BooleanProperty.create(new FullScreenBinding(), false));
        registerControl("always-on-top",
                BooleanProperty.create(new AlwaysOnTopBinding(), false));
        registerControl("undecorated",
                BooleanProperty.create(new UndecoratedBinding(), false));
        registerControl("show-cursor",
                BooleanProperty.create(new ShowCursorBinding(), false));
        
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
        ComponentAddress ad = getAddress();
        if (ad != null) {
            defaultTitle = DEF_TITLE_PREFIX + "/" + ad.getRootID();
        } else {
            defaultTitle = DEF_TITLE_PREFIX;
        }
        if (title.isEmpty()) {
            wHints.setTitle(defaultTitle);
        }
    }

    private class TitleBinding implements StringProperty.Binding {

        public void setBoundValue(long time, String value) {
            title = value;
            if (title.isEmpty()) {
                wHints.setTitle(defaultTitle);
            } else {
                wHints.setTitle(title);
            }
        }

        public String getBoundValue() {
            return title;
        }

    }

    private class FullScreenBinding implements BooleanProperty.Binding {

        public void setBoundValue(long time, boolean value) {
            wHints.setFullScreen(value);
        }

        public boolean getBoundValue() {
            return wHints.isFullScreen();
        }

    }

    private class AlwaysOnTopBinding implements BooleanProperty.Binding {

        public void setBoundValue(long time, boolean value) {
            wHints.setAlwaysOnTop(value);
        }

        public boolean getBoundValue() {
            return wHints.isAlwaysOnTop();
        }

    }

    private class UndecoratedBinding implements BooleanProperty.Binding {

        public void setBoundValue(long time, boolean value) {
            wHints.setUndecorated(value);
        }

        public boolean getBoundValue() {
            return wHints.isUndecorated();
        }

    }
    
    private class ShowCursorBinding implements BooleanProperty.Binding {

        @Override
        public void setBoundValue(long time, boolean value) {
            wHints.setShowCursor(value);
        }

        @Override
        public boolean getBoundValue() {
            return wHints.isShowCursor();
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
            items.add(wHints);
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
