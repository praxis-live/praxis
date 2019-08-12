/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.praxislive.core;

import org.praxislive.core.ArgumentInfo;
import org.praxislive.core.ControlInfo;
import org.praxislive.core.ComponentInfo;
import org.praxislive.core.PortInfo;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.praxislive.core.Value;
import org.praxislive.core.ControlPort;
import org.praxislive.core.protocols.ComponentProtocol;
import org.praxislive.core.protocols.StartableProtocol;
import org.praxislive.core.types.PMap;
import org.praxislive.core.types.PNumber;
import org.praxislive.core.types.PString;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.praxislive.core.Protocol;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class ComponentInfoTest {
    
    private ComponentInfo info;
    
    public ComponentInfoTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        Set<Class<? extends Protocol>> interfaces = new LinkedHashSet<>(2);
        interfaces.add(ComponentProtocol.class);
        interfaces.add(StartableProtocol.class);
        
        Map<String, ControlInfo> controls = new LinkedHashMap<>();
        controls.put("p1", ControlInfo.createPropertyInfo(new ArgumentInfo[]{PNumber.info(0, 1)}, new Value[]{PNumber.ONE}, PMap.of(ControlInfo.KEY_TRANSIENT, true)));
        controls.put("p1", ControlInfo.createPropertyInfo(new ArgumentInfo[]{ArgumentInfo.of(PString.class, PMap.of("template", "public void draw(){"))}, new Value[]{PString.EMPTY}, PMap.EMPTY));
        controls.put("ro1", ControlInfo.createReadOnlyPropertyInfo(new ArgumentInfo[]{PNumber.info(0, 1)}, PMap.of(ControlInfo.KEY_TRANSIENT, true)));
        controls.put("t1", ControlInfo.createActionInfo(PMap.of("key", "value")));
        
        Map<String, PortInfo> ports = new LinkedHashMap<>();
        ports.put("in", PortInfo.create(ControlPort.class, PortInfo.Direction.IN, PMap.EMPTY));
        ports.put("out", PortInfo.create(ControlPort.class, PortInfo.Direction.OUT, PMap.EMPTY));
        
        PMap properties = PMap.of(ComponentInfo.KEY_DYNAMIC, true);
        
        info = ComponentInfo.create(controls, ports, interfaces, properties);
    }
    
    @After
    public void tearDown() {
        info = null;
    }

    /**
     * Test of coerce method, of class ComponentInfo.
     */
    @Test
    public void testCoerce() throws Exception {
        System.out.println("coerce");
        String ci = info.toString();
        System.out.println(ci);
        ComponentInfo info2 = ComponentInfo.coerce(PString.of(ci));
        System.out.println(info2);
        System.out.println(info.controlInfo("p1"));
        System.out.println(info2.controlInfo("p1"));
        System.out.println(info.controlInfo("ro1"));
        System.out.println(info2.controlInfo("ro1"));
//        assertTrue(Value.equivalent(Value.class, info.getControlInfo("p1").getOutputsInfo()[0], info2.getControlInfo("p1").getOutputsInfo()[0]));
//        assertTrue(Value.equivalent(Value.class, info.getControlInfo("ro1").getOutputsInfo()[0], info2.getControlInfo("ro1").getOutputsInfo()[0]));
//        assertTrue(Value.equivalent(Value.class, info, info2));
        assertTrue(info.equivalent(info2));
        assertTrue(info.controlInfo("p1").equivalent(info2.controlInfo("p1")));
        assertTrue(info.controlInfo("ro1").getOutputsInfo()[0].equivalent(info2.controlInfo("ro1").getOutputsInfo()[0]));
        assertTrue(info.portInfo("in").equivalent(info2.portInfo("in")));
    }

    
    @Test
    public void testProtocols() {
        List<Class<? extends Protocol>> protocols = info.protocols().collect(Collectors.toList());
        assertEquals(2, protocols.size());
        assertEquals(ComponentProtocol.class, protocols.get(0));
        assertEquals(StartableProtocol.class, protocols.get(1));
        
    }
   
}
