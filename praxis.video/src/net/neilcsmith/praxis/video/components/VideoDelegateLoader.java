/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2010 Neil C Smith.
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
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.Lookup;
import net.neilcsmith.praxis.core.types.PReference;
import net.neilcsmith.praxis.core.types.PString;
import net.neilcsmith.praxis.core.types.PResource;
import net.neilcsmith.praxis.impl.AbstractAsyncProperty;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.video.VideoDelegateFactory;
import net.neilcsmith.ripl.delegates.VideoDelegate;

/**
 *
 * @author Neil C Smith
 * @TODO add library name mechanism
 */
public class VideoDelegateLoader extends AbstractAsyncProperty<VideoDelegate> {

    private Listener listener;

    public VideoDelegateLoader(AbstractComponent component, Listener listener) {
        super(PResource.info(true), VideoDelegate.class, PString.EMPTY);
        if (listener == null) {
            throw new NullPointerException();
        }
        this.listener = listener;
    }

//    @Override
//    protected Task getLoadTask(Argument id) {
////        Lookup getAll = getComponent().getRoot().getLookup();
//        Lookup lookup = getComponent().getParent().getLookup();
//        // @TODO - can we be called if parent is null?
//        return new LoadTask(lookup, id);
//    }

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

//    @Override
//    protected void setResource(VideoDelegate resource) {
//        binding.setDelegate(resource);
//    }
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
