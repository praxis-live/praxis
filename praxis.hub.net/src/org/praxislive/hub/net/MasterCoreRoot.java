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
package org.praxislive.hub.net;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.praxislive.base.AbstractAsyncControl;
import org.praxislive.core.Call;
import org.praxislive.core.ComponentAddress;
import org.praxislive.core.ComponentType;
import org.praxislive.core.Control;
import org.praxislive.core.ControlAddress;
import org.praxislive.core.Root;
import org.praxislive.core.Value;
import org.praxislive.core.services.RootFactoryService;
import org.praxislive.core.services.RootManagerService;
import org.praxislive.core.types.PReference;
import org.praxislive.hub.BasicCoreRoot;
import org.praxislive.hub.Hub;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
class MasterCoreRoot extends BasicCoreRoot {

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
    protected void buildControlMap(Map<String, Control> ctrls) {
        ctrls.put(RootManagerService.ADD_ROOT, new AddRootControl());
        ctrls.put(RootManagerService.REMOVE_ROOT, new RemoveRootControl());
        super.buildControlMap(ctrls);
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
            } catch (Exception ex) {
                Logger.getLogger(MasterCoreRoot.class.getName()).log(Level.SEVERE, null, ex);
//                throw new RuntimeException(ex);
            }
            slaveClients[i] = ComponentAddress.of("/" + id);
        }
        setRunning();
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
            List<Value> args = call.args();
            if (args.size() < 2) {
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
                to = ControlAddress.of(proxy, RootManagerService.ADD_ROOT);
                return Call.create(to, call.to(), call.time(), args);
            } else {
                to = ControlAddress.of(findService(RootFactoryService.class),
                        RootFactoryService.NEW_ROOT_INSTANCE);
                return Call.create(to, call.to(), call.time(), args.get(1));
            }
        }

        @Override
        protected Call processResponse(Call call) throws Exception {
            Call active = getActiveCall();
            String id = active.getArgs().get(0).toString();
            String source = call.from().component().rootID();
            if (source.startsWith(SLAVE_PREFIX)) {
                Root.Controller ctrl = getHubAccessor().getRootController(source);
                getHubAccessor().registerRootController(id, ctrl);
                remotes.put(id, source);
            } else {
                List<Value> args = call.args();
                if (args.size() < 1) {
                    throw new IllegalArgumentException("Invalid response");
                }
                Root r = (Root) ((PReference) args.get(0)).getReference();
                String type = active.getArgs().get(1).toString();
                installRoot(id, type, r);
            }
            return active.reply();
        }

    }

    private class RemoveRootControl extends AbstractAsyncControl {

        @Override
        protected Call processInvoke(Call call) throws Exception {
            String id = call.args().get(0).toString();
            String remoteProxy = remotes.get(id);
            if (remoteProxy != null) {
                ControlAddress to = ControlAddress.of(
                        ComponentAddress.of("/" + remoteProxy),
                        RootManagerService.REMOVE_ROOT);
                return Call.create(to, call.to(), call.time(), call.args());
            } else {
                uninstallRoot(id);
                return call.reply();
            }
        }

        @Override
        protected Call processResponse(Call call) throws Exception {
            Call active = getActiveCall();
            String id = active.args().get(0).toString();
            remotes.remove(id);
            return call.reply();
        }

    }

}
