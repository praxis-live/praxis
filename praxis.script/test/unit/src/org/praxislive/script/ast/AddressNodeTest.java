/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.praxislive.script.ast;

import org.praxislive.script.ast.AddressNode;
import java.util.ArrayList;
import java.util.List;
import org.praxislive.core.Value;
import org.praxislive.core.ComponentAddress;
import org.praxislive.script.Command;
import org.praxislive.script.Namespace;
import org.praxislive.script.Variable;
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
public class AddressNodeTest {

    public AddressNodeTest() {
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

//    /**
//     * Test of init method, of class AddressNode.
//     */
//    @Test
//    public void testInit() {
//        System.out.println("init");
//        Namespace namespace = null;
//        AddressNode instance = null;
//        instance.init(namespace);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of reset method, of class AddressNode.
//     */
//    @Test
//    public void testReset() {
//        System.out.println("reset");
//        AddressNode instance = null;
//        instance.reset();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of writeResult method, of class AddressNode.
     */
    @Test
    public void testWriteResult() {
        System.out.println("writeResult");
        List<Value> args = new ArrayList<Value>();
        Namespace ns = new NS();
        String[] ads = {"./to/here", "./to/here.control", "./to/here!port", ".control2"};
        for (String ad : ads) {
            AddressNode instance = new AddressNode(ad);
            instance.init(ns);
            instance.writeResult(args);
            System.out.println(ad + " : " + args.get(0));
            args.clear();
        }
    }

    private class NS implements Namespace, Variable {

        public Variable getVariable(String id) {
            if ("_CTXT".equals(id)) {
                return this;
            }
            return null;
        }

        public void addVariable(String id, Variable var) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Namespace createChild() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void setValue(Value value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Value getValue() {
            return ComponentAddress.of("/test/address");
        }

        public Command getCommand(String id) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void addCommand(String id, Command cmd) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

}