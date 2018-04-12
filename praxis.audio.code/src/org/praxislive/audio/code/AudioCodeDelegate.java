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

import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import org.praxislive.audio.code.userapi.Add;
import org.praxislive.audio.code.userapi.Mod;
import org.praxislive.audio.code.userapi.OpGen;
import org.praxislive.audio.code.userapi.Tee;
import org.praxislive.code.DefaultCodeDelegate;
import org.jaudiolibs.pipes.Pipe;
import org.praxislive.audio.code.userapi.AudioTable;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class AudioCodeDelegate extends DefaultCodeDelegate {

    public double sampleRate;
    public int blockSize;
    
    @SuppressWarnings("deprecation")
    public void init() {
        setup();
    }
    
    @Deprecated
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

    public final Mod modFn(DoubleBinaryOperator function) {
        Mod mod = new Mod();
        mod.function(function);
        return mod;
    }
    
    public final Mod modFn(Pipe pipe, DoubleBinaryOperator function) {
        Mod mod = modFn(function);
        mod.addSource(pipe);
        return mod;
    }
    
    public final Tee tee() {
        return new Tee();
    }
    
    public final OpGen fn(DoubleUnaryOperator function) {
        return new OpGen().function(function);
    }
    
    public final double noteToFrequency(String note) {
        int midi = noteToMidi(note);
        if (midi < 0) {
            return 0;
        } else {
            return midiToFrequency(midi);
        }
    }
    
    public final int noteToMidi(String note) {
        return NoteUtils.noteToMidi(note);
    }
    
    public final double midiToFrequency(int midi) {
        return NoteUtils.midiToFrequency(midi);
    }
    
    public double tabread(AudioTable table, double position) {
        return table == null ? 0 : table.get(0, position * table.size());
    }
    
}
