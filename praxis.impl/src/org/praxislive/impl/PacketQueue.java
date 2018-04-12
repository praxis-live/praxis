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
package org.praxislive.impl;

import java.util.PriorityQueue;
import org.praxislive.core.Packet;

/**
 *
 * @author Neil C Smith
 */
public class PacketQueue {

    private final static long NANO_PER_SEC = 1000000000L;
    private final static long UPPER_OVERFLOW_BOUNDARY = Long.MAX_VALUE - (3600 * NANO_PER_SEC);
    private final static long LOWER_OVERFLOW_BOUNDARY = Long.MIN_VALUE + (3600 * NANO_PER_SEC);
    private PriorityQueue<Packet> primaryQueue;
    private PriorityQueue<Packet> secondaryQueue;
    private long time = 0L;
    private boolean inOverflowArea = false;

    public PacketQueue() {
        primaryQueue = new PriorityQueue<Packet>();
        secondaryQueue = new PriorityQueue<Packet>(1);
//        System.out.println("Upper overflow boundary is " + UPPER_OVERFLOW_BOUNDARY);
//        System.out.println("Lower overflow boundary is " + LOWER_OVERFLOW_BOUNDARY);
    }

    public void setTime(long time) {
        this.time = time;
        if (inOverflowArea) {
            if (time > LOWER_OVERFLOW_BOUNDARY && time < UPPER_OVERFLOW_BOUNDARY) {
                // drainSecondaryToPrimary();
                primaryQueue.addAll(secondaryQueue);
                secondaryQueue.clear();
                inOverflowArea = false;
//                System.out.println("Setting overflow area false");
            }
        } else {
            if (time > UPPER_OVERFLOW_BOUNDARY || time < LOWER_OVERFLOW_BOUNDARY) {
                // drainPrimaryToSecondary();
                secondaryQueue.addAll(primaryQueue);
                primaryQueue.clear();
                inOverflowArea = true;
//                System.out.println("Setting overflow area true");
            }
        }
    }

    public long getTime() {
        return this.time;
    }

    public void add(Packet packet) {
        if (inOverflowArea) {
            if (packet.getTimecode() > 0) {
                secondaryQueue.add(packet);
            } else {
                primaryQueue.add(packet);
            }
        } else {
            primaryQueue.add(packet);
        }
    }

    public Packet poll() {
        if (inOverflowArea) {
            return checkedPoll();
        } else if (!primaryQueue.isEmpty() && primaryQueue.peek().getTimecode() < time) {
            return primaryQueue.poll();
        } else {
            return null;
        }

    }

    private Packet checkedPoll() {
        if (time > 0) {
            if (!secondaryQueue.isEmpty() && secondaryQueue.peek().getTimecode() < time) {
                return secondaryQueue.poll();
            } else {
                return null;
            }
        } else {
            if (!secondaryQueue.isEmpty()) {
                return secondaryQueue.poll();
            } else if (!primaryQueue.isEmpty() && primaryQueue.peek().getTimecode() < time) {
                return primaryQueue.poll();
            } else {
                return null;
            }
        }
    }
//    private void drainPrimaryToSecondary() {
//        secondaryQueue.addAll(primaryQueue);
//    }
//
//    private void drainSecondaryToPrimary() {
//        primaryQueue.addAll(secondaryQueue);
//    }
}
