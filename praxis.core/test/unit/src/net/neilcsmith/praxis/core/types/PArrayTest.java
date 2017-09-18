
package net.neilcsmith.praxis.core.types;

import java.util.stream.Collectors;
import net.neilcsmith.praxis.core.Argument;
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
//        for (Argument a : a1) {
//            System.out.println(a);
//        }
        a1.stream().forEach(System.out::println);
        System.out.println("Array 2");
        PArray a2 = PArray.coerce(a1.get(3));
//        for (Argument a : a2) {
//            System.out.println(a);
//        }
        System.out.println(a2.stream().map(Argument::toString).collect(Collectors.joining(" | ")));
        assertEquals(2, a2.getSize());
    }
    
    @Test
    public void testCollector() throws Exception {
        PArray arr1 = PArray.valueOf("a3 104 {some string} c#5");
        PArray arr2 = arr1.stream().collect(PArray.collector());
        
        System.out.println(arr1);
        System.out.println(arr2);
        
        assertEquals(arr1.getSize(), arr2.getSize());
        
        assertTrue(arr1.isEquivalent(arr2));
        
        PArray arr4 = PArray.valueOf("A3 104 C#5");
        PArray arr3 = arr1.stream()
                .map(Object::toString)
                .filter(s -> !s.contains(" "))
                .map(String::toUpperCase)
                .map(PString::valueOf)
                .collect(PArray.collector());
        
        System.out.println(arr3);
        
        assertTrue(arr3.isEquivalent(arr4));
        
    }

    
}
