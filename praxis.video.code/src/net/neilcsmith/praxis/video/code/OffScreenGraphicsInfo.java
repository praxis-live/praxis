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
 */
package net.neilcsmith.praxis.video.code;

import java.lang.reflect.Field;
import net.neilcsmith.praxis.logging.LogLevel;
import net.neilcsmith.praxis.video.code.userapi.OffScreen;
import net.neilcsmith.praxis.video.code.userapi.PGraphics;
import net.neilcsmith.praxis.video.render.Surface;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
class OffScreenGraphicsInfo {

    private final int width;
    private final int height;
    private final OffScreen.Format format;
    private final Field field;
    private final String id;

    private VideoCodeContext<?> context;
    private OffScreenGraphics graphics;

    private OffScreenGraphicsInfo(Field field, int width, int height, OffScreen.Format format) {
        this.field = field;
        this.width = width;
        this.height = height;
        this.format = format;
        this.id = field.getName();
    }

    void attach(VideoCodeContext context, OffScreenGraphicsInfo[] previous) {
        this.context = context;
        if (previous != null) {
            for (OffScreenGraphicsInfo old : previous) {
                if (old.id.equals(id)
                        && old.graphics != null
                        && width == old.width
                        && height == old.height
                        && format == old.format) {
                    graphics = new OffScreenGraphics(old.graphics.surface);
                    old.graphics = null;
                    try {
                        field.set(context.getDelegate(), graphics);
                    } catch (IllegalArgumentException | IllegalAccessException ex) {
                        context.getLog().log(LogLevel.ERROR, ex);
                    }
                    break;
                }
            }
        }
    }

    void validate(Surface output) {
        if (graphics == null
                || !output.checkCompatible(graphics.surface,
                        width == 0,
                        format == OffScreen.Format.Default)) {
            OffScreenGraphics old = graphics;
            graphics = createGraphics(output);
            if (old != null) {
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

    private OffScreenGraphics createGraphics(Surface output) {
        if (width == 0 && format == OffScreen.Format.Default) {
            return new OffScreenGraphics(output.createSurface());
        } else {
            int w = width == 0 ? output.getWidth() : width;
            int h = height == 0 ? output.getHeight() : height;
            boolean alpha;
            if (format == OffScreen.Format.Default) {
                alpha = output.hasAlpha();
            } else {
                alpha = format == OffScreen.Format.ARGB;
            }
            return new OffScreenGraphics(output.createSurface(w, h, alpha));
        }
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
        if (width < 1 || height < 1) {
            width = 0;
            height = 0;
        }
        return new OffScreenGraphicsInfo(field, width, height, format);
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
