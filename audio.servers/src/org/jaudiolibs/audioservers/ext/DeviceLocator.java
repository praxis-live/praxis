/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Neil C Smith.
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
package org.jaudiolibs.audioservers.ext;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class DeviceLocator {
    
    public final static DeviceLocator DEFAULT = new DeviceLocator("");
    
    private String device;
    
    public DeviceLocator(String device) {
        if (device == null) {
            throw new NullPointerException();
        }
        this.device = device;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DeviceLocator) {
            return ((DeviceLocator)obj).device.equals(device);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return device.hashCode();
    }
    
    
    
}
