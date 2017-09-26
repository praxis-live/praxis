/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Neil C Smith.
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
 *
 *
 * Parts of the API of this package, as well as some of the code, is derived from
 * the Processing project (http://processing.org)
 *
 * Copyright (c) 2004-09 Ben Fry and Casey Reas
 * Copyright (c) 2001-04 Massachusetts Institute of Technology
 *
 */
package net.neilcsmith.praxis.video.pgl.code.userapi;

import processing.core.PConstants;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class Constants {

    private Constants() {
    }
    
    public final static BlendMode REPLACE = BlendMode.Replace;
    public final static BlendMode BLEND = BlendMode.Blend;
    public final static BlendMode ADD = BlendMode.Add;
    public final static BlendMode SUBTRACT = BlendMode.Subtract;
    public final static BlendMode MULTIPLY = BlendMode.Multiply;

    public final static DrawingMode CENTER = DrawingMode.Center;
    public final static DrawingMode CORNER = DrawingMode.Corner;
    public final static DrawingMode CORNERS = DrawingMode.Corners;
    public final static DrawingMode RADIUS = DrawingMode.Radius;
    
    //POINTS, Lines, TRIANGLES, TRIANGLE_FAN, TRIANGLE_STRIP, QUADS, or QUAD_STRIP
    public final static ShapeMode POINTS = ShapeMode.Points;
    public final static ShapeMode LINES = ShapeMode.Lines;
    public final static ShapeMode TRIANGLES = ShapeMode.Triangles;
    public final static ShapeMode TRIANGLE_FAN = ShapeMode.TriangleFan;
    public final static ShapeMode TRIANGLE_STRIP = ShapeMode.TriangleStrip;
    public final static ShapeMode QUADS = ShapeMode.Quads;
    public final static ShapeMode QUAD_STRIP = ShapeMode.QuadStrip;
    
    public final static TextureMode IMAGE = TextureMode.Image;
    public final static TextureMode NORMAL = TextureMode.Normal;
    
    public final static TextureWrap CLAMP = TextureWrap.Clamp;
    public final static TextureWrap REPEAT = TextureWrap.Repeat;
    
    public final static ShapeEndMode OPEN = ShapeEndMode.Open;
    public final static ShapeEndMode CLOSE = ShapeEndMode.Close;
    
    public final static ShapeType GROUP = ShapeType.Group;
    public final static ShapeType PATH = ShapeType.Path;
    public final static ShapeType GEOMETRY = ShapeType.Geometry;
    
    public final static ColorMode RGB = ColorMode.RGB;
    public final static ColorMode HSB = ColorMode.HSB;
    
    public final static String DEFAULT_VERTEX_SHADER = 
            ShaderConstants.DEFAULT_VERTEX_SHADER;
    public final static String DEFAULT_FRAGMENT_SHADER = 
            ShaderConstants.DEFAULT_FRAGMENT_SHADER;
    public final static String GLSL_VERTEX_MIME = "text/x-glsl-vert";
    public final static String GLSL_FRAGMENT_MIME = "text/x-glsl-frag";

    public static enum BlendMode {

        Replace(PConstants.REPLACE),
        Blend(PConstants.BLEND),
        Add(PConstants.ADD),
        Subtract(PConstants.SUBTRACT),
        Multiply(PConstants.MULTIPLY);

        private final int value;

        private BlendMode(int value) {
            this.value = value;
        }

        public int unwrap() {
            return value;
        }

    };
    
    public static enum DrawingMode {

        Center(PConstants.CENTER),
        Corner(PConstants.CORNER),
        Corners(PConstants.CORNERS),
        Radius(PConstants.RADIUS);
        
        private final int value;
        
        private DrawingMode(int value) {
            this.value = value;
        }
        
        public int unwrap() {
            return value;
        }
        
    };

    public static enum ShapeMode {
        Points(PConstants.POINTS),
        Lines(PConstants.LINES),
        Triangles(PConstants.TRIANGLES),
        TriangleFan(PConstants.TRIANGLE_FAN),
        TriangleStrip(PConstants.TRIANGLE_STRIP),
        Quads(PConstants.QUADS),
        QuadStrip(PConstants.QUAD_STRIP);
        
        private final int value;
        
        private ShapeMode(int value) {
            this.value = value;
        }
        
        public int unwrap() {
            return value;
        }
        
    }
    
    public static enum TextureMode {
        Image(PConstants.IMAGE),
        Normal(PConstants.NORMAL);
        
        private final int value;
        
        private TextureMode(int value) {
            this.value = value;
        }
        
        public int unwrap() {
            return value;
        }
    }
    
    public static enum TextureWrap {
        Clamp(PConstants.CLAMP),
        Repeat(PConstants.REPEAT);
        
        private final int value;
        
        private TextureWrap(int value) {
            this.value = value;
        }
        
        public int unwrap() {
            return value;
        }
    }
    
    public static enum ShapeEndMode {
        Open(PConstants.OPEN),
        Close(PConstants.CLOSE);
        
        private final int value;
        
        private ShapeEndMode(int value) {
            this.value = value;
        }
        
        public int unwrap() {
            return value;
        }
    }
    
    public static enum ShapeType {
        Group(PConstants.GROUP),
        Path(processing.core.PShape.PATH),
        Geometry(processing.core.PShape.GEOMETRY);
        
        private final int value;
        
        private ShapeType(int value) {
            this.value = value;
        }
        
        public int unwrap() {
            return value;
        }
    }
    
    public static enum ColorMode {
        RGB(PConstants.RGB),
        HSB(PConstants.HSB);
        
        private final int value;
        
        private ColorMode(int value) {
            this.value = value;
        }
        
        public int unwrap() {
            return value;
        }
    }
    
}
