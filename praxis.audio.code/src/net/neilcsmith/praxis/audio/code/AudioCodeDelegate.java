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
 *
 */
package net.neilcsmith.praxis.audio.code;

import net.neilcsmith.praxis.audio.code.userapi.Add;
import net.neilcsmith.praxis.audio.code.userapi.Mod;
import net.neilcsmith.praxis.audio.code.userapi.Tee;
import net.neilcsmith.praxis.code.DefaultCodeDelegate;
import org.jaudiolibs.pipes.Pipe;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class AudioCodeDelegate extends DefaultCodeDelegate {

    public void setup() {
    }

    public void update() {
    }

    public final Pipe link(Pipe ... ugens) {
        int count = ugens.length;
        if (count < 1) {
            return null;
        }
        for (int i = ugens.length - 1; i > 0; i--) {
            ugens[i].addSource(ugens[i - 1]);
        }
        return ugens[ugens.length - 1];
    }
    
    public final Add add() {
        return new Add();
    }

    public final Add add(Pipe ... ugens) {
        Add add = new Add();
        for (Pipe ugen : ugens) {
            add.addSource(ugen);
        }
        return add;
    }
    
    public final Mod mod() {
        return new Mod();
    }

    public final Mod mod(Pipe ... ugens) {
        Mod mod = new Mod();
        for (Pipe ugen : ugens) {
            mod.addSource(ugen);
        }
        return mod;
    }

    public final Tee tee() {
        return new Tee();
    }
    
}
