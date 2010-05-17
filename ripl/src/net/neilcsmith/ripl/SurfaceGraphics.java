/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 - Neil C Smith. All rights reserved.
 * 
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details.
 * 
 * You should have received a copy of the GNU General Public License version 2
 * along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 */
package net.neilcsmith.ripl;

import java.awt.Composite;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.RenderingHints.Key;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;

/**
 *
 * @author Neil C Smith
 */
public abstract class SurfaceGraphics {

    public abstract void draw(Shape s);

    public abstract void fill(Shape s);

    public abstract void drawLine(int x1, int y1, int x2, int y2);

    public abstract void drawRect(int x, int y, int width, int height);

    public abstract void fillRect(int x, int y, int width, int height);

    public abstract void drawPolygon(Polygon p);

    public abstract void fillPolygon(Polygon p);

    public abstract void drawOval(int x, int y, int width, int height);

    public abstract void fillOval(int x, int y, int width, int height);

    public abstract void drawString(String str, int x, int y);

    public abstract void drawImage(Image img, int x, int y);

    public abstract void drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2);

    public abstract void drawSurface(Surface surface, int x, int y);

    public abstract void drawSurface(Surface surface, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2);

    public abstract void copyArea(int x, int y, int width, int height, int dx, int dy);

    public abstract void setComposite(Composite comp);

    public abstract void setPaint(Paint paint);

    public abstract void setStroke(Stroke s);

    public abstract void setFont(Font font);

    public abstract void translate(int x, int y);

    public abstract void rotate(double theta, double x, double y);

    public abstract void scale(double sx, double sy);

    public abstract void shear(double shx, double shy);

    public abstract void transform(AffineTransform Tx);

    public abstract void clip(Shape s);

    public abstract void clipRect(int x, int y, int width, int height);

//    public abstract GraphicsConfiguration getDeviceConfiguration();

    public abstract FontRenderContext getFontRenderContext();

    public abstract void reset();

    public abstract void setRenderingHint(Key key, Object hintValue);

    public abstract Object getRenderingHint(Key key);
}
