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
package org.praxislive.video;

/**
 *
 * @author Neil C Smith
 */
public class WindowHints {
    
    private boolean fullScreen = false;
    private boolean undecorated = false;
    private boolean alwaysOnTop = false;
    private boolean showCursor = false;
    private String title = "";

    public boolean isFullScreen() {
        return fullScreen;
    }

    public void setFullScreen(boolean fullScreen) {
        this.fullScreen = fullScreen;
    }

    public boolean isUndecorated() {
        return undecorated;
    }

    public void setUndecorated(boolean undecorated) {
        this.undecorated = undecorated;
    }
    
    public boolean isAlwaysOnTop() {
        return alwaysOnTop;
    }
    
    public void setAlwaysOnTop(boolean alwaysOnTop) {
        this.alwaysOnTop = alwaysOnTop;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title == null) {
            throw new NullPointerException();
        }
        this.title = title;
    }

    public boolean isShowCursor() {
        return showCursor;
    }

    public void setShowCursor(boolean showCursor) {
        this.showCursor = showCursor;
    }

    
    
}
