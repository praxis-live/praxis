/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2018 Neil C Smith.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * version 3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License version 3
 * along with this work; if not, see http://www.gnu.org/licenses/
 *
 *
 * Please visit https://www.praxislive.org if you need additional information or
 * have any questions.
 *
 */
package org.praxislive.code;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.praxislive.code.userapi.AuxIn;
import org.praxislive.code.userapi.AuxOut;
import org.praxislive.code.userapi.Config;
import org.praxislive.code.userapi.Data;
import org.praxislive.code.userapi.ID;
import org.praxislive.code.userapi.In;
import org.praxislive.code.userapi.Inject;
import org.praxislive.code.userapi.Out;
import org.praxislive.code.userapi.P;
import org.praxislive.code.userapi.Property;
import org.praxislive.code.userapi.ReadOnly;
import org.praxislive.code.userapi.Ref;
import org.praxislive.code.userapi.T;
import org.praxislive.code.userapi.Type;
import org.praxislive.core.ArgumentInfo;
import org.praxislive.core.ControlAddress;
import org.praxislive.core.ComponentInfo;
import org.praxislive.core.ControlInfo;
import org.praxislive.core.Lookup;
import org.praxislive.core.PortInfo;
import org.praxislive.core.Value;
import org.praxislive.core.protocols.ComponentProtocol;
import org.praxislive.core.types.PMap;
import org.praxislive.core.types.PString;
import org.praxislive.logging.LogBuilder;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public abstract class CodeConnector<D extends CodeDelegate> {

    // adapted from http://stackoverflow.com/questions/2559759/how-do-i-convert-camelcase-into-human-readable-names-in-java
    private final static Pattern idRegex = Pattern.compile(
            String.format("%s|%s|%s|%s",
                    "(?<=.)_",
                    "(?<=[A-Z])(?=[A-Z][a-z])",
                    "(?<=[^A-Z])(?=[A-Z])",
                    "(?<=[A-Za-z])(?=[^A-Za-z])"
            )
    );

    private final static List<Plugin> ALL_PLUGINS
            = Lookup.SYSTEM.findAll(Plugin.class).collect(Collectors.toList());

    private final CodeFactory<D> factory;
    private final LogBuilder log;
    private final D delegate;
    private final Map<ControlDescriptor.Category, Map<Integer, ControlDescriptor>> controls;
    private final Map<PortDescriptor.Category, Map<Integer, PortDescriptor>> ports;
    private final Map<String, ReferenceDescriptor> refs;

    private List<Plugin> plugins;
    private Map<String, ControlDescriptor> extControls;
    private Map<String, PortDescriptor> extPorts;
    private Map<String, ReferenceDescriptor> extRefs;
    private ComponentInfo info;
    private int syntheticIdx = Integer.MIN_VALUE;
    private boolean hasPropertyField;

    public CodeConnector(CodeFactory.Task<D> task, D delegate) {
        this.factory = task.getFactory();
        this.log = task.getLog();
        this.delegate = delegate;
        controls = new EnumMap<>(ControlDescriptor.Category.class);
        ports = new EnumMap<>(PortDescriptor.Category.class);
        refs = new TreeMap<>();
    }

    protected void process() {
        plugins = ALL_PLUGINS.stream().filter(p -> p.isSupportedConnector(this))
                .collect(Collectors.toList());
        Class<? extends CodeDelegate> cls = delegate.getClass();
        analyseFields(cls.getDeclaredFields());
        analyseMethods(cls.getDeclaredMethods());
        addDefaultControls();
        addDefaultPorts();
        buildExternalData();
    }

    public D getDelegate() {
        return delegate;
    }

    public LogBuilder getLog() {
        return log;
    }

    protected Map<String, ControlDescriptor> extractControls() {
        return extControls;
    }

    protected Map<String, PortDescriptor> extractPorts() {
        return extPorts;
    }

    protected Map<String, ReferenceDescriptor> extractRefs() {
        return extRefs;
    }

    protected ComponentInfo extractInfo() {
        return info;
    }

    protected boolean requiresClock() {
        return hasPropertyField;
    }

    private void buildExternalData() {
        extControls = buildExternalControlMap();
        extPorts = buildExternalPortMap();
        extRefs = buildExternalRefsMap();
        info = buildComponentInfo(extControls, extPorts);
    }

    private Map<String, ControlDescriptor> buildExternalControlMap() {
        Map<String, ControlDescriptor> map = new LinkedHashMap<>();
        for (Map<Integer, ControlDescriptor> cat : controls.values()) {
            for (ControlDescriptor cd : cat.values()) {
                map.put(cd.getID(), cd);
            }
        }
        return map;
    }

    private Map<String, PortDescriptor> buildExternalPortMap() {
        Map<String, PortDescriptor> map = new LinkedHashMap<>();
        for (Map<Integer, PortDescriptor> cat : ports.values()) {
            for (PortDescriptor pd : cat.values()) {
                map.put(pd.getID(), pd);
            }
        }
        return map;
    }

    private Map<String, ReferenceDescriptor> buildExternalRefsMap() {
        if (refs.isEmpty()) {
            return Collections.EMPTY_MAP;
        } else {
            return new LinkedHashMap(refs);
        }
    }

    protected ComponentInfo buildComponentInfo(Map<String, ControlDescriptor> controls,
            Map<String, PortDescriptor> ports) {
        Map<String, ControlInfo> controlInfo = new LinkedHashMap<>(controls.size());
        for (Map.Entry<String, ControlDescriptor> e : controls.entrySet()) {
            if (!excludeFromInfo(e.getKey(), e.getValue())) {
                controlInfo.put(e.getKey(), e.getValue().getInfo());
            }
        }
        Map<String, PortInfo> portInfo = new LinkedHashMap<>(ports.size());
        for (Map.Entry<String, PortDescriptor> e : ports.entrySet()) {
            if (!excludeFromInfo(e.getKey(), e.getValue())) {
                portInfo.put(e.getKey(), e.getValue().getInfo());
            }
        }
        return ComponentInfo.create(controlInfo,
                portInfo,
                Collections.singleton(ComponentProtocol.class),
                PMap.of(
                        ComponentInfo.KEY_DYNAMIC, true,
                        ComponentInfo.KEY_COMPONENT_TYPE, factory.getComponentType()
                ));
    }

    private boolean excludeFromInfo(String id, ControlDescriptor desc) {
        return desc.getInfo() == null || id.startsWith("_");
    }

    private boolean excludeFromInfo(String id, PortDescriptor desc) {
        return id.startsWith("_");
    }

    public void addControl(ControlDescriptor ctl) {
        controls.computeIfAbsent(ctl.getCategory(), c -> new TreeMap<>()).put(ctl.getIndex(), ctl);
    }

    public void addPort(PortDescriptor port) {
        ports.computeIfAbsent(port.getCategory(), c -> new TreeMap<>()).put(port.getIndex(), port);
    }

    public void addReference(ReferenceDescriptor ref) {
        refs.put(ref.getID(), ref);
    }

    protected void addDefaultControls() {
        addControl(createInfoControl(Integer.MIN_VALUE));
        addControl(createCodeControl(Integer.MIN_VALUE + 1));
        addControl(new LogControl.Descriptor(Integer.MIN_VALUE + 2));
    }

    protected ControlDescriptor createInfoControl(int index) {
        return new InfoProperty.Descriptor(index);
    }

    protected ControlDescriptor createCodeControl(int index) {
        return new CodeProperty.Descriptor<>(factory, index);
    }
    
    protected void addDefaultPorts() {
        // no op hook
    }

    protected void analyseFields(Field[] fields) {
//        LOG.fine("Analysing fields");
        for (Field f : fields) {
            if (Modifier.isStatic(f.getModifiers())) {
                continue;
            }
            if (f.getType() == Property.class) {
                hasPropertyField = true;
            }
            analyseField(f);
        }
    }

    protected void analyseMethods(Method[] methods) {
        for (Method m : methods) {
            if (Modifier.isStatic(m.getModifiers())) {
                continue;
            }
            analyseMethod(m);
        }
    }

    protected void analyseField(Field field) {

        for (Plugin p : plugins) {
            if (p.analyseField(this, field)) {
                return;
            }
        }

        P prop = field.getAnnotation(P.class);
        if (prop != null) {
            if (analyseResourcePropertyField(prop, field)) {
                return;
            }
            if (analysePropertyField(prop, field)) {
                return;
            }
            if (analyseCustomPropertyField(prop, field)) {
                return;
            }
        }
        T trig = field.getAnnotation(T.class);
        if (trig != null && analyseTriggerField(trig, field)) {
            return;
        }
        In in = field.getAnnotation(In.class);
        if (in != null && analyseInputField(in, field)) {
            return;
        }
        AuxIn auxIn = field.getAnnotation(AuxIn.class);
        if (auxIn != null && analyseAuxInputField(auxIn, field)) {
            return;
        }
        Out out = field.getAnnotation(Out.class);
        if (out != null && analyseOutputField(out, field)) {
            return;
        }
        AuxOut aux = field.getAnnotation(AuxOut.class);
        if (aux != null && analyseAuxOutputField(aux, field)) {
            return;
        }
        Inject inject = field.getAnnotation(Inject.class);
        if (inject != null && analyseInjectField(inject, field)) {
            return;
        }
    }

    protected void analyseMethod(Method method) {

        for (Plugin p : plugins) {
            if (p.analyseMethod(this, method)) {
                return;
            }
        }

        T trig = method.getAnnotation(T.class);
        if (trig != null && analyseTriggerMethod(trig, method)) {
            return;
        }
        In in = method.getAnnotation(In.class);
        if (in != null && analyseInputMethod(in, method)) {
            return;
        }
        AuxIn aux = method.getAnnotation(AuxIn.class);
        if (aux != null && analyseAuxInputMethod(aux, method)) {
            return;
        }
    }

    private boolean analyseInputField(In ann, Field field) {
        InputImpl.Descriptor odsc = InputImpl.createDescriptor(this, ann, field);
        if (odsc != null) {
            addPort(odsc);
            addControl(InputPortControl.Descriptor.createInput(odsc.getID(), odsc.getIndex(), odsc));
            return true;
        }
        
        DataPort.InputDescriptor din = DataPort.InputDescriptor.create(this, ann, field);
        if (din != null) {
            addPort(din);
            return true;
        }
        
        return false;
    }

    private boolean analyseAuxInputField(AuxIn ann, Field field) {
        InputImpl.Descriptor odsc = InputImpl.createDescriptor(this, ann, field);
        if (odsc != null) {
            addPort(odsc);
            addControl(InputPortControl.Descriptor.createAuxInput(odsc.getID(), odsc.getIndex(), odsc));
            return true;
        }
        
        DataPort.InputDescriptor din = DataPort.InputDescriptor.create(this, ann, field);
        if (din != null) {
            addPort(din);
            return true;
        }
        
        return false;
        
    }

    private boolean analyseOutputField(Out ann, Field field) {
        OutputImpl.Descriptor odsc = OutputImpl.createDescriptor(this, ann, field);
        if (odsc != null) {
            addPort(odsc);
            return true;
        } 
        
        DataPort.OutputDescriptor dout = DataPort.OutputDescriptor.create(this, ann, field);
        if (dout != null) {
            addPort(dout);
            return true;
        }
        
        return false;
    }

    private boolean analyseAuxOutputField(AuxOut ann, Field field) {
        OutputImpl.Descriptor odsc = OutputImpl.createDescriptor(this, ann, field);
        if (odsc != null) {
            addPort(odsc);
            return true;
        }
        
        DataPort.OutputDescriptor dout = DataPort.OutputDescriptor.create(this, ann, field);
        if (dout != null) {
            addPort(dout);
            return true;
        }
        
        return false;
        
    }

    private boolean analyseTriggerField(T ann, Field field) {
        TriggerControl.Descriptor tdsc
                = TriggerControl.Descriptor.create(this, ann, field);
        if (tdsc != null) {
            addControl(tdsc);
            if (shouldAddPort(field)) {
                addPort(tdsc.createPortDescriptor());
            }
            return true;
        } else {
            return false;
        }
    }
    
    private boolean analyseResourcePropertyField(P ann, Field field) {
        if (field.getAnnotation(Type.Resource.class) != null &&
                String.class.equals(field.getType())) {
            ResourceProperty.Descriptor<String> rpd =
                    ResourceProperty.Descriptor.create(this, ann, field, ResourceProperty.getStringLoader());
            if (rpd != null) {
                addControl(rpd);
                if (shouldAddPort(field)) {
                    addPort(rpd.createPortDescriptor());
                }
                return true;
            }
        }
        return false;
    }

    private boolean analysePropertyField(P ann, Field field) {
        PropertyControl.Descriptor pdsc
                = PropertyControl.Descriptor.create(this, ann, field);
        if (pdsc != null) {
            addControl(pdsc);
            if (shouldAddPort(field)) {
                addPort(pdsc.createPortDescriptor());
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean analyseCustomPropertyField(P ann, Field field) {
        TypeConverter<?> converter = TypeConverter.find(field.getType());
        if (converter == null) {
            return false;
        }
        TypeConverterProperty.Descriptor<?> tcpd
                = TypeConverterProperty.Descriptor.create(this, ann, field, converter);
        if (tcpd != null) {
            addControl(tcpd);
//            if (shouldAddPort(field)) {
//                addPort(tcpd.createPortDescriptor());
//            }
            return true;
        }
        return false;
    }

    private boolean analyseInjectField(Inject ann, Field field) {

        if (Ref.class.equals(field.getType())) {
            RefImpl.Descriptor rdsc = RefImpl.Descriptor.create(this, field);
            if (rdsc != null) {
                addReference(rdsc);
                addControl(rdsc.getControlDescriptor());
                return true;
            } else {
                return false;
            }
        }
        
        if (Data.Sink.class.equals(field.getType())) {
            DataSink.Descriptor dsdsc = DataSink.Descriptor.create(this, field);
            if (dsdsc != null) {
                addReference(dsdsc);
                return true;
            } else {
                return false;
            }
        }

        PropertyControl.Descriptor pdsc
                = PropertyControl.Descriptor.create(this, ann, field);
        if (pdsc != null) {
            addControl(pdsc);
            return true;
        } else {
            return false;
        }
    }

    private boolean analyseTriggerMethod(T ann, Method method) {
        TriggerControl.Descriptor tdsc
                = TriggerControl.Descriptor.create(this, ann, method);
        if (tdsc != null) {
            addControl(tdsc);
            if (shouldAddPort(method)) {
                addPort(tdsc.createPortDescriptor());
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean analyseInputMethod(In ann, Method method) {
        MethodInput.Descriptor desc
                = MethodInput.createDescriptor(this, ann, method);
        if (desc != null) {
            addPort(desc);
            addControl(InputPortControl.Descriptor.createInput(desc.getID(), desc.getIndex(), desc));
            return true;
        } else {
            return false;
        }
    }

    private boolean analyseAuxInputMethod(AuxIn ann, Method method) {
        MethodInput.Descriptor desc
                = MethodInput.createDescriptor(this, ann, method);
        if (desc != null) {
            addPort(desc);
            addControl(InputPortControl.Descriptor.createAuxInput(desc.getID(), desc.getIndex(), desc));
            return true;
        } else {
            return false;
        }
    }

    public String findID(Field field) {
        ID ann = field.getAnnotation(ID.class);
        if (ann != null) {
            String id = ann.value();
            if (ControlAddress.isValidID(id)) {
                return id;
            }
        }
        return javaNameToID(field.getName());
    }

    public String findID(Method method) {
        ID ann = method.getAnnotation(ID.class);
        if (ann != null) {
            String id = ann.value();
            if (ControlAddress.isValidID(id)) {
                return id;
            }
        }
        return javaNameToID(method.getName());
    }

    protected String javaNameToID(String javaName) {
        String ret = idRegex.matcher(javaName).replaceAll("-");
        return ret.toLowerCase();
    }

    public boolean shouldAddPort(AnnotatedElement element) {
        if (element.isAnnotationPresent(ReadOnly.class)) {
            return false;
        }
        Config.Port port = element.getAnnotation(Config.Port.class);
        if (port != null) {
            return port.value();
        }
        return true;
    }

    public int getSyntheticIndex() {
        return syntheticIdx++;
    }

    @SuppressWarnings("deprecation")
    ArgumentInfo infoFromType(Type typeAnnotation) {
        Class<? extends Value> valueCls = typeAnnotation.value() == Value.class
                ? typeAnnotation.cls() : typeAnnotation.value();
        PMap properties = createPropertyMap(typeAnnotation.properties());
        return ArgumentInfo.of(valueCls, properties);
    }

    private PMap createPropertyMap(String... properties) {
        if (properties.length == 0) {
            return PMap.EMPTY;
        }
        if (properties.length % 2 != 0) {
            throw new IllegalArgumentException();
        }
        PMap.Builder bld = PMap.builder(properties.length / 2);
        for (int i = 0; i < properties.length; i += 2) {
            bld.put(properties[i], properties[i + 1]);
        }
        return bld.build();
    }

    @SuppressWarnings("deprecation")
    Value defaultValueFromType(Type typeAnnotation) {
        Class<Value> valueCls = typeAnnotation.value() == Value.class
                ? (Class<Value>) typeAnnotation.cls()
                : (Class<Value>) typeAnnotation.value();
        Value.Type<Value> valueType = Value.Type.of(valueCls);
        String defaultString = typeAnnotation.def();
        return valueType.converter().apply(PString.of(defaultString)).orElse(PString.EMPTY);
    }

    public static interface Plugin {

        default boolean analyseField(CodeConnector<?> connector, Field field) {
            return false;
        }

        default boolean analyseMethod(CodeConnector<?> connector, Method method) {
            return false;
        }

        default boolean isSupportedConnector(CodeConnector<?> connector) {
            return true;
        }

    }

}
