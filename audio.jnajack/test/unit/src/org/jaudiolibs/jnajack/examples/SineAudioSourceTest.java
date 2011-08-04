/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaudiolibs.jnajack.examples;

import java.nio.FloatBuffer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author nsigma
 */
public class SineAudioSourceTest {
    
    public SineAudioSourceTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of main method, of class SineAudioSource.
     */
    @Test
    public void testMain() throws Exception {
        System.out.println("main");
        String[] args = null;
        SineAudioSource.main(args);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

//    /**
//     * Test of setup method, of class SineAudioSource.
//     */
//    @Test
//    public void testSetup() {
//        System.out.println("setup");
//        float samplerate = 0.0F;
//        int buffersize = 0;
//        SineAudioSource instance = new SineAudioSource();
//        instance.setup(samplerate, buffersize);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of process method, of class SineAudioSource.
//     */
//    @Test
//    public void testProcess() {
//        System.out.println("process");
//        FloatBuffer[] inputs = null;
//        FloatBuffer[] outputs = null;
//        SineAudioSource instance = new SineAudioSource();
//        instance.process(inputs, outputs);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of shutdown method, of class SineAudioSource.
//     */
//    @Test
//    public void testShutdown() {
//        System.out.println("shutdown");
//        SineAudioSource instance = new SineAudioSource();
//        instance.shutdown();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}
