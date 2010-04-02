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

/**
 *
 * @author Neil C Smith
 */
public interface Root extends Component {

//    public void routeCalls(CallPacket calls);

    public long submitTask(Task task, TaskListener listener) throws ServiceUnavailableException;

    public void addRootStateListener(RootStateListener listener);

    public void removeRootStateListener(RootStateListener listener);

    public long getTime();

    public void addControlFrameListener(ControlFrameListener listener);

    public void removeControlFrameListener(ControlFrameListener listener);
    
    public Root.Controller initialize(String ID, RootHub hub) throws IllegalRootStateException;
    
    public ServiceManager getServiceManager();
    
    public Lookup getLookup();
    
    public PacketRouter getPacketRouter();
    
    public Root.State getState();
    
    public static enum State {NEW, INITIALIZING, INITIALIZED, ACTIVE_IDLE, ACTIVE_RUNNING, TERMINATING, TERMINATED};
    
    public interface Controller {
        
        public boolean submitPacket(Packet packet);
        
        public void shutdown();
        
        public void run() throws IllegalRootStateException;
        
    }
    
}
