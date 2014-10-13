/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.neilcsmith.praxis.core.types;

import java.util.Collection;
import java.util.Iterator;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.info.ArgumentInfo;
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
public class PArrayTest {
    
    public PArrayTest() {
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
     * Test of toString method, of class PArray.
     */
    @Test
    public void testToString() {

    }

  
    /**
     * Test of coerce method, of class PArray.
     */
    @Test
    public void testCoerce() throws Exception {
        System.out.println("coerce");
        PArray startArr = PArray.valueOf(PString.valueOf("this has spaces"), PString.valueOf("is"),
                PString.valueOf("an"),
                PArray.valueOf(PString.valueOf("embedded"), PString.valueOf("\\\\array")));
        String arrStr = startArr.toString();
        System.out.println(arrStr);
        PArray a1 = PArray.valueOf(arrStr);
        System.out.println("Array 1");
        for (Argument a : a1) {
            System.out.println(a);
        }
        System.out.println("Array 2");
        PArray a2 = PArray.coerce(a1.get(3));
        for (Argument a : a2) {
            System.out.println(a);
        }
        assertEquals(2, a2.getSize());
    }

    
}
