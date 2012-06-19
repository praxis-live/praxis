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

import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.ExecutionContext;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.impl.AbstractExecutionContextComponent;
import net.neilcsmith.praxis.impl.ArgumentProperty;
import net.neilcsmith.praxis.impl.TriggerControl;
import net.neilcsmith.praxis.video.impl.DefaultVideoOutputPort;
import net.neilcsmith.ripl.components.Delegator;
import net.neilcsmith.ripl.delegates.VideoDelegate;
import net.neilcsmith.ripl.delegates.VideoDelegate.StateException;

/**
 *
 * @author Neil C Smith
 */
public class VideoCapture extends AbstractExecutionContextComponent {
    
    private VideoDelegate video;
    private Delegator container;
    private VideoDelegateLoader loader;

    public VideoCapture() {
        
        container = new Delegator();
        registerPort(Port.OUT, new DefaultVideoOutputPort(this, container));
        
        loader = new VideoDelegateLoader(this, new VideoBinding(), true);
        registerControl("device", loader);
        TriggerControl start = TriggerControl.create( new StartBinding());
        registerControl("play", start);
        registerPort("play", start.createPort());
        TriggerControl stop = TriggerControl.create( new StopBinding());
        registerControl("stop", stop);
        registerPort("stop", stop.createPort());
               
    }

    public void stateChanged(ExecutionContext source) {
        if (source.getState() == ExecutionContext.State.IDLE) {
            if (video != null) {
                try {
                    video.stop();
                } catch (StateException ex) {
                // no op
                }
            }
        } else if (source.getState() == ExecutionContext.State.TERMINATED) {
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

        public void delegateLoaded(VideoDelegateLoader source, long time) {
            setDelegate(source.getDelegate());
        }

        public void delegateError(VideoDelegateLoader source, long time) {

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
