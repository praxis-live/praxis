
package org.praxislive.code.userapi;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.praxislive.core.DataObject;
import org.praxislive.core.types.PBytes;

/**
 *
 * @author Neil C Smith - https://www.neilcsmith.net
 */
public class StructTest {
    
    private PBytes data1, data2;
    private final String TEST_STRING = "This is our\nTEST STRING";
    
    public StructTest() {
    }
    
    @Before
    public void setUp() throws Exception {
        PBytes.OutputStream pbos = new PBytes.OutputStream();
        DataOutputStream dos = new DataOutputStream(pbos);
        dos.writeDouble(10);
        dos.writeDouble(20);
        dos.writeDouble(30);
        dos.writeDouble(40);
        dos.writeDouble(50);
        dos.writeDouble(60);
        data1 = pbos.toBytes();
        
        
        pbos = new PBytes.OutputStream();
        dos = new DataOutputStream(pbos);
        dos.writeUTF(TEST_STRING);
        dos.writeDouble(40);
        dos.writeDouble(50);
        dos.writeDouble(60);
        data2 = pbos.toBytes();
    }

    /**
     * Test of writeTo method, of class Struct.
     */
    @Test
    public void testWriteTo() throws Exception {
        System.out.println("writeTo");
        StructImpl struct1 = new StructImpl();
        struct1.vec1.x = 10;
        struct1.vec1.y = 20;
        struct1.vec1.z = 30;
        struct1.vec2.x = 40;
        struct1.vec2.y = 50;
        struct1.vec2.z = 60;
        PBytes bytes = Stream.of(struct1).collect(PBytes.collector());
        assertEquals(bytes, data1);
        
        UnsizedStructImpl struct2 = new UnsizedStructImpl();
        struct2.sdo.string = TEST_STRING;
        struct2.vec.x = 40;
        struct2.vec.y = 50;
        struct2.vec.z = 60;
        bytes = Stream.of(struct2).collect(PBytes.collector());
        assertEquals(bytes, data2);
        
        double[] data = new double[]{10,20,30,40,50,60};
        Struct struct3 = new Struct() {
            double[] d = register(data);
        };
        bytes = Stream.of(struct3).collect(PBytes.collector());
        assertEquals(bytes, data1);
        
    }

    /**
     * Test of readFrom method, of class Struct.
     */
    @Test
    public void testReadFrom() throws Exception {
        System.out.println("readFrom");
        int[] count = new int[1];
        StructImpl struct1 = new StructImpl();
        data1.forEachIn(struct1, s -> {
            assertEquals(0, count[0]);
            count[0]++;
            assertEquals(new PVector(10,20,30), s.vec1);
            assertEquals(new PVector(40,50,60), s.vec2);
        });
        UnsizedStructImpl struct2 = new UnsizedStructImpl();
        data2.forEachIn(struct2, s -> {
            assertEquals(1, count[0]);
            count[0]++;
            assertEquals(TEST_STRING, s.sdo.string);
            assertEquals(new PVector(40,50,60), s.vec);
        });
        
        double[] data = new double[6];
        
        Struct struct3 = new Struct() { 
            double[] d = register(data);
        };
        
        data1.forEachIn(struct3, s -> {
            assertEquals(2, count[0]);
            count[0]++;
            assertArrayEquals(new double[]{10,20,30,40,50,60}, data, 0);
        });
        
        Struct empty = new Struct(){};
        
        try {
            data1.forEachIn(empty, s -> {});
        } catch (Exception ex) {
            System.out.println(ex);
            assertTrue(true);
            return;
        } 
        assertFalse(true);
        
    }

    /**
     * Test of size method, of class Struct.
     */
    @Test
    public void testSize() {
        System.out.println("size");
        StructImpl s1 = new StructImpl();
        assertEquals(s1.size().getAsInt(), 6 * Double.BYTES);
        UnsizedStructImpl s2 = new UnsizedStructImpl();
        assertFalse(s2.size().isPresent());
    }

    private static class StructImpl extends Struct {
        PVector vec1 = register(new PVector());
        PVector vec2 = register(new PVector());
        
    }
    
    private static class UnsizedStructImpl extends Struct {
        StringDataObject sdo = register(new StringDataObject());
        PVector vec = register(new PVector());
        
    }
    
    private static class StringDataObject implements DataObject {

        String string = "";
        
        @Override
        public void writeTo(DataOutput out) throws Exception {
            out.writeUTF(string);
        }

        @Override
        public void readFrom(DataInput in) throws Exception {
            string = in.readUTF();
        }
        
    }
    
}
