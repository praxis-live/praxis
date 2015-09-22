/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2015 Neil C Smith.
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
package net.neilcsmith.praxis.video.gst1.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.Lookup;
import net.neilcsmith.praxis.core.info.ArgumentInfo;
import net.neilcsmith.praxis.core.interfaces.TaskService;
import net.neilcsmith.praxis.core.types.PArray;
import net.neilcsmith.praxis.core.types.PMap;
import net.neilcsmith.praxis.core.types.PReference;
import net.neilcsmith.praxis.core.types.PString;
import net.neilcsmith.praxis.impl.AbstractAsyncProperty;
import net.neilcsmith.praxis.impl.TriggerControl;
import net.neilcsmith.praxis.video.InvalidVideoResourceException;
import net.neilcsmith.praxis.video.VideoSettings;

/**
 *
 * @author Neil C Smith
 */
public class VideoCapture extends AbstractVideoComponent {

    private final static List<Argument> suggestedValues;

    static {
        List<Argument> list = new ArrayList<>(4);
        list.add(PString.valueOf("1"));
        list.add(PString.valueOf("2"));
        list.add(PString.valueOf("3"));
        list.add(PString.valueOf("4"));
        suggestedValues = Collections.unmodifiableList(list);
    }

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
    }

    private class DelegateLoader extends AbstractAsyncProperty<VideoDelegate> {

        DelegateLoader() {
            super(ArgumentInfo.create(PString.class, PMap.create(
                    ArgumentInfo.KEY_SUGGESTED_VALUES, PArray.valueOf(suggestedValues))),
                    VideoDelegate.class, PString.EMPTY);
        }

        @Override
        protected TaskService.Task createTask(CallArguments keys) throws Exception {
            Argument key;
            if (keys.getSize() < 1 || (key = keys.get(0)).isEmpty()) {
                return null;
            } else {
                return new LoadTask(getLookup(), key.toString());
            }
        }

        @Override
        protected void valueChanged(long time) {
            setDelegate(getValue());
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
        public Argument execute() throws Exception {

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
                        = VideoSettings.getCaptureDevice(Integer.valueOf(dev));
                return dsc;
            } catch (Exception ex) {
                return null;
            }

        }

        private VideoDelegate createDelegateFromDescription(String desc) {
            return VideoDelegateFactory.getInstance().createCaptureDelegate(desc);
        }

    }

}
