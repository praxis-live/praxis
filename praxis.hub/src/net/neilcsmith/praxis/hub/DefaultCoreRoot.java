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
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.Call;
import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.Component;
import net.neilcsmith.praxis.core.ComponentAddress;
import net.neilcsmith.praxis.core.ControlAddress;
import net.neilcsmith.praxis.core.IllegalRootStateException;
import net.neilcsmith.praxis.core.InterfaceDefinition;
import net.neilcsmith.praxis.core.InvalidAddressException;
import net.neilcsmith.praxis.core.Root;
import net.neilcsmith.praxis.core.RootHub;
import net.neilcsmith.praxis.core.info.ComponentInfo;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.core.interfaces.RootFactoryService;
import net.neilcsmith.praxis.core.interfaces.RootManagerService;
import net.neilcsmith.praxis.core.interfaces.Service;
import net.neilcsmith.praxis.core.types.PArray;
import net.neilcsmith.praxis.core.types.PReference;
import net.neilcsmith.praxis.core.types.PString;
import net.neilcsmith.praxis.impl.AbstractAsyncControl;
import net.neilcsmith.praxis.impl.AbstractRoot;
import net.neilcsmith.praxis.impl.SimpleControl;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class DefaultCoreRoot extends AbstractRoot {
    
    private final static Logger LOG = Logger.getLogger(DefaultCoreRoot.class.getName());
    
    private final Hub.Accessor hubAccess;
    private final List<Root> exts;
    
    private RootHub rootHub;
    private Root.Controller controller;
    private String ID;
    
    protected DefaultCoreRoot(Hub.Accessor hubAccess, List<Root> exts) {
        if (hubAccess == null || exts == null) {
            throw new NullPointerException();
        }
        this.hubAccess = hubAccess;
        this.exts = exts;
    }

    @Override
    public Root.Controller initialize(String ID, RootHub hub) throws IllegalRootStateException {
        controller = super.initialize(ID, hub);
        this.ID = ID;
        return controller;
    }
    
    

    @Override
    protected void activating() {
        createDefaultControls();
        installExtensions();
    }

    @Override
    protected void terminating() {
        String[] ids = hubAccess.getRootIDs().toArray(new String[0]);
        for (String id : ids) {
            uninstallRoot(id);
        }
    }
    
    protected void forceTermination() {
        controller.shutdown();
        interrupt();
    }
    
    protected String getID() {
        return ID;
    }
    
    protected void createDefaultControls() {
        registerControl(RootManagerService.ADD_ROOT, new AddRootControl());
        registerControl(RootManagerService.REMOVE_ROOT, new RemoveRootControl());
        registerControl(RootManagerService.ROOTS, new RootsControl());
        registerInterface(RootManagerService.class);
        hubAccess.registerService(RootManagerService.class, getAddress());
    }
    
    protected void installExtensions() {
        List<Class<? extends Service>> services = new ArrayList<>();
        for (Root ext : exts) {
            services.clear();
            extractServices(ext, services);
            String extID = Hub.EXT_PREFIX + Integer.toHexString(ext.hashCode());
            try {
                LOG.log(Level.CONFIG, "Installing extension {0}", extID);
                installRoot(extID, "sysex", ext);
            } catch (InvalidAddressException | IllegalRootStateException ex) {
                LOG.log(Level.SEVERE, "Failed to install extension\n{0} to /{1}\n{2}",
                        new Object[]{ext.getClass(), extID, ex});
                continue;
            }
            ComponentAddress ad = ComponentAddress.create("/" + extID);
            for (Class<? extends Service> service : services) {
                LOG.log(Level.CONFIG, "Registering service {0}", service);
                hubAccess.registerService(service, ad);
            }
        }
    }
    
    private void extractServices(Root root, List<Class<? extends Service>> services) {
        if (root instanceof Component) {
            ComponentInfo info = ((Component) root).getInfo();
            for (Class<? extends InterfaceDefinition> id : info.getAllInterfaces()) {
                if (Service.class.isAssignableFrom(id)) {
                    services.add((Class<? extends Service>) id);
                }
            }
        }
    }
    
    protected void installRoot(String id, String type, Root root)
            throws InvalidAddressException, IllegalRootStateException {
        if (!ComponentAddress.isValidID(id) || hubAccess.getRootController(id) != null) {
            throw new InvalidAddressException();
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
        Thread thr = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    ctrl.run();
                } catch (IllegalRootStateException ex) {
                    LOG.severe("Root " + id + " threw root state exception");
                } finally {
                    if (hubAccess.unregisterRootController(id) != null) {
                        LOG.warning("Root " + id + " terminated unexpectedly");
                    }
                }
            }
        }, id);
        if ("root:audio".equals(type)) {
            thr.setPriority(Thread.MAX_PRIORITY);
        } else {
            thr.setPriority(7); // @TODO work out priority scheme
        }

        thr.start();
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
            CallArguments args = call.getArgs();
            if (args.getSize() < 2) {
                throw new IllegalArgumentException("Invalid arguments");
            }
            if (!ComponentAddress.isValidID(args.get(0).toString())) {
                throw new IllegalArgumentException("Invalid Component ID");
            }
            ControlAddress to = ControlAddress.create(
                    findService(RootFactoryService.class),
                    RootFactoryService.NEW_ROOT_INSTANCE);
            return Call.createCall(to, getAddress(), call.getTimecode(), args.get(1));
        }

        @Override
        protected Call processResponse(Call call) throws Exception {
            CallArguments args = call.getArgs();
            if (args.getSize() < 1) {
                throw new IllegalArgumentException("Invalid response");
            }
            Root r = (Root) ((PReference) args.get(0)).getReference();
            Call active = getActiveCall();
//            addChild(active.getArgs().get(0).toString(), c);
            String id = active.getArgs().get(0).toString();
            String type = active.getArgs().get(1).toString();
            installRoot(id, type, r);
            return Call.createReturnCall(active, CallArguments.EMPTY);
        }

        @Override
        public ControlInfo getInfo() {
            return RootManagerService.ADD_ROOT_INFO;
        }
        
    }
    
    private class RemoveRootControl extends SimpleControl {

        private RemoveRootControl() {
            super(RootManagerService.REMOVE_ROOT_INFO);
        }
        
        @Override
        protected CallArguments process(long time, CallArguments args, boolean quiet) throws Exception {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
    }

    private class RootsControl extends SimpleControl {

        private Set<String> knownIDs;
        private PArray ret;
        
        private RootsControl() {
            super(RootManagerService.ROOTS_INFO);
        }

        @Override
        protected CallArguments process(long time, CallArguments args, boolean quiet) throws Exception {
            Set<String> ids = hubAccess.getRootIDs();
            if (!ids.equals(knownIDs)) {
                knownIDs = ids;
                List<PString> list = new ArrayList<>(ids.size());
                for (String id : ids) {
                    list.add(PString.valueOf(id));
                }
                ret = PArray.valueOf(list);
            }
            return CallArguments.create(ret);
        }



    }
    
    private static class Factory extends Hub.CoreRootFactory {

        @Override
        public Root createCoreRoot(Hub.Accessor accessor, List<Root> extensions) {
            return new DefaultCoreRoot(accessor, extensions);
        }
        
    }
    
}
