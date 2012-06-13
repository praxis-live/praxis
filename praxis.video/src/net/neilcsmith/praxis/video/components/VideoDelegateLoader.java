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

import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.interfaces.TaskService;
import net.neilcsmith.praxis.video.InvalidVideoResourceException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.Lookup;
import net.neilcsmith.praxis.core.info.ArgumentInfo;
import net.neilcsmith.praxis.core.types.*;
import net.neilcsmith.praxis.impl.AbstractAsyncProperty;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.video.VideoDelegateFactory;
import net.neilcsmith.praxis.video.VideoSettings;
import net.neilcsmith.ripl.delegates.VideoDelegate;

/**
 *
 * @author Neil C Smith
 */
public class VideoDelegateLoader extends AbstractAsyncProperty<VideoDelegate> {
    
    private final static List<Argument> suggestedValues;
    static {
        List<Argument> list = new ArrayList<Argument>(4);
        list.add(PString.valueOf("capture://0"));
        list.add(PString.valueOf("capture://1"));
        list.add(PString.valueOf("capture://2"));
        list.add(PString.valueOf("capture://3"));
        suggestedValues = Collections.unmodifiableList(list);
    }
    

    private Listener listener;

    public VideoDelegateLoader(AbstractComponent component, Listener listener) {
        super(ArgumentInfo.create(PResource.class, PMap.create(
                ArgumentInfo.KEY_ALLOW_EMPTY, true,
                ArgumentInfo.KEY_SUGGESTED_VALUES, PArray.valueOf(suggestedValues))),
                VideoDelegate.class, PString.EMPTY);
        if (listener == null) {
            throw new NullPointerException();
        }
        this.listener = listener;
    }


    public VideoDelegate getDelegate() {
        return getValue();
    }

    @Override
    protected TaskService.Task createTask(CallArguments keys) throws Exception {
        Argument key;
        if (keys.getSize() < 1 || (key = keys.get(0)).isEmpty()) {
            return null;
        } else {
            return new LoadTask(getLookup(), key);
        }
    }

    public static interface Listener {

        public void delegateLoaded(VideoDelegateLoader source, long time);

        public void delegateError(VideoDelegateLoader source, long time);
    }

    private class LoadTask implements TaskService.Task {

        private Lookup lookup;
        private Argument id;

        private LoadTask(Lookup lookup, Argument id) {
            this.lookup = lookup;
            this.id = id;
        }

        public Argument execute() throws Exception {
            URI uri = PResource.coerce(id).value();
            if ("capture".equals(uri.getScheme())) {
                uri = translateCapture(uri);
            }
            Lookup.Result<VideoDelegateFactory.Provider> providers =
                    lookup.getAll(VideoDelegateFactory.Provider.class);
            VideoDelegate delegate = null;
            for (VideoDelegateFactory.Provider provider : providers) {
                if (provider.getSupportedSchemes().contains(uri.getScheme())) {
                    try {
                        delegate = provider.getFactory().create(uri);
                        break;
                    } catch (Exception ex) {
                        // log
                    }
                }
            }
            if (delegate == null) {
                throw new InvalidVideoResourceException();
            }
            try {
                VideoDelegate.State state = delegate.initialize();
                if (state == VideoDelegate.State.Ready) {
                    return PReference.wrap(delegate);
                } else {
                    delegate.dispose();
                    throw new InvalidVideoResourceException();
                }
            } catch (Exception ex) {
                delegate.dispose();
                throw new InvalidVideoResourceException();
            }
        }
        
        private URI translateCapture(URI uri) {
            int idx = 0;
            try {
                String auth = uri.getAuthority();
                if (auth != null) {
                    idx = Integer.parseInt(uri.getAuthority());
                }          
            } catch (Exception ex) {}
            String dev = VideoSettings.getCaptureDevice(idx);
            try {
                URI out = new URI(dev);
                if (out.getQuery() == null) {
                    String query = uri.getQuery();
                    if (query != null) {
                        out = new URI(out.getScheme(), out.getAuthority(), null, query, null);
                    }
                }
                return out;
            } catch (URISyntaxException ex) {
                return uri;
            }
        }
    }


    @Override
    protected void valueChanged(long time) {
        listener.delegateLoaded(this, time);
    }

    @Override
    protected void taskError(long time) {
        listener.delegateError(this, time);
    }


}
