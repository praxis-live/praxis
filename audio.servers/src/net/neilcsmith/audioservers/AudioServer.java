/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Neil C Smith.
 *
 * Copying and distribution of this file, with or without modification,
 * are permitted in any medium without royalty provided the copyright
 * notice and this notice are preserved.  This file is offered as-is,
 * without any warranty.
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
     * Get the current AudioConfiguration. This value should remain constant while the
     * server is processing audio.
     * @return AudioConfiguration
     */
    public AudioConfiguration getAudioContext();

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
