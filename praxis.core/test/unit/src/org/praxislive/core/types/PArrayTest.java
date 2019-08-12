
package org.praxislive.core.types;

import org.praxislive.core.types.PArray;
import org.praxislive.core.types.PString;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
     * Test of get method, of class PArray.
     */
    @Test
    public void testGet() throws Exception {
        PArray arr1 = PArray.parse("1 2 3 4");
        assertEquals(arr1.get(3), arr1.get(-1));
        PArray arr2 = IntStream.range(-10, 10)
                .mapToObj(arr1::get)
                .collect(PArray.collector());
        System.out.println(arr2);
        assertEquals(arr1.get(0).toString(), arr2.get(2).toString());
        
        PArray arr3 = PArray.parse("");
        assertEquals(arr3.get(0).toString(), "");
        
        
    }
  
    /**
     * Test of coerce method, of class PArray.
     */
    @Test
    public void testCoerce() throws Exception {
        System.out.println("coerce");
        PArray startArr = PArray.of(PString.of("this has spaces"), PString.of("is"),
                PString.of("an"),
                PArray.of(PString.of("embedded"), PString.of("\\\\array")));
        String arrStr = startArr.toString();
        System.out.println(arrStr);
        PArray a1 = PArray.parse(arrStr);
        System.out.println("Array 1");
//        for (Value a : a1) {
//            System.out.println(a);
//        }
        a1.stream().forEach(System.out::println);
        System.out.println("Array 2");
        PArray a2 = PArray.coerce(a1.get(3));
//        for (Value a : a2) {
//            System.out.println(a);
//        }
        System.out.println(a2.stream().map(Value::toString).collect(Collectors.joining(" | ")));
        assertEquals(2, a2.size());
    }
    
    @Test
    public void testCollector() throws Exception {
        PArray arr1 = PArray.parse("a3 104 {some string} c#5");
        PArray arr2 = arr1.stream().collect(PArray.collector());
        
        System.out.println(arr1);
        System.out.println(arr2);
        
        assertEquals(arr1.size(), arr2.size());
        
        assertTrue(arr1.equivalent(arr2));
        
        PArray arr4 = PArray.parse("A3 104 C#5");
        PArray arr3 = arr1.stream()
                .map(Object::toString)
                .filter(s -> !s.contains(" "))
                .map(String::toUpperCase)
                .map(PString::of)
                .collect(PArray.collector());
        
        System.out.println(arr3);
        
        assertTrue(arr3.equivalent(arr4));
        
    }

    
}
