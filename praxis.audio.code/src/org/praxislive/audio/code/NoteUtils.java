/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Neil C Smith.
 * Derived from code in Frinika copyright 2007 Jens Gulden
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
package org.praxislive.audio.code;

/**
 *
 * @author Jens Gulden
 * @author Neil C Smith (http://neilcsmith.net)
 */
class NoteUtils {
    
    private NoteUtils() {}
    
    private final static String NOTES = "c d ef g a b";
    private final static String NOTES_UPPER = NOTES.toUpperCase();

    static int noteToMidi(String s) {
        int note;
        int mod = 0; // -1, 0, +1
        int octave = 0;

        char n = s.charAt(0);
        note = NOTES.indexOf(n);
        if (note == -1) {
            note = NOTES_UPPER.indexOf(n);
            if (note == -1) {
                return -1; // invalid
            }
        }

        int len = s.length();
        switch (len) {
            case 3:
                char m = s.charAt(1);
                switch (m) {
                    case '#':
                        mod = 1;
                        break;
                    case 'b':
                        mod = -1;
                        break;
                    default:
                        return -1; // invalid
                }
            // fallthrough
            case 2:
                char oc = s.charAt(len - 1);
                octave += (int) oc - 48;
                if (octave < 0 || octave > 9) {
                    return -1;
                }
                break;
            default:
                return -1;
        }

        int result = note + mod + 12 + (octave * 12);
        return result;
    }

    static double midiToFrequency(int midi) {
        return 440.0 * Math.pow(2.0, ((midi - 69.0) / 12.0));
    }
    
}
