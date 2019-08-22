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
 *
 * Parts of the API of this package, as well as some of the code, is derived from
 * the Processing project (http://processing.org)
 *
 * Copyright (c) 2004-09 Ben Fry and Casey Reas
 * Copyright (c) 2001-04 Massachusetts Institute of Technology
 *
 */
package org.praxislive.video.pgl.code.userapi;

import processing.core.PConstants;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class Constants {

    private Constants() {
    }
    
    public static final BlendMode REPLACE = BlendMode.Replace;
    public static final BlendMode BLEND = BlendMode.Blend;
    public static final BlendMode ADD = BlendMode.Add;
    public static final BlendMode SUBTRACT = BlendMode.Subtract;
    public static final BlendMode MULTIPLY = BlendMode.Multiply;

    public static final DrawingMode CENTER = DrawingMode.Center;
    public static final DrawingMode CORNER = DrawingMode.Corner;
    public static final DrawingMode CORNERS = DrawingMode.Corners;
    public static final DrawingMode RADIUS = DrawingMode.Radius;
    
    //POINTS, Lines, TRIANGLES, TRIANGLE_FAN, TRIANGLE_STRIP, QUADS, or QUAD_STRIP
    public static final ShapeMode POINTS = ShapeMode.Points;
    public static final ShapeMode LINES = ShapeMode.Lines;
    public static final ShapeMode TRIANGLES = ShapeMode.Triangles;
    public static final ShapeMode TRIANGLE_FAN = ShapeMode.TriangleFan;
    public static final ShapeMode TRIANGLE_STRIP = ShapeMode.TriangleStrip;
    public static final ShapeMode QUADS = ShapeMode.Quads;
    public static final ShapeMode QUAD_STRIP = ShapeMode.QuadStrip;
    
    public static final TextureMode IMAGE = TextureMode.Image;
    public static final TextureMode NORMAL = TextureMode.Normal;
    
    public static final TextureWrap CLAMP = TextureWrap.Clamp;
    public static final TextureWrap REPEAT = TextureWrap.Repeat;
    
    public static final ShapeEndMode OPEN = ShapeEndMode.Open;
    public static final ShapeEndMode CLOSE = ShapeEndMode.Close;
    
    public static final ShapeType GROUP = ShapeType.Group;
    public static final ShapeType PATH = ShapeType.Path;
    public static final ShapeType GEOMETRY = ShapeType.Geometry;
    
    public static final ColorMode RGB = ColorMode.RGB;
    public static final ColorMode HSB = ColorMode.HSB;
    
    public static final String DEFAULT_VERTEX_SHADER = 
            ShaderConstants.DEFAULT_VERTEX_SHADER;
    public static final String DEFAULT_FRAGMENT_SHADER = 
            ShaderConstants.DEFAULT_FRAGMENT_SHADER;
    public static final String GLSL_VERTEX_MIME = "text/x-glsl-vert";
    public static final String GLSL_FRAGMENT_MIME = "text/x-glsl-frag";
    
    public static final Hint ENABLE_DEPTH_TEST = Hint.ENABLE_DEPTH_TEST;
    public static final Hint DISABLE_DEPTH_TEST = Hint.DISABLE_DEPTH_TEST;
    public static final Hint ENABLE_DEPTH_SORT = Hint.ENABLE_DEPTH_SORT;
    public static final Hint DISABLE_DEPTH_SORT = Hint.DISABLE_DEPTH_SORT;
    public static final Hint DISABLE_OPENGL_ERRORS = Hint.DISABLE_OPENGL_ERRORS;
    public static final Hint ENABLE_OPENGL_ERRORS = Hint.ENABLE_OPENGL_ERRORS;
    public static final Hint DISABLE_DEPTH_MASK = Hint.DISABLE_DEPTH_MASK;
    public static final Hint ENABLE_DEPTH_MASK = Hint.ENABLE_DEPTH_MASK;
    public static final Hint DISABLE_OPTIMIZED_STROKE = Hint.DISABLE_OPTIMIZED_STROKE;
    public static final Hint ENABLE_OPTIMIZED_STROKE = Hint.ENABLE_OPTIMIZED_STROKE;
    public static final Hint ENABLE_STROKE_PERSPECTIVE = Hint.ENABLE_STROKE_PERSPECTIVE;
    public static final Hint DISABLE_STROKE_PERSPECTIVE = Hint.DISABLE_STROKE_PERSPECTIVE;
    public static final Hint DISABLE_TEXTURE_MIPMAPS = Hint.DISABLE_TEXTURE_MIPMAPS;
    public static final Hint ENABLE_TEXTURE_MIPMAPS = Hint.ENABLE_TEXTURE_MIPMAPS;
    public static final Hint ENABLE_STROKE_PURE = Hint.ENABLE_STROKE_PURE;
    public static final Hint DISABLE_STROKE_PURE = Hint.DISABLE_STROKE_PURE;
    public static final Hint ENABLE_BUFFER_READING = Hint.ENABLE_BUFFER_READING;
    public static final Hint DISABLE_BUFFER_READING = Hint.DISABLE_BUFFER_READING;
    public static final Hint DISABLE_KEY_REPEAT = Hint.DISABLE_KEY_REPEAT;
    public static final Hint ENABLE_KEY_REPEAT = Hint.ENABLE_KEY_REPEAT;
    public static final Hint DISABLE_ASYNC_SAVEFRAME = Hint.DISABLE_ASYNC_SAVEFRAME;
    public static final Hint ENABLE_ASYNC_SAVEFRAME = Hint.ENABLE_ASYNC_SAVEFRAME;
    
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
    
    public static enum Hint {
        
        ENABLE_DEPTH_TEST(PConstants.ENABLE_DEPTH_TEST),
        DISABLE_DEPTH_TEST(PConstants.DISABLE_DEPTH_TEST),

        ENABLE_DEPTH_SORT(PConstants.ENABLE_DEPTH_SORT),
        DISABLE_DEPTH_SORT(PConstants.DISABLE_DEPTH_SORT),

        DISABLE_OPENGL_ERRORS(PConstants.DISABLE_OPENGL_ERRORS),
        ENABLE_OPENGL_ERRORS(PConstants.ENABLE_OPENGL_ERRORS),

        DISABLE_DEPTH_MASK(PConstants.DISABLE_DEPTH_MASK),
        ENABLE_DEPTH_MASK(PConstants.ENABLE_DEPTH_MASK),
        
        DISABLE_OPTIMIZED_STROKE(PConstants.DISABLE_OPTIMIZED_STROKE),
        ENABLE_OPTIMIZED_STROKE(PConstants.ENABLE_OPTIMIZED_STROKE),

        ENABLE_STROKE_PERSPECTIVE(PConstants.ENABLE_STROKE_PERSPECTIVE),
        DISABLE_STROKE_PERSPECTIVE(PConstants.DISABLE_STROKE_PERSPECTIVE),

        DISABLE_TEXTURE_MIPMAPS(PConstants.DISABLE_TEXTURE_MIPMAPS),
        ENABLE_TEXTURE_MIPMAPS(PConstants.ENABLE_TEXTURE_MIPMAPS),
        
        ENABLE_STROKE_PURE(PConstants.ENABLE_STROKE_PURE),
        DISABLE_STROKE_PURE(PConstants.DISABLE_STROKE_PURE),
        
        ENABLE_BUFFER_READING(PConstants.ENABLE_BUFFER_READING),
        DISABLE_BUFFER_READING(PConstants.DISABLE_BUFFER_READING),
        
        DISABLE_KEY_REPEAT(PConstants.DISABLE_KEY_REPEAT),
        ENABLE_KEY_REPEAT(PConstants.ENABLE_KEY_REPEAT),
        
        DISABLE_ASYNC_SAVEFRAME(PConstants.DISABLE_ASYNC_SAVEFRAME),
        ENABLE_ASYNC_SAVEFRAME(PConstants.ENABLE_ASYNC_SAVEFRAME);
        
        private final int value;
        
        private Hint(int value) {
            this.value = value;
        }
        
        public int unwrap() {
            return value;
        }
        
    }
    
}
