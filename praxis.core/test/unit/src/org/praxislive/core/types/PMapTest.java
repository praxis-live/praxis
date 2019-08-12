/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.praxislive.core.types;

import org.praxislive.core.types.PMap;
import org.praxislive.core.Value;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class PMapTest {
    
    public PMapTest() {
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

   

    /**
     * Test of coerce method, of class PMap.
     */
    @Test
    public void testCoerce() throws Exception {
        PMap m = PMap.of("template", "public void draw(){");
        String mStr = m.toString();
        System.out.println(mStr);
        PMap m2 = PMap.parse(mStr);
        assertTrue(Utils.equivalent(m, m2));
    }


}
