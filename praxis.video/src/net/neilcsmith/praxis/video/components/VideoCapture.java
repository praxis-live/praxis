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
package net.neilcsmith.praxis.video.components;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.core.Root;
import net.neilcsmith.praxis.core.Root.State;
import net.neilcsmith.praxis.impl.AbstractRoot;
import net.neilcsmith.praxis.impl.AbstractRootStateComponent;
import net.neilcsmith.praxis.impl.ArgumentProperty;
import net.neilcsmith.praxis.impl.TriggerControl;
import net.neilcsmith.praxis.video.DefaultVideoOutputPort;
import net.neilcsmith.ripl.components.Delegator;
import net.neilcsmith.ripl.delegates.VideoDelegate;
import net.neilcsmith.ripl.delegates.VideoDelegate.StateException;

/**
 *
 * @author Neil C Smith
 */
public class VideoCapture extends AbstractRootStateComponent {

//    private final static String DEFAULT_DEVICE = "v4l2://0?width=640&height=480";
    private VideoDelegate video;
    private Delegator container;
    private VideoDelegateLoader loader;
    private ArgumentProperty device;

    public VideoCapture() {
        
        loader = new VideoDelegateLoader(this, new VideoBinding());
        registerControl("device", loader);
        TriggerControl start = TriggerControl.create(this, new StartBinding());
        registerControl("start", start);
        registerPort("start", start.createPort());
        TriggerControl stop = TriggerControl.create(this, new StopBinding());
        registerControl("stop", stop);
        registerPort("stop", stop.createPort());
        container = new Delegator();
        registerPort(Port.OUT, new DefaultVideoOutputPort(this, container));
    }

    public void rootStateChanged(AbstractRoot source, State state) {
        if (state == State.ACTIVE_IDLE) {
            if (video != null) {
                try {
                    video.stop();
                } catch (StateException ex) {
                // no op
                }
            }
        } else if (state == State.TERMINATING) {
            if (video != null) {
                container.setDelegate(null);
                video.dispose();
                video = null;

            }
        }
    }



    private class VideoBinding implements VideoDelegateLoader.Listener {

        public void setDelegate(VideoDelegate delegate) {
            if (video != null) {
                video.dispose();
            }
            video = delegate;
            container.setDelegate(delegate);

        }

        public void delegateLoaded(VideoDelegateLoader source) {
            setDelegate(source.getDelegate());
        }

        public void delegateError(VideoDelegateLoader source) {

        }
    }
   

    private class StartBinding implements TriggerControl.Binding {

        public void trigger(long time) {
            if (video != null) {
                try {
                    video.play();
                } catch (StateException ex) {
                    Logger.getLogger(VideoCapture.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private class StopBinding implements TriggerControl.Binding {

        public void trigger(long time) {
            if (video != null) {
                try {
                    video.stop();
                } catch (StateException ex) {
                    Logger.getLogger(VideoCapture.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
