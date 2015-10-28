/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2015 Neil C Smith.
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
 */

package net.neilcsmith.praxis.video.gst1.components;

import net.neilcsmith.praxis.video.render.Surface;
import net.neilcsmith.praxis.video.utils.ResizeMode;

/**
 *
 * @author Neil C Smith
 */
public abstract class VideoDelegate {
    
    public enum State {New, Ready, Playing, Paused, Error, Disposed}; 
    
    private ResizeMode resizeMode;
    
    protected VideoDelegate() {
        resizeMode = new ResizeMode(ResizeMode.Type.Stretch, 0.5, 0.5);
    }
    
    public abstract void process(Surface output);
    
    public void setResizeMode(ResizeMode mode) {
        if (mode == null) {
            throw new NullPointerException();
        }
        resizeMode = mode;
    }
    
    public ResizeMode getResizeMode() {
        return resizeMode;
    }
    
    public long getDuration() {
        return -1;
    }
    
    public long getPosition() {
        return -1;
    }
    
    public void setPosition(long position) {
        throw new UnsupportedOperationException();
    }
    
    public boolean isSeekable() {
        return false;
    }
    
    public boolean isLooping() {
        return false;
    }
    
    public boolean isLoopable() {
        return false;
    }
    
    public void setLooping(boolean loop) {
        if (loop) {
            throw new UnsupportedOperationException();
        }
    }
    
    public boolean isVariableSpeed() {
        return false;
    }
    
    public double getSpeed() {
        return 1;
    }
    
    public void setSpeed(double speed) {
        throw new UnsupportedOperationException();
    }
    
    public boolean supportsFrameSizeRequest() {
        return false;
    }
    
    public void requestFrameWidth(int width) {
        throw new UnsupportedOperationException();
    }
    
    public void requestFrameHeight(int height) {
        throw new UnsupportedOperationException();
    }
    
    public void defaultFrameWidth() {
        requestFrameWidth(-1);
    }
    
    public void defaultFrameHeight() {
        requestFrameHeight(-1);
    }
    
    public boolean supportsFrameRateRequest() {
        return false;
    }
    
    public void requestFrameRate(double rate) {
        throw new UnsupportedOperationException();
    }
    
    public void defaultFrameRate() {
        requestFrameRate(-1);
    }
         
    public abstract State initialize() throws StateException;
    
    public abstract void play() throws StateException;
    
    public abstract void pause() throws StateException;
    
    public abstract void stop() throws StateException;
    
    public abstract void dispose();
    
    public abstract State getState();
    
    
    public static class StateException extends Exception {
        
        public StateException() {}
        
        public StateException(String message) {
            super(message);
        }
        
        public StateException(Throwable cause) {
            super(cause);
        }
        
        public StateException(String message, Throwable cause) {
            super(message, cause);
        }
        
    }
}
