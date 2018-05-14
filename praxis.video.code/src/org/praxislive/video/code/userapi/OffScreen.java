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
 *
 */
package org.praxislive.video.code.userapi;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotate a PGraphics field as an additional offscreen buffer.
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface OffScreen {
    
    /**
     * Provide a fixed width for the offscreen buffer. The default value of zero
     * will cause the buffer width to be relative to the current output surface
     * width.
     * 
     * @return width in pixels
     */
    public int width() default 0;

    /**
     * Provide a fixed height for the offscreen buffer. The default value of zero
     * will cause the buffer height to be relative to the current output surface
     * height.
     * 
     * @return height in pixels
     */
    public int height() default 0;

    /**
     * Provide a scaling factor for the width. Default value is 1.0
     * 
     * @return scale (1.0 is identity)
     */
    public double scaleWidth() default 1;
    
    /**
     * Provide a scaling factor for the height. Default value is 1.0
     * 
     * @return scale (1.0 is identity)
     */
    public double scaleHeight() default 1;

    /**
     * Whether to persist the contents of the offscreen buffer between frames.
     * Default value is true. If false the surface will be released after each
     * draw() call.
     * 
     * @return persist contents between frames
     */
    public boolean persistent() default true;
    
    /**
     * The pixel format of the buffer. The default value of {@link Format#Default}
     * will match the output surface.
     * 
     * @return format (Default, RGB, ARGB)
     */
    public Format format() default Format.Default;
    
    /**
     * Pixel format of the buffer.
     */
    public static enum Format {
        Default, RGB, ARGB;
    }
    
}
