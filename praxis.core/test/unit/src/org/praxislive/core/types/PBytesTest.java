/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.praxislive.core.types;

import org.praxislive.core.types.PBytes;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.praxislive.core.DataObject;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class PBytesTest {

    private static class Data implements DataObject {

        double x, y, z;

        Data() {
        }

        Data(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public void writeTo(DataOutput out) throws Exception {
            out.writeDouble(x);
            out.writeDouble(y);
            out.writeDouble(z);
        }

        @Override
        public void readFrom(DataInput in) throws Exception {
            x = in.readDouble();
            y = in.readDouble();
            z = in.readDouble();
        }

        @Override
        public String toString() {
            return String.format("Data : %.2f,%.2f,%.2f", x, y, z);
        }
    }

    private static class FailedData implements DataObject {

        @Override
        public void writeTo(DataOutput out) throws Exception {
        }

        @Override
        public void readFrom(DataInput in) throws Exception {
        }

    }

    private final PBytes testBytes;

    public PBytesTest() throws IOException {
        PBytes.OutputStream os = new PBytes.OutputStream();
        DataOutputStream dos = new DataOutputStream(os);
        dos.writeDouble(10);
        dos.writeDouble(20);
        dos.writeDouble(30);
        dos.writeDouble(40);
        dos.writeDouble(50);
        dos.writeDouble(60);
        dos.flush();
        testBytes = os.toBytes();
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
     * Test of toString method, of class PBytes.
     */
    @Test
    public void testToString() {
    }

    /**
     * Test of read method, of class PBytes.
     */
    @Test
    public void testRead() {
    }

    /**
     * Test of asInputStream method, of class PBytes.
     */
    @Test
    public void testAsInputStream() {
    }

    /**
     * Test of getSize method, of class PBytes.
     */
    @Test
    public void testGetSize() {
    }

    /**
     * Test of hashCode method, of class PBytes.
     */
    @Test
    public void testHashCode() {
    }

    /**
     * Test of equals method, of class PBytes.
     */
    @Test
    public void testEquals() {
    }

    /**
     * Test of isEquivalent method, of class PBytes.
     */
    @Test
    public void testIsEquivalent() {
    }

    /**
     * Test of isEmpty method, of class PBytes.
     */
    @Test
    public void testIsEmpty() {
    }

    /**
     * Test of forEachIn method, of class PBytes.
     */
    @Test
    public void testForEachIn() throws IOException {
        PBytes bytes = testBytes;
        Data data = new Data();
        int[] count = new int[1];
        bytes.forEachIn(data, d -> {
            System.out.println(d);
            count[0]++;
        });
        assertEquals(data.x, 40, 0.001);
        assertEquals(data.y, 50, 0.001);
        assertEquals(data.z, 60, 0.001);
        assertEquals(count[0], 2);

        FailedData failer = new FailedData();
        Exception ex = null;
        count[0] = 0;
        try {
            bytes.forEachIn(failer, f -> {
                count[0]++;
            });
        } catch (Exception e) {
            System.out.println("Expected exception : " + e);
            ex = e;
        }
        assertNotNull(ex);
        assertEquals(count[0], 0);
        assertEquals(ex.getClass(), IllegalArgumentException.class);
    }

    /**
     * Test of transformIn method, of class PBytes.
     */
    @Test
    public void testTransformIn() throws IOException {
        PBytes bytes = testBytes;
        Data data = new Data();
        bytes = bytes.transformIn(data, d -> {
            System.out.println(String.format("Data : %.2f,%.2f,%.2f", d.x, d.y, d.z));
            d.x *= 2;
            d.y *= -2;
            d.z = Math.PI;
        });
        int[] count = new int[1];
        bytes.transformIn(data, d -> {
            System.out.println(d);
            count[0]++;
        });
        assertEquals(data.x, 40 * 2, 0.001);
        assertEquals(data.y, 50 * -2, 0.001);
        assertEquals(data.z, Math.PI, 0.001);
        assertEquals(count[0], 2);
        assertEquals(bytes.size(), 8 * 3 * 2);
    }

    /**
     * Test of streamOf method, of class PBytes.
     */
    @Test
    public void testStreamOf_Supplier() throws IOException {
        PBytes bytes = testBytes;
        List<Data> list = bytes.streamOf(Data::new).collect(Collectors.toList());
        System.out.println(list);
        assertEquals(list.size(), 2);
        assertEquals(list.get(0).x, 10, 0.001);
        assertEquals(list.get(0).y, 20, 0.001);
        assertEquals(list.get(0).z, 30, 0.001);
        assertEquals(list.get(1).x, 40, 0.001);
        assertEquals(list.get(1).y, 50, 0.001);
        assertEquals(list.get(1).z, 60, 0.001);
    }

    /**
     * Test of streamOf method, of class PBytes.
     */
    @Test
    public void testStreamOf_int_Supplier() {
        PBytes bytes = testBytes;
        List<Data> list = bytes.streamOf(5, Data::new)
                .map(d -> {
                    if (d.x == 0) {
                        d.x = 70;
                        d.y = 80;
                        d.z = 90;
                    }
                    return d;
                })
                .collect(Collectors.toList());
        System.out.println(list);
        assertEquals(list.size(), 5);
        assertEquals(list.get(0).x, 10, 0.001);
        assertEquals(list.get(0).y, 20, 0.001);
        assertEquals(list.get(0).z, 30, 0.001);
        assertEquals(list.get(1).x, 40, 0.001);
        assertEquals(list.get(1).y, 50, 0.001);
        assertEquals(list.get(1).z, 60, 0.001);
        assertEquals(list.get(2).x, 70, 0.001);
        assertEquals(list.get(2).y, 80, 0.001);
        assertEquals(list.get(2).z, 90, 0.001);
    }

    /**
     * Test of collector method, of class PBytes.
     */
    @Test
    public void testCollector() {
        PBytes bytes = Stream.of(new Data(10, 20, 30), new Data(40, 50, 60)).collect(PBytes.collector());
        assertEquals(bytes, testBytes);
        Data d = new Data();
        PBytes bytes2 = bytes.streamOf((() -> d)).collect(PBytes.collector());
        assertEquals(bytes2, testBytes);
    }

    /**
     * Test of valueOf method, of class PBytes.
     */
    @Test
    public void testValueOf_byteArr() {
    }

    /**
     * Test of valueOf method, of class PBytes.
     */
    @Test
    public void testValueOf_String() throws Exception {
    }

    /**
     * Test of coerce method, of class PBytes.
     */
    @Test
    public void testCoerce() throws Exception {
    }

    /**
     * Test of from method, of class PBytes.
     */
    @Test
    public void testFrom() {
    }

    /**
     * Test of info method, of class PBytes.
     */
    @Test
    public void testInfo() {
    }

    /**
     * Test of deserialize method, of class PBytes.
     */
    @Test
    public void testDeserialize() throws IOException {
        int[] ints = new int[]{1,2,3,4,5};
        PBytes bytes = PBytes.serialize(ints);
        System.out.println("Serialized size : " + bytes.size());
        System.out.println("Base64 : " + bytes.toString());
        int[] out = bytes.deserialize(int[].class);
        assertArrayEquals(ints, out);
        try {
            double[] dbles = bytes.deserialize(double[].class);
            fail("Deserialization to double[] should fail");
        } catch (Exception e) {
            System.out.println("Expected exception : " + e);
        }
    }

    /**
     * Test of valueOf method, of class PBytes.
     */
    @Test
    public void testValueOf_List() {
    }



}
