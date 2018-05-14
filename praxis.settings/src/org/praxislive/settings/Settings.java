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
package org.praxislive.settings;

/**
 *
 * @author nsigma
 */
public class Settings {

    private final static Provider provider = new DefaultProvider();

    public static String get(String key) {
        return provider.get(key);
    }

    public static String get(String key, String def) {
        return provider.get(key, def);
    }

    public static boolean getBoolean(String key, boolean def) {
        return provider.getBoolean(key, def);
    }
    
    public static int getInt(String key, int def) {
        return provider.getInt(key, def);
    }
    
    public static double getDouble(String key, double def) {
        return provider.getDouble(key, def);
    }
    
    public static void put(String key, String value) {
        put(key, value, true);
    }
    
    public static void put(String key, String value, boolean persistent) {
        provider.put(key, value, persistent);
    }
    
    public static void putBoolean(String key, boolean value) {
        putBoolean(key, value, true);
    }
    
    public static void putBoolean(String key, boolean value, boolean persistent) {
        provider.putBoolean(key, value, persistent);
    }
    
    public static void putInt(String key, int value) {
        putInt(key, value, true);
    }
    
    public static void putInt(String key, int value, boolean persistent) {
        provider.putInt(key, value, persistent);
    }
    
    public static void putDouble(String key, double value) {
        putDouble(key, value, true);
    }
    
    public static void putDouble(String key, double value, boolean persistent) {
        provider.putDouble(key, value, persistent);
    }

    public static abstract class Provider {

        public abstract String get(String key);

        public String get(String key, String def) {
            String ret = get(key);
            if (ret == null) {
                return def;
            } else {
                return ret;
            }
        }

        public boolean getBoolean(String key, boolean def) {
            boolean ret = def;
            String val = get(key);
            if (val != null) {
                if (val.equalsIgnoreCase("true")) {
                    ret = true;
                } else if (val.equalsIgnoreCase("false")) {
                    ret = false;
                }
            }
            return ret;
        }
        
        public int getInt(String key, int def) {
            int ret = def;
            try {
                String val = get(key);
                if (val != null) {
                    ret = Integer.parseInt(val);
                }
            } catch (NumberFormatException nfe) {
                // ignore
            }
            return ret;
        }
        
        public double getDouble(String key, double def) {
            double ret = def;
            try {
                String val = get(key);
                if (val != null) {
                    ret = Double.parseDouble(val);
                }
            } catch (NumberFormatException nfe) {
                // ignore
            }
            return ret;
        }
        
        public abstract void put(String key, String value, boolean persistent);
        
        public void putBoolean(String key, boolean value, boolean persistent) {
            put(key, String.valueOf(value), persistent);
        }
        
        public void putInt(String key, int value, boolean persistent) {
            put(key, String.valueOf(value), persistent);
        }
        
        public void putDouble(String key, double value, boolean persistent) {
            put(key, String.valueOf(value), persistent);
        }
        
        public abstract boolean isPersistent(String key);
        
    }
}
