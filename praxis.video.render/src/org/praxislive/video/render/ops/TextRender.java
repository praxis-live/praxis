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
package org.praxislive.video.render.ops;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import org.praxislive.video.render.PixelData;
import org.praxislive.video.render.SurfaceOp;
import org.praxislive.video.render.utils.ImageUtils;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class TextRender implements SurfaceOp {

    private String text;
    private Font font;
    private Color color;
    private double x;
    private double y;
    private AffineTransform transform;

    public TextRender() {
        text = "";
        color = Color.WHITE;
        x = 0;
        y = 0;
    }

    public String getText() {
        return text;
    }

    public TextRender setText(String text) {
        if (text == null) {
            text = "";
        }
        this.text = text;
        return this;
    }

    public Font getFont() {
        return font;
    }

    public TextRender setFont(Font font) {
        this.font = font;
        return this;
    }

    public Color getColor() {
        return color;
    }

    public TextRender setColor(Color color) {
        if (color == null) {
            color = Color.WHITE;
        }
        this.color = color;
        return this;
    }

    public double getX() {
        return x;
    }

    public TextRender setX(double x) {
        this.x = x;
        return this;
    }

    public double getY() {
        return y;
    }

    public TextRender setY(double y) {
        this.y = y;
        return this;
    }

    public AffineTransform getTransform() {
        return transform;
    }

    public TextRender setTransform(AffineTransform transform) {
        this.transform = transform;
        return this;
    }

    @Override
    public void process(PixelData output, PixelData... inputs) {
        if (text == null || text.isEmpty() || font == null) {
            return;
        }
        BufferedImage im = ImageUtils.toImage(output);
        Graphics2D g = im.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        if (transform != null) {
            g.setTransform(transform);
        }
        g.setColor(color);
        g.setFont(font);
        g.drawString(text, (float) x, (float) y);

    }

}
