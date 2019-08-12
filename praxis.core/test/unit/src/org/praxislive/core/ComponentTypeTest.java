/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.praxislive.core;

import org.praxislive.core.ComponentType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class ComponentTypeTest {

    public ComponentTypeTest() {
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
     * Test of toString method, of class ComponentType.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        
    }

    /**
     * Test of hashCode method, of class ComponentType.
     */
    @Test
    public void testHashCode() {
        System.out.println("hashCode");
        
    }

    /**
     * Test of equals method, of class ComponentType.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        
    }

    /**
     * Test of create method, of class ComponentType.
     */
    @Test
    public void testCreate() {
        System.out.println("create");
        String[] types = new String[] {
            "root:audio",
            "core:test-this_one",
            " root:audio",
            "_root:audio",
            "root:audio:",
            "Not a valid type",
            "core:test&this",
            "notvalid",
            "core:array:random"
        };
        boolean[] allowed = new boolean[] {
            true, true, false, false, false, false, false, false, true
        };
        for (int i=0; i < types.length; i++) {
            try {
                System.out.println("Testing - " + types[i]);
                System.out.println("Created Type - "
                        + ComponentType.parse(types[i]));
                if (! allowed[i] ) {
                    fail("Illegal type allowed through");
                }
            } catch (Exception ex) {
                if (allowed[i]) {
                    fail("Allowed type failed");
                }
            }
        }
    }

    /**
     * Test of valueOf method, of class ComponentType.
     */
    @Test
    public void testValueOf() throws Exception {
        System.out.println("valueOf");
        
    }

    /**
     * Test of info method, of class ComponentType.
     */
    @Test
    public void testInfo() {
        System.out.println("info");
       
    }

    /**
     * Test of coerce method, of class ComponentType.
     */
    @Test
    public void testCoerce() throws Exception {
        System.out.println("coerce");
        
    }

}