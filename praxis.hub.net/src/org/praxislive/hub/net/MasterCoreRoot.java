/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2016 Neil C Smith.
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
package org.praxislive.hub.net;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.praxislive.core.Call;
import org.praxislive.core.CallArguments;
import org.praxislive.core.ComponentAddress;
import org.praxislive.core.ComponentType;
import org.praxislive.core.ControlAddress;
import org.praxislive.core.IllegalRootStateException;
import org.praxislive.core.InvalidAddressException;
import org.praxislive.core.Root;
import org.praxislive.core.ControlInfo;
import org.praxislive.core.interfaces.RootFactoryService;
import org.praxislive.core.interfaces.RootManagerService;
import org.praxislive.core.types.PArray;
import org.praxislive.core.types.PReference;
import org.praxislive.core.types.PString;
import org.praxislive.hub.DefaultCoreRoot;
import org.praxislive.hub.Hub;
import org.praxislive.impl.AbstractAsyncControl;
import org.praxislive.impl.SimpleControl;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
class MasterCoreRoot extends DefaultCoreRoot {

    private final static Logger LOG = Logger.getLogger(MasterCoreRoot.class.getName());
    private final static String SLAVE_PREFIX = Hub.SYS_PREFIX + "net_";

    private final Map<String, String> remotes;
    private final SlaveInfo[] slaves;
    
    private ComponentAddress[] slaveClients;
    private FileServer fileServer;
    
    MasterCoreRoot(Hub.Accessor hubAccess,
            List<Root> exts,
            List<? extends SlaveInfo> slaves) {
        super(hubAccess, exts);
        this.slaves = slaves.toArray(new SlaveInfo[slaves.size()]);
        this.remotes = new HashMap<>();
    }

    @Override
    protected void createRootManagerService() {
        registerControl(RootManagerService.ADD_ROOT, new AddRootControl());
        registerControl(RootManagerService.REMOVE_ROOT, new RemoveRootControl());
        registerControl(RootManagerService.ROOTS, new RootsControl());
        registerInterface(RootManagerService.class);
        getHubAccessor().registerService(RootManagerService.class, getAddress());
    }

    @Override
    protected void activating() {
        super.activating();
        FileServer.Info fileServerInfo = activateFileServer();
        slaveClients = new ComponentAddress[slaves.length];
        for (int i = 0; i < slaves.length; i++) {
            String id = SLAVE_PREFIX + (i + 1);
            try {
                installRoot(id, "netex", new MasterClientRoot(slaves[i], fileServerInfo));
            } catch (InvalidAddressException | IllegalRootStateException ex) {
                Logger.getLogger(MasterCoreRoot.class.getName()).log(Level.SEVERE, null, ex);
//                throw new RuntimeException(ex);
            }
            slaveClients[i] = ComponentAddress.create("/" + id);
        }
    }

    @Override
    protected void terminating() {
        super.terminating(); 
        if (fileServer != null) {
            fileServer.stop();
            fileServer = null;
        }
    }

    private FileServer.Info activateFileServer() {
        boolean reqServer = false;
        for (SlaveInfo slave : slaves) {
            if (!slave.isLocal() && slave.getUseRemoteResources()) {
                reqServer = true;
                break;
            }
        }
        
        if (reqServer) {
            try {
                fileServer = new FileServer(Utils.getFileServerPort(), Utils.getUserDirectory());
                fileServer.start();
                return fileServer.getInfo();
            } catch (IOException ex) {
                Logger.getLogger(MasterCoreRoot.class.getName()).log(Level.SEVERE, null, ex);
                fileServer = null;
            }
        }
        
        return null;
    }

    private class AddRootControl extends AbstractAsyncControl {

        @Override
        protected Call processInvoke(Call call) throws Exception {
            CallArguments args = call.getArgs();
            if (args.getSize() < 2) {
                throw new IllegalArgumentException("Invalid arguments");
            }
            String id = args.get(0).toString();
            if (!ComponentAddress.isValidID(id)) {
                throw new IllegalArgumentException("Invalid Component ID");
            }
            ComponentType type = ComponentType.coerce(args.get(1));

            ComponentAddress proxy = null;
            for (int i = 0; i < slaves.length; i++) {
                if (slaves[i].matches(id, type)) {
                    proxy = slaveClients[i];
                }
            }

            ControlAddress to;
            if (proxy != null) {
                to = ControlAddress.create(proxy, RootManagerService.ADD_ROOT);
                return Call.createCall(to, getAddress(), call.getTimecode(), args);
            } else {
                to = ControlAddress.create(
                        findService(RootFactoryService.class),
                        RootFactoryService.NEW_ROOT_INSTANCE);
                return Call.createCall(to, getAddress(), call.getTimecode(), args.get(1));
            }
        }

        @Override
        protected Call processResponse(Call call) throws Exception {
            Call active = getActiveCall();
            String id = active.getArgs().get(0).toString();
            String source = call.getFromAddress().getComponentAddress().getRootID();
            if (source.startsWith(SLAVE_PREFIX)) {
                Root.Controller ctrl = getHubAccessor().getRootController(source);
                getHubAccessor().registerRootController(id, ctrl);
                remotes.put(id, source);
            } else {
                CallArguments args = call.getArgs();
                if (args.getSize() < 1) {
                    throw new IllegalArgumentException("Invalid response");
                }
                Root r = (Root) ((PReference) args.get(0)).getReference();
                String type = active.getArgs().get(1).toString();
                installRoot(id, type, r);
            }
            return Call.createReturnCall(active, CallArguments.EMPTY);
        }

        @Override
        public ControlInfo getInfo() {
            return RootManagerService.ADD_ROOT_INFO;
        }

    }

    private class RemoveRootControl extends AbstractAsyncControl {

        @Override
        protected Call processInvoke(Call call) throws Exception {
            String id = call.getArgs().get(0).toString();
            String remoteProxy = remotes.get(id);
            if (remoteProxy != null) {
                ControlAddress to = ControlAddress.create(
                        ComponentAddress.create("/" + remoteProxy),
                        RootManagerService.REMOVE_ROOT);
                return Call.createCall(to, getAddress(), call.getTimecode(), call.getArgs());
            } else {
                uninstallRoot(id);
                return Call.createReturnCall(call, CallArguments.EMPTY);
            }
        }

        @Override
        protected Call processResponse(Call call) throws Exception {
            Call active = getActiveCall();
            String id = active.getArgs().get(0).toString();
            remotes.remove(id);
            return Call.createReturnCall(active, CallArguments.EMPTY);
        }

        @Override
        public ControlInfo getInfo() {
            return RootManagerService.REMOVE_ROOT_INFO;
        }

    }

     private class RootsControl extends SimpleControl {

        private String[] knownIDs;
        private PArray ret;
        
        private RootsControl() {
            super(RootManagerService.ROOTS_INFO);
            knownIDs = new String[0];
            ret = PArray.EMPTY;
        }

        @Override
        protected CallArguments process(long time, CallArguments args, boolean quiet) throws Exception {
            String[] ids = getHubAccessor().getRootIDs();
            if (!Arrays.equals(ids, knownIDs)) {
                knownIDs = ids;
                List<PString> list = new ArrayList<>(ids.length);
                for (String id : ids) {
                    list.add(PString.valueOf(id));
                }
                ret = PArray.valueOf(list);
            }
            return CallArguments.create(ret);
        }



    }

}
