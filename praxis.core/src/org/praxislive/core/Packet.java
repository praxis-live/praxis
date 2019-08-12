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
 *
 */
package org.praxislive.core;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Superclass of Call. Intended to allow future support for atomic Call bundles.
 * 
 * @author Neil C Smith
 */
public class Packet implements Comparable<Packet> {

    private static final AtomicInteger ID_SOURCE = new AtomicInteger(0);

    private final String rootID;
    private final long timeCode;
    private final int id;

    Packet(String rootID, long timeCode) {
        this.rootID = rootID;
        this.timeCode = timeCode;
        this.id = ID_SOURCE.getAndIncrement();
    }

    /**
     * Get the time that this Packet should be processed, relative to {@link RootHub#getClock() }
     *
     * @return long time
     */
    public final long time() {
        return timeCode;
    }
    
    /**
     * Get the time that this Packet should be processed, relative to {@link RootHub#getClock() }
     *
     * @return long time
     */
    @Deprecated
    public final long getTimecode() {
        return timeCode;
    }

    /**
     * Get the ID of the Root component that this packet should be sent to.
     *
     * @return String Root ID
     */
    public final String rootID() {
        return rootID;
    }
    /**
     * Get the ID of the Root component that this packet should be sent to.
     *
     * @return String Root ID
     */
    @Deprecated
    public final String getRootID() {
        return rootID;
    }

    /**
     * Get the ID of this Packet.
     *
     * This should not be used to test equality as it may not be unique if the
     * Packet has been serialized.
     *
     * @return long ID
     */
    public final int id() {
        // should not be used for object equality
        // @TODO Change ID semantics to maintain uniqueness?
        return id;
    }
    /**
     * Get the ID of this Packet.
     *
     * This should not be used to test equality as it may not be unique if the
     * Packet has been serialized.
     *
     * @return long ID
     */
    @Deprecated
    public final int getID() {
        // should not be used for object equality
        // @TODO Change ID semantics to maintain uniqueness?
        return id;
    }

 /**
     * Compare Packets and order them by timeCode and ID.
     *
     * @param obj
     * @return int
     */
    public final int compareTo(Packet obj) {
        if (this == obj) {
            return 0;
        }
        long thisTime = time();
        long objTime = obj.time();
        if (thisTime == objTime) {
            long thisID = id();
            long objID = obj.id();
            if (thisID == objID) {
                // duplicate ids could happen with serialization
                return hashCode() < obj.hashCode() ? -1 : 1;
            }
            return thisID < objID ? -1 : 1;
        }
        return thisTime < objTime ? -1 : 1;
    }

}
