/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Neil C Smith. All rights reserved.
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
 *
 */

package net.neilcsmith.audioservers;

/**
 * A base interface for classes providing a server to run AudioClients. It is up
 * to implementations how the AudioClient is connected to the server - this will
 * usually happen at creation time.
 *
 * AudioServers provide a run() method as it is intended that the application should
 * provide the Thread in which the server will run, however they do not extend
 * Runnable as the run() method throws an Exception, forcing the application to
 * deal with any problems starting the server.
 *
 * @author Neil C Smith
 */
public interface AudioServer {
    
    /**
     * Start and run the audio server in the current thread.
     * @throws Exception
     */
    public void run() throws Exception;

    /**
     * Get the current AudioContext. This value should remain constant while the
     * server is processing audio.
     * @return AudioContext
     */
    public AudioContext getAudioContext();

    /**
     * Check whether the server is active. This method can be called from another
     * thread.
     * @return true if active.
     */
    public boolean isActive();

    /**
     * Trigger the server to shut down. This method can be called from another
     * thread, but does not guarantee that the server is shut down at the moment
     * it returns.
     */
    public void shutdown();

}
