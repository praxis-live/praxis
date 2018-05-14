
package org.praxislive.audio.code;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Neil C Smith - http://www.neilcsmith.net
 */
public class NoteUtilsTest {

    public NoteUtilsTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testNoteToMidi() {
        String[] notes1 = "c c# d d# e f f# g g# a a# b".split("\\s");
        String[] notes2 = "C Db D Eb E F Gb G Ab A Bb B".split("\\s");

        String[][] tests = {notes1, notes2};

        for (String[] notes : tests) {
            int midi = 11;

            for (int octave = 0; octave < 10; octave++) {
                for (String note : notes) {
                    int m = NoteUtils.noteToMidi(note + octave);
                    assertEquals(midi + 1, m);
                    midi++;

                }
            }
        }
        
        String[] invalids = "C d10 bb d# a-1".split("\\s");
        
        for (String invalid : invalids) {
            assertEquals(-1, NoteUtils.noteToMidi(invalid));
        }
        

    }

}
