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
package org.praxislive.video.render.utils;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class FontUtils {
    
    private final static Map<URI, Font> cache = new HashMap<>();
    
    private FontUtils() {}
    
    public synchronized static Font load(URI location) throws FontFormatException, IOException {
        Font font = cache.get(location);
        if (font == null) {
            font = Font.createFont(Font.TRUETYPE_FONT, location.toURL().openStream());
            cache.put(location, font);
        }
        return font;
    }
    
}
