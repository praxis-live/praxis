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
 */
package net.neilcsmith.praxis.video.pgl;

import net.neilcsmith.praxis.video.PlayerFactory;

/**
 *
 * @author nsigma
 */
public class PGLPlayerFactoryProvider implements PlayerFactory.Provider {
    
    private final static String LIBRARY_NAME = "OpenGL";
    private final static PlayerFactory factory = PGLPlayer.getFactory(); 

    @Override
    public PlayerFactory getFactory() {
        return factory;
    }

    @Override
    public String getLibraryName() {
        return LIBRARY_NAME;
    } 
}
