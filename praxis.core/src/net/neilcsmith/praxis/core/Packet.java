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
package net.neilcsmith.praxis.core;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Common superclass of Call and CallBundle
 * 
 * @author Neil C Smith
 */
public class Packet implements Comparable<Packet>, Serializable {

    private static AtomicLong idSource = new AtomicLong(0);

    private String rootID;
    private long timeCode;
    private long id;

    Packet(String rootID, long timeCode) {
        this.rootID = rootID;
        this.timeCode = timeCode;
        this.id = idSource.getAndIncrement();
    }

    /**
     * Get the time that this Packet should be processed, relative to System.nanoTime()
     *
     * @return long time
     */
    public final long getTimecode() {
        return timeCode;
    }

    /**
     * Get the ID of the Root component that this packet should be sent to.
     *
     * @return String Root ID
     */
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
    public final long getID() {
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
        long thisTime = getTimecode();
        long objTime = obj.getTimecode();
        if (thisTime == objTime) {
            long thisID = getID();
            long objID = obj.getID();
            if (thisID == objID) {
                // duplicate ids could happen with serialization
                return hashCode() < obj.hashCode() ? -1 : 1;
            }
            return thisID < objID ? -1 : 1;
        }
        return thisTime < objTime ? -1 : 1;
    }

}
