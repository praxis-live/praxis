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
package net.neilcsmith.praxis.core;

import net.neilcsmith.praxis.core.interfaces.ServiceManager;


/**
 *
 * @author Neil C Smith
 */
public interface Root extends Component {

//    public void routeCalls(CallPacket calls);

    //@Deprecated
    //public long submitTask(Task task, TaskListener listener) throws ServiceUnavailableException;

//    @Deprecated
//    public void addRootStateListener(RootStateListener listener);
//
//    @Deprecated
//    public void removeRootStateListener(RootStateListener listener);
//
//    @Deprecated
//    public long getTime();

//    @Deprecated
//    public void addControlFrameListener(ControlFrameListener listener);
//
//    @Deprecated
//    public void removeControlFrameListener(ControlFrameListener listener);
    
    public Root.Controller initialize(String ID, RootHub hub) throws IllegalRootStateException;

    @Deprecated
    public ServiceManager getServiceManager();

    @Deprecated
    public Lookup getLookup();

    @Deprecated
    public PacketRouter getPacketRouter();
    
    public Root.State getState();
    
    public static enum State {NEW, INITIALIZING, INITIALIZED, ACTIVE_IDLE, ACTIVE_RUNNING, TERMINATING, TERMINATED};
    
    public interface Controller {
        
        public boolean submitPacket(Packet packet);
        
        public void shutdown();
        
        public void run() throws IllegalRootStateException;
        
    }
    
}
