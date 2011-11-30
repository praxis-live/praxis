/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2011 Neil C Smith.
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
package net.neilcsmith.praxis.video;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author nsigma
 */
public class ClientConfiguration {
    
    public final static String CLIENT_KEY_WIDTH = "client.width";
    public final static String CLIENT_KEY_HEIGHT = "client.height";
    public final static String CLIENT_KEY_ROTATION = "client.rotation";
    public final static String CLIENT_KEY_DEVICE = "client.device";
    public final static String CLIENT_KEY_FULLSCREEN = "client.fullscreen";
    public final static String CLIENT_KEY_TITLE = "client.title";
    
    private final int sourceCount;
    private final int sinkCount;
    private final Map<String, Object> hints;

    
    public ClientConfiguration(int sourceCount, int sinkCount, Map<String, Object> hints) {
        if (sourceCount < 0) {
            throw new IllegalArgumentException();
        }
        if (sinkCount < 0) {
            throw new IllegalArgumentException();
        }
        this.sourceCount = sourceCount;
        this.sinkCount = sinkCount;
        if (hints == null) {
            this.hints = Collections.emptyMap();
        } else {
            this.hints = new ConcurrentHashMap<String, Object>(hints);
        }
    }
    
    public int getSourceCount() {
        return sourceCount;
    }
    
    public int getSinkCount() {
        return sinkCount;
    }

    public Object getHint(String key) {
        return hints.get(key);
    }
    
}
