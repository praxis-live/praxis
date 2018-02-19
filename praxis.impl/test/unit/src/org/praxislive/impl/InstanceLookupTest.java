/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.praxislive.impl;

import java.util.Optional;
import java.util.stream.Collectors;
import org.praxislive.core.Lookup;
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
public class InstanceLookupTest {

    public InstanceLookupTest() {
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
     * Test of find method, of class InstanceLookup.
     */
    @Test
    public void testFind() {
        System.out.println("find");
        InstanceLookup lkp = create();
        Optional<String> s = lkp.find(String.class);
        assertEquals(s.get(), "This");
        Optional<Boolean> b = lkp.find(Boolean.class);
        assertEquals(b.get(), true);
        Optional<Number> n = lkp.find(Number.class);
        assertEquals(n.get(), 42);
    }

    /**
     * Test of findAll method, of class InstanceLookup.
     */
    @Test
    public void testFindAll() {
        System.out.println("findAll");
        InstanceLookup lkp = create();
        String test = lkp.findAll(String.class).collect(Collectors.joining(" "));
        assertEquals(test, "This is our test String");
        assertEquals(8, lkp.findAll(Object.class).count());
    }

    private InstanceLookup create() {
        return InstanceLookup.create(
                InstanceLookup.create(Lookup.EMPTY, "test", "String", true),
                "This", "is", 42, "our", new Object());
    }

}
