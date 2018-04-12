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
package org.praxislive.audio.code;

import org.jaudiolibs.pipes.Pipe;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
class Utils {

    private Utils() {
    }
    
    static void disconnect(Pipe pipe) {
        disconnectSources(pipe);
        disconnectSinks(pipe);
    }

    static void disconnectSources(Pipe pipe) {
        for (int i = pipe.getSourceCount(); i > 0; i--) {
            pipe.removeSource(pipe.getSource(i - 1));
        }
    }

    static void disconnectSinks(Pipe pipe) {
        for (int i = pipe.getSinkCount(); i > 0; i--) {
            pipe.getSink(i - 1).removeSource(pipe);
        }
    }

}
