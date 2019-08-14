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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.praxislive.base.AbstractAsyncControl;
import org.praxislive.base.AbstractRoot;
import org.praxislive.core.Call;
import org.praxislive.core.Component;
import org.praxislive.core.ComponentAddress;
import org.praxislive.core.ControlAddress;
import org.praxislive.core.Root;
import org.praxislive.core.RootHub;
import org.praxislive.core.ComponentInfo;
import org.praxislive.core.Control;
import org.praxislive.core.PacketRouter;
import org.praxislive.core.Value;
import org.praxislive.core.services.RootFactoryService;
import org.praxislive.core.services.RootManagerService;
import org.praxislive.core.services.Service;
import org.praxislive.core.types.PArray;
import org.praxislive.core.types.PError;
import org.praxislive.core.types.PReference;
import org.praxislive.core.types.PString;

/**
 *
 */
public class BasicCoreRoot extends AbstractRoot {

    private final static Logger LOG = Logger.getLogger(BasicCoreRoot.class.getName());

    private final Hub.Accessor hubAccess;
    private final List<Root> exts;
    private final Map<String, Control> controls;

    private Controller controller;

    protected BasicCoreRoot(Hub.Accessor hubAccess, List<Root> exts) {
        this.hubAccess = Objects.requireNonNull(hubAccess);
        this.exts = Objects.requireNonNull(exts);
        this.controls = new HashMap<>();
    }

    @Override
    public Controller initialize(String id, RootHub hub) {
        Controller ctrl = super.initialize(id, hub);
        this.controller = ctrl;
        return ctrl;
    }

    @Override
    protected void activating() {
        registerServices();
        HashMap<String, Control> ctrls = new HashMap<>();
        buildControlMap(ctrls);
        controls.putAll(ctrls);
        installExtensions();
    }

    @Override
    protected void terminating() {
        String[] ids = hubAccess.getRootIDs();
        for (String id : ids) {
            uninstallRoot(id);
        }
    }

    protected void forceTermination() {
        controller.shutdown();
        interrupt();
    }

    @Override
    protected void processCall(Call call, PacketRouter router) {
        try {
            controls.get(call.to().controlID()).call(call, router);
        } catch (Exception ex) {
            router.route(Call.createErrorCall(call, PError.of(ex)));
        }
    }

    protected void registerServices() {
        hubAccess.registerService(RootManagerService.class, getAddress());
    }

    protected void buildControlMap(Map<String, Control> ctrls) {
        ctrls.computeIfAbsent(RootManagerService.ADD_ROOT, k -> new AddRootControl());
        ctrls.computeIfAbsent(RootManagerService.REMOVE_ROOT, k -> new RemoveRootControl());
        ctrls.computeIfAbsent(RootManagerService.ROOTS, k -> new RootsControl());
//        ctrls.computeIfAbsent(ComponentProtocol.INFO, k -> (call, router) -> 
//            router.route(Call.createReturnCall(call, RootManagerService.API_INFO))
//        );
    }

    protected void installExtensions() {
        for (Root ext : exts) {
            List<Class<? extends Service>> services = extractServices(ext);
            String extID = Hub.EXT_PREFIX + Integer.toHexString(ext.hashCode());
            try {
                LOG.log(Level.CONFIG, "Installing extension {0}", extID);
                installRoot(extID, "sysex", ext);
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, "Failed to install extension\n{0} to /{1}\n{2}",
                        new Object[]{ext.getClass(), extID, ex});
                continue;
            }
            ComponentAddress ad = ComponentAddress.of("/" + extID);
            for (Class<? extends Service> service : services) {
                LOG.log(Level.CONFIG, "Registering service {0}", service);
                hubAccess.registerService(service, ad);
            }
        }
    }

    private List<Class<? extends Service>> extractServices(Root root) {
        if (root instanceof RootHub.ServiceProvider) {
            return ((RootHub.ServiceProvider) root).services();
        } else if (root instanceof Component) {
            ComponentInfo info = ((Component) root).getInfo();
            return info.protocols()
                    .filter(Service.class::isAssignableFrom)
                    .map(c -> c.asSubclass(Service.class))
                    .collect(Collectors.toList());

        } else {
            return Collections.EMPTY_LIST;
        }
    }

    protected void installRoot(String id, String type, Root root)
            throws Exception {
        if (!ComponentAddress.isValidID(id) || hubAccess.getRootController(id) != null) {
            throw new IllegalArgumentException();
        }
        Root.Controller ctrl = root.initialize(id, hubAccess.getRootHub());
        if (hubAccess.registerRootController(id, ctrl)) {
            startRoot(id, type, ctrl);
        } else {
            assert false;
        }
    }

    protected void uninstallRoot(String id) {
        Root.Controller ctrl = hubAccess.unregisterRootController(id);
        if (ctrl != null) {
            ctrl.shutdown();
        }
    }

    protected void startRoot(final String id, String type, final Root.Controller ctrl) {
        ctrl.start(r -> new Thread(r, id));
    }

    protected Hub.Accessor getHubAccessor() {
        return hubAccess;
    }

    public static Hub.CoreRootFactory factory() {
        return new Factory();
    }

    private class AddRootControl extends AbstractAsyncControl {

        @Override
        protected Call processInvoke(Call call) throws Exception {
            List<Value> args = call.args();
            if (args.size() < 2) {
                throw new IllegalArgumentException("Invalid arguments");
            }
            if (!ComponentAddress.isValidID(args.get(0).toString())) {
                throw new IllegalArgumentException("Invalid Component ID");
            }
            ControlAddress to = ControlAddress.of(findService(RootFactoryService.class),
                    RootFactoryService.NEW_ROOT_INSTANCE);
            return Call.create(to, call.to(), call.time(), args.get(1));
        }

        @Override
        protected Call processResponse(Call call) throws Exception {
            List<Value> args = call.args();
            if (args.size() < 1) {
                throw new IllegalArgumentException("Invalid response");
            }
            Root r = (Root) ((PReference) args.get(0)).getReference();
            Call active = getActiveCall();
//            addChild(active.getArgs().get(0).toString(), c);
            String id = active.args().get(0).toString();
            String type = active.args().get(1).toString();
            installRoot(id, type, r);
            return active.reply();
        }

    }

    private class RemoveRootControl implements Control {

        @Override
        public void call(Call call, PacketRouter router) throws Exception {
            if (call.isRequest()) {
                String id = call.args().get(0).toString();
                uninstallRoot(id);
                router.route(call.reply());
            } else {
                throw new IllegalArgumentException();
            }
        }

    }

    private class RootsControl implements Control {

        private String[] knownIDs;
        private PArray ret;

        private RootsControl() {
            knownIDs = new String[0];
            ret = PArray.EMPTY;
        }

        @Override
        public void call(Call call, PacketRouter router) throws Exception {
            if (call.isRequest()) {
                String[] ids = hubAccess.getRootIDs();
                if (!Arrays.equals(ids, knownIDs)) {
                    knownIDs = ids;
                    ret = Stream.of(ids).map(PString::of).collect(PArray.collector());
                }
                router.route(call.reply(ret));
            } else {
                throw new IllegalArgumentException();
            }
        }
    }

    private static class Factory extends Hub.CoreRootFactory {

        @Override
        public Root createCoreRoot(Hub.Accessor accessor, List<Root> extensions) {
            return new BasicCoreRoot(accessor, extensions);
        }

    }

}
