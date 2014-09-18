/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2014 Neil C Smith.
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
package net.neilcsmith.praxis.hub;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.ComponentAddress;
import net.neilcsmith.praxis.core.IllegalRootStateException;
import net.neilcsmith.praxis.core.InterfaceDefinition;
import net.neilcsmith.praxis.core.InvalidAddressException;
import net.neilcsmith.praxis.core.Lookup;
import net.neilcsmith.praxis.core.Packet;
import net.neilcsmith.praxis.core.Root;
import net.neilcsmith.praxis.core.RootHub;
import net.neilcsmith.praxis.core.interfaces.ServiceManager;
import net.neilcsmith.praxis.core.interfaces.ServiceUnavailableException;
import net.neilcsmith.praxis.core.interfaces.Services;
import net.neilcsmith.praxis.impl.InstanceLookup;
import net.neilcsmith.praxis.script.impl.ScriptServiceImpl;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public final class Hub {

    public final static String SYS_PREFIX = "_sys_";
    public final static String CORE_PREFIX = SYS_PREFIX + "core_";
    public final static String EXT_PREFIX = SYS_PREFIX + "ext_";

    private final ConcurrentMap<String, Root.Controller> roots;
    private final ConcurrentMap<Class<? extends InterfaceDefinition>, ComponentAddress[]> services;
    private final Root core;
    private final Lookup lookup;
    private final RootHubImpl rootHub;

    private Thread coreThread;
    private Root.Controller coreController;

    private Hub(Builder builder) {
        CoreRootFactory coreFactory = builder.coreRootFactory;
        List<Root> exts = new ArrayList<>();
        extractExtensions(builder, exts);
        core = coreFactory.createCoreRoot(new Accessor(), exts);
        Lookup lkp = InstanceLookup.create(new ServicesImpl());
        lkp = coreFactory.extendLookup(lkp);
        lookup = lkp;
        roots = new ConcurrentHashMap<>();
        services = new ConcurrentHashMap<>();
        rootHub = new RootHubImpl();
    }

    private void extractExtensions(Builder builder, List<Root> exts) {
            exts.add(builder.componentFactory == null ? 
                    new DefaultComponentFactoryService() :
                    builder.componentFactory);
            exts.add(builder.scriptService == null ?
                    new ScriptServiceImpl() :
                    builder.scriptService);
            exts.add(builder.taskService == null ?
                    new DefaultTaskService() :
                    builder.taskService);
            exts.addAll(builder.extensions);
    }
    
    public void start() throws Exception {
        if (coreThread != null) {
            throw new IllegalStateException();
        }
        String coreID = CORE_PREFIX + Integer.toHexString(core.hashCode());
        coreController = core.initialize(coreID, rootHub);
        roots.put(coreID, coreController);
        Runnable runner = new Runnable() {

            @Override
            public void run() {
                try {
                    coreController.run();
                } catch (IllegalRootStateException ex) {
                    Logger.getLogger(Hub.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        coreThread = new Thread(runner, "PRAXIS_CORE_THREAD");
        coreThread.start();
    }

    public void shutdown() {
        coreController.shutdown();
    }

    public void await(long ms) throws InterruptedException {
        coreThread.join(ms);
    }
    
    public boolean isAlive() {
        Thread t = coreThread;
        return t == null ? false : t.isAlive();
    }

    private boolean registerRootController(String id, Root.Controller controller) {
        if (id == null || controller == null) {
            throw new NullPointerException();
        }
        Root.Controller existing = roots.putIfAbsent(id, controller);
        return existing == null;
    }

    private Root.Controller unregisterRootController(String id) {
        return roots.remove(id);
    }

    private Root.Controller getRootController(String id) {
        return roots.get(id);
    }

    private Set<String> getRootIDs() {
        return Collections.unmodifiableSet(roots.keySet());
    }
    
    private RootHub getRootHub() {
        return rootHub;
    }
    
    private void registerService(Class<? extends InterfaceDefinition> service,
            ComponentAddress provider) {
        if (service == null || provider == null) {
            throw new NullPointerException();
        }
        ComponentAddress[] provs = services.get(service);
        if (provs == null) {
            services.put(service, new ComponentAddress[]{provider});
        } else {
            ComponentAddress[] nprovs = new ComponentAddress[provs.length + 1];
            nprovs[0] = provider;
            System.arraycopy(provs, 0, nprovs, 1, provs.length);
            services.put(service, nprovs);
        }
    }
            
    private Set<Class<? extends InterfaceDefinition>> getServices() {
        return Collections.unmodifiableSet(services.keySet());
    }
    
    public static Builder builder() {
        return new Builder();
    }

    private class RootHubImpl implements RootHub {

        @Override
        public void dispatch(Packet packet) throws InvalidAddressException {
            Root.Controller dest = roots.get(packet.getRootID());
            if (dest != null) {
                dest.submitPacket(packet);
            } else {
                coreController.submitPacket(packet);
            }
        }

        @Override
        public Lookup getLookup() {
            return lookup;
        }

    }

    private class ServicesImpl extends Services implements ServiceManager {

        // THREAD SAFE
        @Override
        public ComponentAddress findService(InterfaceDefinition info) throws ServiceUnavailableException {
            return findService(info.getClass());
        }

        // THREAD SAFE
        @Override
        public ComponentAddress[] findAllServices(InterfaceDefinition info) throws ServiceUnavailableException {
            return findAllServices(info.getClass());
        }

        // THREAD SAFE
        public ComponentAddress findService(Class<? extends InterfaceDefinition> def)
                throws ServiceUnavailableException {
            return findServicesImpl(def)[0];
        }

        // THREAD SAFE
        public ComponentAddress[] findAllServices(Class<? extends InterfaceDefinition> def)
                throws ServiceUnavailableException {
            ComponentAddress[] provs = findServicesImpl(def);
            return Arrays.copyOf(provs, provs.length);
        }

        private ComponentAddress[] findServicesImpl(Class<? extends InterfaceDefinition> def) throws ServiceUnavailableException {
            ComponentAddress[] provs = services.get(def);
            if (provs == null) {
                throw new ServiceUnavailableException();
            } else {
                return provs;
            }
        }

    }

    public final class Accessor {

        public boolean registerRootController(String id, Root.Controller controller) {
            return Hub.this.registerRootController(id, controller);
        }

        public Root.Controller unregisterRootController(String id) {
            return Hub.this.unregisterRootController(id);
        }

        public Root.Controller getRootController(String id) {
            return Hub.this.getRootController(id);
        }

        public Set<String> getRootIDs() {
            return Hub.this.getRootIDs();
        }

        public void registerService(Class<? extends InterfaceDefinition> service,
            ComponentAddress provider) {
            Hub.this.registerService(service, provider);
        }
        
        public Set<Class<? extends InterfaceDefinition>> getServices() {
            return Hub.this.getServices();
        }
        
        public RootHub getRootHub() {
            return Hub.this.getRootHub();
        }
        
    }

    public static abstract class CoreRootFactory {

        public abstract Root createCoreRoot(Accessor accessor, List<Root> extensions);

        public Lookup extendLookup(Lookup lookup) {
            return lookup;
        }

    }

    public static class Builder {

        private final List<Root> extensions;
        private CoreRootFactory coreRootFactory;
        private Root componentFactory;
        private Root scriptService;
        private Root taskService;

        private Builder() {
            extensions = new ArrayList<>();
            coreRootFactory = DefaultCoreRoot.factory();
        }
       
        public Builder setCoreRootFactory(CoreRootFactory coreRootFactory) {
            if (coreRootFactory == null) {
                throw new NullPointerException();
            }
            this.coreRootFactory = coreRootFactory;
            return this;
        }
               
        public Builder replaceComponentFactoryService(Root componentFactory) {
            this.componentFactory = componentFactory;
            return this;
        }
        
        public Builder replaceScriptService(Root scriptService) {
            this.scriptService = scriptService;
            return this;
        }
        
        public Builder replaceTaskService(Root taskService) {
            this.taskService = taskService;
            return this;
        }
        
        public Builder addExtension(Root extension) {
            if (extension == null) {
                throw new NullPointerException();
            }
            extensions.add(extension);
            return this;
        }
        
        public Hub build() {
            
            Hub hub = new Hub(this);
            return hub;
        }

    }

}
