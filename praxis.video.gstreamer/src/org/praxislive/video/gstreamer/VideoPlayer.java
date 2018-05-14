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
package org.praxislive.video.gstreamer;

import java.util.Optional;
import java.util.function.Consumer;
import org.praxislive.core.types.PResource;
import org.praxislive.video.code.userapi.PImage;

/**
 *
 * @author Neil C Smith - http://www.neilcsmith.net
 */
public interface VideoPlayer {
    
    public enum State {Empty, Ready, Playing, Paused, Error}
    
    public VideoPlayer location(Optional<PResource> location);
    
    public VideoPlayer play();
    
    public VideoPlayer pause();
    
    public VideoPlayer stop();
    
    public State state();
    
    public VideoPlayer looping(boolean looping);
    
    public VideoPlayer rate(double rate);
    
    public void audioSink(String audioSink);
    
    public boolean render(Consumer<PImage> renderer);
    
    public VideoPlayer position(double position);
    
    public double position();
    
    public double duration();
    
    public VideoPlayer onReady(Runnable ready);
    
    public VideoPlayer onError(Consumer<String> error);
    
    public VideoPlayer onEOS(Runnable eos);
    
}
