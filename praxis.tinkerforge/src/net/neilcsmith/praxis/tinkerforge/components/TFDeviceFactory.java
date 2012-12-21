/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2012 Neil C Smith.
 * 
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 3 only, as
 * published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 3 for more details.
 * 
 * You should have received a copy of the GNU General Public License version 3
 * along with this work; if not, see http://www.gnu.org/licenses/
 * 
 * 
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 */
package net.neilcsmith.praxis.tinkerforge.components;

import com.tinkerforge.BrickletAmbientLight;
import com.tinkerforge.BrickletDistanceIR;
import com.tinkerforge.BrickletLCD20x4;
import com.tinkerforge.BrickletRotaryPoti;
import com.tinkerforge.Device;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Neil C Smith
 */
class TFDeviceFactory {

    private final static TFDeviceFactory INSTANCE = new TFDeviceFactory();
    private Map<String, Class<? extends Device>> map;

    private TFDeviceFactory() {
        initMap();
    }

    private void initMap() {
        map = new HashMap<String, Class<? extends Device>>();
        map.put("LCD 20x4 Bricklet", BrickletLCD20x4.class);
        map.put("Rotary Poti Bricklet", BrickletRotaryPoti.class);
        map.put("Distance IR Bricklet", BrickletDistanceIR.class);
        map.put("Ambient Light Bricklet", BrickletAmbientLight.class);
        
    }

    Device createDevice(String name, String uid) throws Exception {
        
        Class<? extends Device> cls = findDeviceClass(name);
        return constructDevice(cls, uid);

    }

    private Class<? extends Device> findDeviceClass(String name) {
        int i = name.lastIndexOf(' ');
        if (i < 0) {
            throw new IllegalArgumentException("Illegal device name - no space found");
        }
        name = name.substring(0, i).replace('-', ' ');
        Class<? extends Device> cls = map.get(name);
        if (cls == null) {
            throw new IllegalArgumentException("Unregistered device name : " + name);
        }
        return cls;
    }
    
    private Device constructDevice(Class<? extends Device> cls, String uid) throws Exception {
        Constructor<? extends Device> con = cls.getConstructor(String.class);
        return con.newInstance(uid);
    }
    
    
    
    static TFDeviceFactory getDefault() {
        return INSTANCE;
    }
}
