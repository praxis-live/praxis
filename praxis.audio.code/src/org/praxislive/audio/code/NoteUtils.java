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

/**
 *
 * @author Neil C Smith (http://www.neilcsmith.net)
 */
class NoteUtils {

    private NoteUtils() {
    }

    static int noteToMidi(String note) {
        int len = note.length();
        if (len < 2 || len > 3) {
            return -1;
        }
        int midi;

        switch (note.charAt(0)) {
            case 'c':
            case 'C':
                midi = 0;
                break;
            case 'd':
            case 'D':
                midi = 2;
                break;
            case 'e':
            case 'E':
                midi = 4;
                break;
            case 'f':
            case 'F':
                midi = 5;
                break;
            case 'g':
            case 'G':
                midi = 7;
                break;
            case 'a':
            case 'A':
                midi = 9;
                break;
            case 'b':
            case 'B':
                midi = 11;
                break;
            default:
                return -1;
        }

        switch (len) {
            case 3:
                switch (note.charAt(1)) {
                    case '#':
                        midi++;
                        break;
                    case 'b':
                        midi--;
                        break;
                    default:
                        return -1;
                }
            default:
                int octave = note.charAt(len - 1) - '0';
                if (octave < 0 || octave > 9) {
                    return -1;
                }
                midi += (octave * 12) + 12; 
         }
        
        return midi;
    }

    static double midiToFrequency(int midi) {
        return midi < 1 ? 0 : 440.0 * Math.pow(2.0, ((midi - 69.0) / 12.0));
    }

}
