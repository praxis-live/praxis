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

import net.neilcsmith.praxis.video.VideoDelegateFactoryProvider;
import net.neilcsmith.praxis.video.InvalidVideoResourceException;
import java.net.URI;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.Component;
import net.neilcsmith.praxis.core.Lookup;
import net.neilcsmith.praxis.core.Task;
import net.neilcsmith.praxis.core.types.PReference;
import net.neilcsmith.praxis.core.types.PUri;
import net.neilcsmith.praxis.impl.ResourceLoader;
import net.neilcsmith.ripl.delegates.VideoDelegate;

/**
 *
 * @author Neil C Smith
 * @TODO add library name mechanism
 */
public class VideoDelegateLoader extends ResourceLoader<VideoDelegate> {
    
    private Listener listener;

    public VideoDelegateLoader(Component component, Listener listener) {
        super(component, VideoDelegate.class);
        if (listener == null) {
            throw new NullPointerException();
        }
        this.listener = listener;
    }

    @Override
    protected Task getLoadTask(Argument id) {
        Lookup lookup = getComponent().getRoot().getLookup();
        // @TODO - can we be called if root is null?
        return new LoadTask(lookup, id);
    }
    
    public VideoDelegate getDelegate() {
        return getResource();
    }

//    @Override
//    protected void setResource(VideoDelegate resource) {
//        binding.setDelegate(resource);
//    }
    
    public static interface Listener {
        
        public void delegateLoaded(VideoDelegateLoader source);
        
        public void delegateError(VideoDelegateLoader source);
        
    }

    private class LoadTask implements Task {

        private Lookup lookup;
        private Argument id;

        private LoadTask(Lookup lookup, Argument id) {
            this.lookup = lookup;
            this.id = id;
        }

        public Argument execute() throws Exception {
            URI uri = PUri.coerce(id).value();
            Lookup.Result<VideoDelegateFactoryProvider> providers = 
                    lookup.lookup(VideoDelegateFactoryProvider.class);
            VideoDelegate delegate = null;
            for (VideoDelegateFactoryProvider provider : providers) {
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
    protected void resourceLoaded() {
        listener.delegateLoaded(this);
    }

    @Override
    protected void resourceError() {
        listener.delegateError(this);
    }
}
