/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Neil C Smith.
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

package net.neilcsmith.praxis.video.java;

import net.neilcsmith.praxis.impl.ListenerUtils;
import net.neilcsmith.praxis.java.CodeContext;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public abstract class VideoCodeContext extends CodeContext {

    private ImageListener[] listeners;

    protected VideoCodeContext() {
        listeners = new ImageListener[0];
    }

    public abstract PImage getImage(int index);

    public abstract int getImageCount();

    public void addImageListener(ImageListener listener) {
        listeners = ListenerUtils.add(listeners, listener);
    }

    public void removeImageListener(ImageListener listener) {
        listeners = ListenerUtils.remove(listeners, listener);
    }

    protected void fireImageChange(int index) {
        for (ImageListener l : listeners) {
            l.imageChanged(index);
        }
    }

    protected void fireImageLoadError(int index) {
        for (ImageListener l : listeners) {
            l.imageLoadError(index);
        }
    }

    public static interface ImageListener {

        public void imageChanged(int index);

        public void imageLoadError(int index);

    }

}
