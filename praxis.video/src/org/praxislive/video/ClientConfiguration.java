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
package org.praxislive.video;

import org.praxislive.core.Lookup;

/**
 *
 * @author nsigma
 */
public final class ClientConfiguration {

    private final int sourceCount;
    private final int sinkCount;
    private final Lookup lookup;
    
    public ClientConfiguration(int sourceCount, int sinkCount) {
        this(sourceCount, sinkCount, Lookup.EMPTY);
    }

    public ClientConfiguration(int sourceCount, int sinkCount, Lookup lookup) {

        if (sourceCount < 0) {
            throw new IllegalArgumentException();
        }
        if (sinkCount < 0) {
            throw new IllegalArgumentException();
        }
        this.sourceCount = sourceCount;
        this.sinkCount = sinkCount;
        this.lookup = lookup;
    }

    public int getSourceCount() {
        return sourceCount;
    }

    public int getSinkCount() {
        return sinkCount;
    }

    public Lookup getLookup() {
        return lookup;
    }

    public final static class Dimension {

        private final int width;
        private final int height;

        public Dimension(int width, int height) {
            if (width < 1 || height < 1) {
                throw new IllegalArgumentException("Illegal dimensions");
            }
            this.width = width;
            this.height = height;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 59 * hash + this.width;
            hash = 59 * hash + this.height;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Dimension other = (Dimension) obj;
            if (this.width != other.width) {
                return false;
            }
            if (this.height != other.height) {
                return false;
            }
            return true;
        }
    }

    public final static class Rotation {

        public final static Rotation DEG_0 = new Rotation(0);
        public final static Rotation DEG_90 = new Rotation(90);
        public final static Rotation DEG_180 = new Rotation(180);
        public final static Rotation DEG_270 = new Rotation(270);
        private final int angle;

        private Rotation(int angle) {
            this.angle = angle;
        }

        public int getAngle() {
            return angle;
        }

        @Override
        public int hashCode() {
            return angle;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Rotation other = (Rotation) obj;
            if (this.angle != other.angle) {
                return false;
            }
            return true;
        }
    }
    
//    public final static class DeviceID {
//        
//        private final String deviceID;
//        
//        public DeviceID(String deviceID) {
//            if (deviceID == null) {
//                throw new NullPointerException();
//            }
//            this.deviceID = deviceID;
//        }
//        
//        public String getValue() {
//            return deviceID;
//        }
//               
//    }
    
    public final static class DeviceIndex {
        
        private final int index;
        
        public DeviceIndex(int index) {
            this.index = index;
        }
        
        public int getValue() {
            return index;
        }

        @Override
        public int hashCode() {
            return index;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final DeviceIndex other = (DeviceIndex) obj;
            if (this.index != other.index) {
                return false;
            }
            return true;
        }
        
        
    }
}