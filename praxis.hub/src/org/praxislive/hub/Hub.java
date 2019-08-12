/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2019 Neil C Smith.
 * 
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * version 3 for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License version 3
 * along with this work; if not, see http://www.gnu.org/licenses/
 * 
 * 
 * Please visit https://www.praxislive.org if you need additional information or
 * have any questions.
 */
package org.praxislive.hub;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.praxislive.core.Clock;
import org.praxislive.core.ComponentAddress;
import org.praxislive.core.Lookup;
import org.praxislive.core.Packet;
import org.praxislive.core.Root;
import org.praxislive.core.RootHub;
import org.praxislive.core.services.Service;
import org.praxislive.core.services.Services;
import org.praxislive.impl.InstanceLookup;
import org.praxislive.script.impl.ScriptServiceImpl;
import org.praxislive.util.ArrayUtils;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public final class Hub {

    public final static String SYS_PREFIX = "_sys_";
    public final static String CORE_PREFIX = SYS_PREFIX + "core_";
    public final static String EXT_PREFIX = SYS_PREFIX + "ext_";

    private final ConcurrentMap<String, Root.Controller> roots;
    private final ConcurrentMap<Class<? extends Service>, ComponentAddress[]> services;
    private final Root core;
    private final Lookup lookup;
    private final RootHubImpl rootHub;

//    private Thread coreThread;
    private String[] rootIDs;
    private Thread coreThread;
    private Root.Controller coreController;
    long startTime;

    private Hub(Builder builder) {
        CoreRootFactory coreFactory = builder.coreRootFactory;
        List<Root> exts = new ArrayList<>();
        extractExtensions(builder, exts);
        core = coreFactory.createCoreRoot(new Accessor(), exts);
        builder.lookupContent.add(0, new ServicesImpl());
        Lookup lkp = InstanceLookup.create(builder.lookupContent.toArray());
        lkp = coreFactory.extendLookup(lkp);
        lookup = lkp;
        roots = new ConcurrentHashMap<>();
        services = new ConcurrentHashMap<>();
        rootHub = new RootHubImpl();
        rootIDs = new String[0];
    }

    private void extractExtensions(Builder builder, List<Root> exts) {
            exts.add(new DefaultComponentFactoryService());
            exts.add(new ScriptServiceImpl());
            exts.add(new DefaultTaskService());
            exts.addAll(builder.extensions);
    }
    
    public synchronized void start() throws Exception {
        if (coreThread != null) {
            throw new IllegalStateException();
        }
        startTime = System.nanoTime();
        String coreID = CORE_PREFIX + Integer.toHexString(core.hashCode());
        coreController = core.initialize(coreID, rootHub);
        roots.put(coreID, coreController);
        coreController.start(r -> {
            coreThread =  new Thread(r, "PRAXIS_CORE_THREAD");
            return coreThread;
        });
        assert coreThread.isAlive();
    }

    public void shutdown() {
        coreController.shutdown();
    }

    public void await() throws InterruptedException {
        coreThread.join();
    }
    
    public void await(long time, TimeUnit unit) throws InterruptedException, TimeoutException {
        coreThread.join(unit.toMillis(time));
        if (coreThread.isAlive()) {
            throw new TimeoutException();
        }
    }
    
    public boolean isAlive() {
        return coreThread.isAlive();
    }

    private boolean registerRootController(String id, Root.Controller controller) {
        if (id == null || controller == null) {
            throw new NullPointerException();
        }
        Root.Controller existing = roots.putIfAbsent(id, controller);
        if (existing == null) {
            rootIDs = ArrayUtils.add(rootIDs, id);
            return true;
        } else {
            return false;
        }
    }

    private Root.Controller unregisterRootController(String id) {
        rootIDs = ArrayUtils.remove(rootIDs, id);
        return roots.remove(id);
    }

    private Root.Controller getRootController(String id) {
        return roots.get(id);
    }

    private String[] getRootIDs() {
        return rootIDs;
    }
    
    private RootHub getRootHub() {
        return rootHub;
    }
    
    private void registerService(Class<? extends Service> service,
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
            
    private Set<Class<? extends Service>> getServices() {
        return Collections.unmodifiableSet(services.keySet());
    }
    
    public static Builder builder() {
        return new Builder();
    }

    private class RootHubImpl implements RootHub, Clock {

        @Override
        public boolean dispatch(Packet packet) {
            Root.Controller dest = roots.get(packet.rootID());
            if (dest != null) {
                return dest.submitPacket(packet);
            } else {
                return coreController.submitPacket(packet);
            }
        }

        @Override
        public Lookup getLookup() {
            return lookup;
        }

        @Override
        public Clock getClock() {
            return this;
        }

        @Override
        public long getTime() {
            return System.nanoTime() - startTime;
        }

    }

    private class ServicesImpl implements Services {

        @Override
        public Optional<ComponentAddress> locate(Class<? extends Service> service) {
            ComponentAddress[] provs = services.get(service);
            if (provs == null || provs.length == 0) {
                return Optional.empty();
            } else {
                return Optional.of(provs[0]);
            }
        }

        @Override
        public Stream<ComponentAddress> locateAll(Class<? extends Service> service) {
            ComponentAddress[] provs = services.get(service);
            if (provs == null) {
                return Stream.empty();
            } else {
                return Stream.of(provs);
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

        public String[] getRootIDs() {
            return Hub.this.getRootIDs();
        }

        public void registerService(Class<? extends Service> service,
            ComponentAddress provider) {
            Hub.this.registerService(service, provider);
        }
        
        public Set<Class<? extends Service>> getServices() {
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
        private final List<Object> lookupContent;
        private CoreRootFactory coreRootFactory;

        private Builder() {
            extensions = new ArrayList<>();
            lookupContent = new ArrayList<>();
            coreRootFactory = BasicCoreRoot.factory();
            extensions.addAll(findDefaultExtensions());
        }
       
        private List<Root> findDefaultExtensions() {
            return Lookup.SYSTEM.findAll(RootHub.ExtensionProvider.class)
                    .flatMap(ep -> ep.getExtensions().stream())
                    .collect(Collectors.toList());
        }
        
        public Builder setCoreRootFactory(CoreRootFactory coreRootFactory) {
            this.coreRootFactory = Objects.requireNonNull(coreRootFactory);
            return this;
        }
        
        public Builder addExtension(Root extension) {
            extensions.add(Objects.requireNonNull(extension));
            return this;
        }
        
        public Builder extendLookup(Object obj) {
            lookupContent.add(Objects.requireNonNull(obj));
            return this;
        }
        
        public Hub build() {
            
            Hub hub = new Hub(this);
            return hub;
        }

    }

}
