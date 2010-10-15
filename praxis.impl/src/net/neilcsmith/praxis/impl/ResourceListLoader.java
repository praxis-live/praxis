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
package net.neilcsmith.praxis.impl;

import java.io.File;
import java.net.URI;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.Component;
import net.neilcsmith.praxis.core.interfaces.Task;
import net.neilcsmith.praxis.core.types.PArray;
import net.neilcsmith.praxis.core.types.PUri;

/**
 *
 * @author Neil C Smith
 */
public class ResourceListLoader extends ResourceLoader<PArray> {

    private static Logger logger = Logger.getLogger(ResourceListLoader.class.getName());
    private Listener listener;
    private Filter filter;

    private ResourceListLoader(Component host, Listener listener, Filter filter) {
        super(host, PArray.class);
        if (listener == null) {
            throw new NullPointerException();
        }
        this.listener = listener;
        this.filter = filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public Filter getFitler() {
        return filter;
    }

    @Override
    protected Task getLoadTask(Argument uri) {
        return new LoadTask(uri, filter);
    }
    
    public PArray getList() {
        return getResource();
    }

//    protected void setResource(PArray resource) {
//        if (resource != null) {
//            binding.setResourceList(resource);
//        }
//        
//    }

//    @Override
//    protected void setResource(Argument arg) {
//        if (arg instanceof PArray) {
//            binding.setResourceList((PArray) arg);
//        }
//        
//    }
    private static class LoadTask implements Task {

        private Argument uri;
        private Filter filter;

        private LoadTask(Argument uri, Filter filter) {
            this.uri = uri;
            this.filter = filter;
        }

        public Argument execute() throws Exception {
            URI loc = PUri.coerce(uri).value();
            if (!loc.getScheme().equals("file")) {
                return PArray.EMPTY;
            }
            File dir = new File(loc);
            if (!dir.isDirectory()) {
                return PArray.EMPTY;
            }
            File[] files = dir.listFiles();
            if (files == null) {
                return PArray.EMPTY;
            }
            ArrayList<String> names = new ArrayList<String>();
            for (File f : files) {
                if (!f.isDirectory() && !f.isHidden()) {
                    names.add(f.toURI().toString());
                }
            }
            Collator col = Collator.getInstance();
            Collections.sort(names, col);
            ArrayList<PUri> resources = new ArrayList<PUri>();
            for (String s : names) {
                URI u = URI.create(s);
                if (filter == null || filter.accept(u)) {
                    resources.add(PUri.valueOf(u));
                }
            }
            return PArray.valueOf(resources);


        }
    }

    public static interface Listener {

        public void listLoaded(ResourceListLoader source);
        
        public void listError(ResourceListLoader source);
    }

    public static interface Filter {

        public boolean accept(URI uri);
    }

//    public static void main(String[] args) throws Exception {
//        IUri uri = IUri.valueOf(new File("/home/nsigma/Documents").toURI());
//        Task t = new LoadTask(uri, new Filter() {
//
//            public boolean accept(URI uri) {
//                return true;
//            }
//        });
//        System.out.println(t.execute());
//    }
    public static ResourceListLoader create(Component host, Listener listener) {
        return create(host, listener, null);
    }

    public static ResourceListLoader create(Component host, Listener listener, Filter filter) {
        if (host == null || listener == null) {
            throw new NullPointerException();
        }
        return new ResourceListLoader(host, listener, filter);
    }

    @Override
    protected void resourceLoaded() {
        listener.listLoaded(this);
    }

    @Override
    protected void resourceError() {
        listener.listError(this);
    }
}
