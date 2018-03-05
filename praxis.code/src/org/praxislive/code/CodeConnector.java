/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2018 Neil C Smith.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License version 3
 * along with this work; if not, see http://www.gnu.org/licenses/
 *
 *
 * Please visit http://neilcsmith.net if you need additional information or
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
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import org.praxislive.code.userapi.AuxIn;
import org.praxislive.code.userapi.AuxOut;
import org.praxislive.code.userapi.Config;
import org.praxislive.code.userapi.ID;
import org.praxislive.code.userapi.In;
import org.praxislive.code.userapi.Inject;
import org.praxislive.code.userapi.Out;
import org.praxislive.code.userapi.P;
import org.praxislive.code.userapi.Port;
import org.praxislive.code.userapi.ReadOnly;
import org.praxislive.code.userapi.Ref;
import org.praxislive.code.userapi.T;
import org.praxislive.core.ControlAddress;
import org.praxislive.core.ComponentInfo;
import org.praxislive.core.ControlInfo;
import org.praxislive.core.PortInfo;
import org.praxislive.core.protocols.ComponentProtocol;
import org.praxislive.core.types.PMap;
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

    private final CodeFactory<D> factory;
    private final LogBuilder log;
    private final D delegate;
    private final Map<ControlDescriptor.Category, Map<Integer, ControlDescriptor>> controls;
    private final Map<PortDescriptor.Category, Map<Integer, PortDescriptor>> ports;
    private final Map<String, RefDescriptor> refs;

    private Map<String, ControlDescriptor> extControls;
    private Map<String, PortDescriptor> extPorts;
    private Map<String, RefDescriptor> extRefs;
    private ComponentInfo info;
    private int syntheticIdx = Integer.MIN_VALUE;

    public CodeConnector(CodeFactory.Task<D> task, D delegate) {
        this.factory = task.getFactory();
        this.log = task.getLog();
        this.delegate = delegate;
        controls = new EnumMap<>(ControlDescriptor.Category.class);
        for (ControlDescriptor.Category cat : ControlDescriptor.Category.values()) {
            controls.put(cat, new TreeMap<Integer, ControlDescriptor>());
        }
        ports = new EnumMap<>(PortDescriptor.Category.class);
        for (PortDescriptor.Category cat : PortDescriptor.Category.values()) {
            ports.put(cat, new TreeMap<Integer, PortDescriptor>());
        }
        refs = new TreeMap<>();
    }

    protected void process() {
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
    
    /*protected*/ Map<String, RefDescriptor> extractRefs() {
        return extRefs;
    }

    protected ComponentInfo extractInfo() {
        return info;
    }
    
    protected boolean requiresClock() {
        return false;
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
    
    private Map<String, RefDescriptor> buildExternalRefsMap() {
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
                PMap.create(ComponentInfo.KEY_DYNAMIC, true));
    }
    
    private boolean excludeFromInfo(String id, ControlDescriptor desc) {
        return desc.getInfo() == null || id.startsWith("_");
    }
    
    private boolean excludeFromInfo(String id, PortDescriptor desc) {
        return id.startsWith("_");
    }

    protected void addControl(ControlDescriptor ctl) {
        controls.get(ctl.getCategory()).put(ctl.getIndex(), ctl);
    }

    protected void addPort(PortDescriptor port) {
        ports.get(port.getCategory()).put(port.getIndex(), port);
    }

    protected void addDefaultControls() {
        addControl(createInfoControl(Integer.MIN_VALUE));
        addControl(createCodeControl(Integer.MIN_VALUE + 1));
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

        P prop = field.getAnnotation(P.class);
        if (prop != null && analysePropertyField(prop, field)) {
            return;
        }
        if (prop != null && analyseCustomPropertyField(prop, field)) {
            return;
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
            return true;
        } else {
            return false;
        }
    }

    private boolean analyseAuxInputField(AuxIn ann, Field field) {
        InputImpl.Descriptor odsc = InputImpl.createDescriptor(this, ann, field);
        if (odsc != null) {
            addPort(odsc);
            return true;
        } else {
            return false;
        }
    }

    private boolean analyseOutputField(Out ann, Field field) {
        OutputImpl.Descriptor odsc = OutputImpl.createDescriptor(this, ann, field);
        if (odsc != null) {
            addPort(odsc);
            return true;
        } else {
            return false;
        }
    }

    private boolean analyseAuxOutputField(AuxOut ann, Field field) {
        OutputImpl.Descriptor odsc = OutputImpl.createDescriptor(this, ann, field);
        if (odsc != null) {
            addPort(odsc);
            return true;
        } else {
            return false;
        }
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
        TypeConverterProperty.Descriptor<?> tcpd =
                TypeConverterProperty.Descriptor.create(this, ann, field, converter);
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
            RefDescriptor rdsc = RefDescriptor.create(this, field);
            if (rdsc != null) {
                refs.put(rdsc.getID(), rdsc);
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

    protected boolean shouldAddPort(AnnotatedElement element) {
        if (element.isAnnotationPresent(ReadOnly.class)) {
            return false;
        }
        Config.Port port = element.getAnnotation(Config.Port.class);
        if (port != null) {
            return port.value();
        }
        Port ann = element.getAnnotation(Port.class);
        if (ann != null) {
            return ann.value();
        }
        return true;
    }

    protected int getSyntheticIndex() {
        return syntheticIdx++;
    }
    
}
