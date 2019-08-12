
package org.praxislive.core;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.praxislive.core.protocols.ComponentProtocol;
import org.praxislive.core.protocols.StartableProtocol;
import org.praxislive.core.types.PBoolean;
import org.praxislive.core.types.PMap;
import org.praxislive.core.types.PNumber;
import org.praxislive.core.types.PString;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Neil C Smith - https://www.neilcsmith.net
 */
public class InfoTest {
    
    private ComponentInfo compareInfo;
    
    public InfoTest() {
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
        controls.put("p2", ControlInfo.createPropertyInfo(new ArgumentInfo[]{ArgumentInfo.of(PString.class, PMap.of("template", "public void draw(){"))}, new Value[]{PString.EMPTY}, PMap.EMPTY));
        controls.put("ro1", ControlInfo.createReadOnlyPropertyInfo(new ArgumentInfo[]{PNumber.info(0, 1)}, PMap.EMPTY));
        controls.put("t1", ControlInfo.createActionInfo(PMap.of("key", "value")));
        
        Map<String, PortInfo> ports = new LinkedHashMap<>();
        ports.put("in", PortInfo.create(ControlPort.class, PortInfo.Direction.IN, PMap.EMPTY));
        ports.put("out", PortInfo.create(ControlPort.class, PortInfo.Direction.OUT, PMap.EMPTY));
        
        PMap properties = PMap.of(ComponentInfo.KEY_DYNAMIC, true);
        
        compareInfo = ComponentInfo.create(controls, ports, interfaces, properties);
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testCreate() {
        ComponentInfo info = Info.component(cmp -> cmp
                .protocol(ComponentProtocol.class)
                .protocol(StartableProtocol.class)
                .control("p1", c -> c.property()
                        .input(a -> a.number().min(0).max(1)).defaultValue(PNumber.ONE)
                        .property(ControlInfo.KEY_TRANSIENT, PBoolean.TRUE))
                .control("p2", c -> c.property()
                        .input(a -> a.string().template("public void draw(){"))
                        .defaultValue(PString.EMPTY))
                .control("ro1", c -> c.readOnlyProperty()
                        .output(a -> a.number().min(0).max(1)))
                .control("t1", c -> c.action().property("key", PString.of("value")))
                .port("in", p -> p.input(ControlPort.class))
                .port("out", p -> p.output(ControlPort.class))
                .property(ComponentInfo.KEY_DYNAMIC, PBoolean.TRUE)
                
        );
        
        System.out.println(info);
        System.out.println(compareInfo);
        assertEquals(compareInfo, info);
        
        ComponentInfo info2 = Info.component(cmp -> cmp.merge(info));
        assertEquals(info, info2);
    }
    
}
