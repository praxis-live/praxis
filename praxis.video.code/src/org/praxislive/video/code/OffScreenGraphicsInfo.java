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
package org.praxislive.video.code;

import java.lang.reflect.Field;
import org.praxislive.logging.LogLevel;
import org.praxislive.video.code.userapi.OffScreen;
import org.praxislive.video.code.userapi.PGraphics;
import org.praxislive.video.render.Surface;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
class OffScreenGraphicsInfo {

    private final int width;
    private final int height;
    private final double scaleWidth;
    private final double scaleHeight;
    private final boolean persistent;
    private final OffScreen.Format format;
    private final Field field;

    private VideoCodeContext<?> context;
    private OffScreenGraphics graphics;

    private OffScreenGraphicsInfo(Field field,
            int width,
            int height,
            double scaleWidth,
            double scaleHeight,
            boolean persistent,
            OffScreen.Format format) {
        this.field = field;
        this.width = width;
        this.height = height;
        this.scaleWidth = scaleWidth;
        this.scaleHeight = scaleHeight;
        this.persistent = persistent;
        this.format = format;
    }

    void attach(VideoCodeContext context, OffScreenGraphicsInfo previous) {
        this.context = context;
        if (previous != null) {
            if (persistent
                    && previous.graphics != null /* will get validated later anyway?
                        && width == old.width
                        && height == old.height
                        && format == old.format*/) {
                graphics = new OffScreenGraphics(previous.graphics.surface);
                previous.graphics = null;
                try {
                    field.set(context.getDelegate(), graphics);
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    context.getLog().log(LogLevel.ERROR, ex);
                }
            }

        }
    }

    void validate(Surface output) {
        if (graphics == null
                || !isValid(graphics, output)) {
            OffScreenGraphics old = graphics;
            graphics = createGraphics(output);
            if (old != null && persistent) {
                graphics.surface.copy(old.surface);
                old.surface.release();
            }
            try {
                field.set(context.getDelegate(), graphics);
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                context.getLog().log(LogLevel.ERROR, ex);
            }
        }
    }

    void endFrame() {
        if (!persistent) {
            release();
        }
    }

    void release() {
        if (graphics != null) {
            graphics.surface.release();
        }
    }

    private boolean isValid(OffScreenGraphics graphics, Surface output) {
        if (graphics == null) {
            return false;
        }
        Surface offscreenSurface = graphics.surface;
        if (!output.checkCompatible(offscreenSurface, false, false)) {
            return false;
        }
        return offscreenSurface.hasAlpha() == calculateAlpha(output)
                && offscreenSurface.getWidth() == calculateWidth(output)
                && offscreenSurface.getHeight() == calculateHeight(output);

    }

    private boolean calculateAlpha(Surface output) {
        if (format == OffScreen.Format.Default) {
            return output.hasAlpha();
        } else {
            return format == OffScreen.Format.ARGB;
        }
    }

    private int calculateWidth(Surface output) {
        int w = width < 1 ? output.getWidth() : width;
        w *= scaleWidth;
        return Math.max(w, 1);
    }

    private int calculateHeight(Surface output) {
        int h = height < 1 ? output.getHeight() : height;
        h *= scaleHeight;
        return Math.max(h, 1);
    }

    private OffScreenGraphics createGraphics(Surface output) {
        return new OffScreenGraphics(output.createSurface(
                calculateWidth(output), calculateHeight(output), calculateAlpha(output)));
    }

    static OffScreenGraphicsInfo create(Field field) {
        OffScreen ann = field.getAnnotation(OffScreen.class);
        if (ann == null
                || !PGraphics.class.isAssignableFrom(field.getType())) {
            return null;
        }
        field.setAccessible(true);
        int width = ann.width();
        int height = ann.height();
        OffScreen.Format format = ann.format();
//        if (width < 1 || height < 1) {
//            width = 0;
//            height = 0;
//        }
        double scaleWidth = ann.scaleWidth();
        double scaleHeight = ann.scaleHeight();
        boolean persistent = ann.persistent();
        return new OffScreenGraphicsInfo(field,
                width,
                height,
                scaleWidth,
                scaleHeight,
                persistent,
                format);
    }

    private static class OffScreenGraphics extends PGraphics {

        private final Surface surface;

        private OffScreenGraphics(Surface surface) {
            super(surface.getWidth(), surface.getHeight());
            this.surface = surface;
        }

        @Override
        protected Surface getSurface() {
            return surface;
        }

    }

}
