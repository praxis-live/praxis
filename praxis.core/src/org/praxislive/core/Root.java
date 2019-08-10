/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2018 Neil C Smith.
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
package org.praxislive.core;

import java.util.concurrent.ThreadFactory;

/**
 * Root provides the companion part of the actor-model to Component within 
 * PraxisCORE's forest-of-actors model. The Root implementation handles initial
 * message handling and scheduling. There may be many Roots within a running
 * PraxisCORE system - the Roots are sandboxed from each other and the RootHub
 * handles delivery of messages (Packet / Call) from one Root to another.
 * 
 * A Root may be a Component or Container, but this is not required. As
 * Component implementations are intended to be lock-free and single-threaded,
 * the Root implementation will ensure that all messages are handled serially.
 * Some Root implementations will have a one-to-one relationship to a thread of
 * execution.
 * 
 * @author Neil C Smith
 */
public interface Root extends Lookup.Provider { 
    
    /**
     * Method used by the RootHub to initialize the Root and obtain a Controller.
     * Root implementations will ensure this method can only be invoked once.
     * 
     * @param ID the unique ID of this Root
     * @param hub the RootHub the Root resides within
     * @return Controller for use by the RootHub instance
     * @throws IllegalStateException if the Root has already been initialized
     */
    public Root.Controller initialize(String ID, RootHub hub);
    
    /**
     * An interface used by the RootHub to control the lifecycle of, and
     * communicate with, the Root.
     */
    public interface Controller {
        
        /**
         * Deliver a Packet to this Root. This method is intended to be called
         * from a thread other than the primary thread of the Root. It will add
         * the packet to a queue and return immediately - this method will never
         * block as it may be called from the thread of another Root.
         * 
         * This method will return true if the Packet can be handled (see eg.
         * BlockingQueue::offer)
         * 
         * @param packet message (see Packet / Call) to handle
         * @return true if the packet can be handled
         */
        public boolean submitPacket(Packet packet);
        
        /**
         * Start the Root. If the Root implementation requires a primary thread
         * to run on it will use the supplied ThreadFactory so that the RootHub
         * can manage thread creation. The ThreadFactory is not required to
         * support the creation of more than one Thread.
         * 
         * Controller implementations will ensure that this method can only be
         * invoked once.
         * 
         * @param threadFactory used if the Root requires a thread to run on.
         * @throws IllegalStateException if the Root has already been started.
         */
        public void start(ThreadFactory threadFactory);
        
        /**
         * Signal the Root to be shutdown. This method is intended to be called
         * asynchronously and will return immediately - it will not wait for the
         * Root to actually complete execution.
         */
        public void shutdown();
        
    }
    
}
