/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.praxislive.impl;

import org.praxislive.impl.InstanceLookup;
import java.util.Collections;
import java.util.Iterator;
import org.praxislive.core.Lookup;
import org.praxislive.core.Lookup.Result;
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
     * Test of get method, of class InstanceLookup.
     */
    @Test
    public void testGet() {
        System.out.println("get");
        InstanceLookup lkp = create();
        System.out.println(lkp.get(String.class));
        System.out.println(lkp.get(Object.class));

    }

    /**
     * Test of getAll method, of class InstanceLookup.
     */
    @Test
    public void testGetAll() {
        System.out.println("getAll");
        InstanceLookup lkp = create();
        Lookup.Result<String> r1 = lkp.getAll(String.class);
        for (String str : r1) {
            System.out.print(str + " ");
        }
        System.out.println("");
        Lookup.Result<Object> r2 = lkp.getAll(Object.class);
        for (Object obj : r2) {
            System.out.print(obj + " ");
        }
        System.out.println("");
    }

    private InstanceLookup create() {
        return InstanceLookup.create(
                new EmptyLookup(), new Object[]{"This", "Is", "Our", "Test", "String", new Object()});
    }

    private static class EmptyLookup implements Lookup {

        public <T> T get(Class<T> type) {
            return null;
        }

        public <T> Result<T> getAll(Class<T> type) {
            return new Result<T>() {

                public Iterator<T> iterator() {
                    return Collections.EMPTY_LIST.iterator();
                }
            };
        }

    }

}