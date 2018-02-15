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
package org.praxislive.video.gst1.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.praxislive.core.Value;
import org.praxislive.core.CallArguments;
import org.praxislive.core.ControlPort;
import org.praxislive.core.Lookup;
import org.praxislive.core.ArgumentInfo;
import org.praxislive.core.services.TaskService;
import org.praxislive.core.types.PArray;
import org.praxislive.core.types.PMap;
import org.praxislive.core.types.PReference;
import org.praxislive.core.types.PString;
import org.praxislive.impl.AbstractAsyncProperty;
import org.praxislive.impl.DefaultControlOutputPort;
import org.praxislive.impl.TriggerControl;
import org.praxislive.video.InvalidVideoResourceException;
import org.praxislive.video.gst1.GStreamerSettings;

/**
 *
 * @author Neil C Smith
 */
public class VideoCapture extends AbstractVideoComponent {

    private final static List<Value> suggestedValues;

    static {
        List<Value> list = new ArrayList<>(5);
        list.add(PString.valueOf("autovideosrc"));
        list.add(PString.valueOf("1"));
        list.add(PString.valueOf("2"));
        list.add(PString.valueOf("3"));
        list.add(PString.valueOf("4"));
        suggestedValues = Collections.unmodifiableList(list);
    }

    private final ControlPort.Output readyPort;
    private final ControlPort.Output errorPort;
    
    private DelegateLoader loader;

    public VideoCapture() {

        loader = new DelegateLoader();
        registerControl("device", loader);

        createResizeModeControls();
        createSourceCapsControls();

        TriggerControl play = TriggerControl.create(createTriggerBinding(TriggerState.Play));
        registerControl("play", play);
        registerPort("play", play.createPort());
        TriggerControl stop = TriggerControl.create(createTriggerBinding(TriggerState.Stop));
        registerControl("stop", stop);
        registerPort("stop", stop.createPort());
        
        readyPort = new DefaultControlOutputPort();
        registerPort("ready", readyPort);
        errorPort = new DefaultControlOutputPort();
        registerPort("error", errorPort);
        
    }

    private class DelegateLoader extends AbstractAsyncProperty<VideoDelegate> {

        DelegateLoader() {
            super(ArgumentInfo.create(PString.class, PMap.create(
                    ArgumentInfo.KEY_SUGGESTED_VALUES, PArray.valueOf(suggestedValues))),
                    VideoDelegate.class, PString.EMPTY);
        }

        @Override
        protected TaskService.Task createTask(CallArguments keys) throws Exception {
            Value key;
            if (keys.getSize() < 1 || (key = keys.get(0)).isEmpty()) {
                return null;
            } else {
                return new LoadTask(getLookup(), key.toString());
            }
        }

        @Override
        protected void valueChanged(long time) {
            setDelegate(getValue());
            if (rootActive) {
                readyPort.send(time);
            }
        }

        @Override
        protected void taskError(long time) {
            if (rootActive) {
                errorPort.send(time);
            }
        }

    }

    private class LoadTask implements TaskService.Task {

        private final Lookup lookup;
        private final String source;

        private LoadTask(Lookup lookup, String source) {
            this.lookup = lookup;
            this.source = source;
        }

        @Override
        public Value execute() throws Exception {

            String dsc = source;
            VideoDelegate delegate = null;
            if (dsc.length() == 1) {
                String s = getDefaultDeviceDescription(source);
                dsc = s == null ? dsc : s;
            }
            delegate = createDelegateFromDescription(dsc);
            if (delegate == null) {
                throw new InvalidVideoResourceException();
            }
            try {
                VideoDelegate.State state = delegate.initialize();
                if (state == VideoDelegate.State.Ready) {
                    return PReference.wrap(delegate);
                }
            } catch (Exception ex) {
                delegate.dispose();
                throw new InvalidVideoResourceException(ex);
            }
            delegate.dispose();
            throw new InvalidVideoResourceException();
        }

        private String getDefaultDeviceDescription(String dev) {
            try {
                String dsc
                        = GStreamerSettings.getCaptureDevice(Integer.valueOf(dev));
                return dsc;
            } catch (Exception ex) {
                return null;
            }

        }

        private VideoDelegate createDelegateFromDescription(String desc) {
            return new BinDelegate(desc);
        }

    }

}
