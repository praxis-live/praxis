/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.praxislive.core.syntax;

import org.praxislive.core.syntax.Tokenizer;
import org.praxislive.core.syntax.Token;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class TokenizerTest {

    public TokenizerTest() {
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
     * Test of iterator method, of class Tokenizer.
     */
    @Test
    public void testIterator() {
        System.out.println("iterator");
        Tokenizer tokens = new Tokenizer("#comment\nthis \"is\"; [a] {{test\\}}} in\\; for you; now");
        for (Token tok : tokens) {
            System.out.print(tok.getType());
            System.out.print(" : ");
            System.out.println(tok.getText());
        }
    }

}