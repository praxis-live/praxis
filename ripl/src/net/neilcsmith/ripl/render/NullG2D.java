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

package net.neilcsmith.ripl.render;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;

/**
 *
 * @author Neil C Smith
 */
class NullG2D extends Graphics2D {
    
    private final static NullG2D instance = new NullG2D();
    
    public static NullG2D getInstance() {
        return instance;
    }

    private NullG2D() {
        
    }
    
    @Override
    public void draw(Shape s) {
        // no op
    }

    @Override
    public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
        return true;
    }

    @Override
    public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
        // no op
    }

    @Override
    public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
        // no op
    }

    @Override
    public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
        // no op
    }

    @Override
    public void drawString(String str, int x, int y) {
        // no op
    }

    @Override
    public void drawString(String str, float x, float y) {
        // no op
    }

    @Override
    public void drawString(AttributedCharacterIterator iterator, int x, int y) {
        // no op
    }

    @Override
    public void drawString(AttributedCharacterIterator iterator, float x, float y) {
        // no op
    }

    @Override
    public void drawGlyphVector(GlyphVector g, float x, float y) {
        // no op
    }

    @Override
    public void fill(Shape s) {
        // no op
    }

    @Override
    public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
        return false;
    }

    @Override
    public GraphicsConfiguration getDeviceConfiguration() {
        return null;
    }

    @Override
    public void setComposite(Composite comp) {
        // no op
    }

    @Override
    public void setPaint(Paint paint) {
        // no op
    }

    @Override
    public void setStroke(Stroke s) {
        // no op
    }

    @Override
    public void setRenderingHint(Key hintKey, Object hintValue) {
        // no op
    }

    @Override
    public Object getRenderingHint(Key hintKey) {
        return null;
    }

    @Override
    public void setRenderingHints(Map<?, ?> hints) {
        // no op
    }

    @Override
    public void addRenderingHints(Map<?, ?> hints) {
        // no op
    }

    @Override
    public RenderingHints getRenderingHints() {
        return new RenderingHints(null);
    }

    @Override
    public void translate(int x, int y) {
        // no op
    }

    @Override
    public void translate(double tx, double ty) {
        // no op
    }

    @Override
    public void rotate(double theta) {
        // no op
    }

    @Override
    public void rotate(double theta, double x, double y) {
        // no op
    }

    @Override
    public void scale(double sx, double sy) {
        // no op
    }

    @Override
    public void shear(double shx, double shy) {
        // no op
    }

    @Override
    public void transform(AffineTransform Tx) {
        // no op
    }

    @Override
    public void setTransform(AffineTransform Tx) {
        // no op
    }

    @Override
    public AffineTransform getTransform() {
        return new AffineTransform();
    }

    @Override
    public Paint getPaint() {
        return Color.BLACK;
    }

    @Override
    public Composite getComposite() {
        return AlphaComposite.SrcOver;
    }

    @Override
    public void setBackground(Color color) {
        // no op
    }

    @Override
    public Color getBackground() {
        return Color.BLACK;
    }

    @Override
    public Stroke getStroke() {
        return new BasicStroke();
    }

    @Override
    public void clip(Shape s) {
        // no op
    }

    @Override
    public FontRenderContext getFontRenderContext() {
        return new FontRenderContext(null, false, false);
    }

    @Override
    public Graphics create() {
        return this;
    }

    @Override
    public Color getColor() {
        return Color.BLACK;
    }

    @Override
    public void setColor(Color c) {
        // no op
    }

    @Override
    public void setPaintMode() {
        // no op
    }

    @Override
    public void setXORMode(Color c1) {
        // no op
    }

    @Override
    public Font getFont() {
        return null; // what should this return?
    }

    @Override
    public void setFont(Font font) {
        // no op
    }

    @Override
    public FontMetrics getFontMetrics(Font f) {
        return null; // what should this return?
    }

    @Override
    public Rectangle getClipBounds() {
        return new Rectangle();
    }

    @Override
    public void clipRect(int x, int y, int width, int height) {
        // no op
    }

    @Override
    public void setClip(int x, int y, int width, int height) {
        // no op
    }

    @Override
    public Shape getClip() {
        return new Rectangle();
    }

    @Override
    public void setClip(Shape clip) {
        // no op
    }

    @Override
    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
        // no op
    }

    @Override
    public void drawLine(int x1, int y1, int x2, int y2) {
        // no op
    }

    @Override
    public void fillRect(int x, int y, int width, int height) {
        // no op
    }

    @Override
    public void clearRect(int x, int y, int width, int height) {
        // no op
    }

    @Override
    public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        // no op
    }

    @Override
    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        // no op
    }

    @Override
    public void drawOval(int x, int y, int width, int height) {
        // no op
    }

    @Override
    public void fillOval(int x, int y, int width, int height) {
        // no op
    }

    @Override
    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        // no op
    }

    @Override
    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
       // no op
    }

    @Override
    public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
        // no op
    }

    @Override
    public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        // no op
    }

    @Override
    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
       // no op
    }

    @Override
    public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
        return true;
    }

    @Override
    public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) {
        return true;
    }

    @Override
    public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor, ImageObserver observer) {
        return true;
    }

    @Override
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
        return true;
    }

    @Override
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bgcolor, ImageObserver observer) {
        return true;
    }

    @Override
    public void dispose() {
        // no op
    }

}
