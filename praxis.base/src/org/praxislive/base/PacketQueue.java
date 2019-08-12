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
package org.praxislive.base;

import java.util.List;
import java.util.PriorityQueue;
import org.praxislive.core.Packet;

/**
 *
 */
class PacketQueue {
    
    private final PriorityQueue<Packet> q;
    private long time;

    PacketQueue(long time) {
        q = new PriorityQueue<>(this::compare);
        this.time = time;
    }

    private int compare(Packet p1, Packet p2) {
        if (p1 == p2) {
            return 0;
        }
        long timeDiff = p1.time() - p2.time();
        if (timeDiff == 0) {
            int diff = p1.id() - p2.id();
            return diff < 0 ? -1 : 1;
        }
        return timeDiff < 0 ? -1 : 1;
    }

    void setTime(long time) {
        this.time = time;
    }

    long getTime() {
        return this.time;
    }

    void add(Packet packet) {
        q.add(packet);
    }

    Packet poll() {
        if (!q.isEmpty() && q.peek().time() - time <= 0) {
            return q.poll();
        }
        return null;
    }
    
    void drainTo(List<Packet> list) {
        list.addAll(q);
        q.clear();
    }
    
}
