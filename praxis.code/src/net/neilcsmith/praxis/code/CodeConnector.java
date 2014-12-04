/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Neil C Smith.
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
package net.neilcsmith.praxis.code;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import net.neilcsmith.praxis.code.userapi.ID;
import net.neilcsmith.praxis.code.userapi.Out;
import net.neilcsmith.praxis.code.userapi.Output;
import net.neilcsmith.praxis.code.userapi.P;
import net.neilcsmith.praxis.code.userapi.Port;
import net.neilcsmith.praxis.code.userapi.T;
import net.neilcsmith.praxis.core.ControlAddress;
import net.neilcsmith.praxis.core.InterfaceDefinition;
import net.neilcsmith.praxis.core.info.ComponentInfo;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.core.info.PortInfo;
import net.neilcsmith.praxis.core.interfaces.ComponentInterface;
import net.neilcsmith.praxis.core.types.PMap;
import net.neilcsmith.praxis.logging.LogBuilder;

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

    private Map<String, ControlDescriptor> extControls;
    private Map<String, PortDescriptor> extPorts;
    private ComponentInfo info;

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
    }

    protected void process() {
        Class<? extends CodeDelegate> cls = delegate.getClass();
        analyseFields(cls.getDeclaredFields());
        analyseMethods(cls.getDeclaredMethods());
        addDefaultControls();
        addDefaultPorts();
        buildExternalData();
    }

    protected D getDelegate() {
        return delegate;
    }

    protected LogBuilder getLog() {
        return log;
    }

    protected Map<String, ControlDescriptor> extractControls() {
        return extControls;
    }

    protected Map<String, PortDescriptor> extractPorts() {
        return extPorts;
    }

    protected ComponentInfo extractInfo() {
        return info;
    }

    private void buildExternalData() {
        extControls = buildExternalControlMap();
        extPorts = buildExternalPortMap();
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

    protected ComponentInfo buildComponentInfo(Map<String, ControlDescriptor> controls,
            Map<String, PortDescriptor> ports) {
//        LOG.fine("Building component info");
//        LOG.fine("Building control info");
        Map<String, ControlInfo> controlInfo = new LinkedHashMap<>(controls.size());
        for (Map.Entry<String, ControlDescriptor> e : controls.entrySet()) {
//            LOG.log(Level.FINE, "Adding {0}\n{1}", new Object[]{e.getKey(), e.getValue().getInfo()});
            controlInfo.put(e.getKey(), e.getValue().getInfo());
        }
//        LOG.fine("Building port info");
        Map<String, PortInfo> portInfo = new LinkedHashMap<>(ports.size());
        for (Map.Entry<String, PortDescriptor> e : ports.entrySet()) {
//            LOG.log(Level.FINE, "Adding {0}\n{1}", new Object[]{e.getKey(), e.getValue().getInfo()});
            portInfo.put(e.getKey(), e.getValue().getInfo());
        }
        return ComponentInfo.create(
                controlInfo,
                portInfo,
                Collections.<Class<? extends InterfaceDefinition>>singleton(ComponentInterface.class),
                PMap.create(ComponentInfo.KEY_DYNAMIC, true));
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
            analyseField(f);
        }
    }

    protected void analyseMethods(Method[] methods) {
        for (Method m : methods) {
            analyseMethod(m);
        }
    }

    protected void analyseField(Field field) {
//        LOG.log(Level.FINE, "Analysing field : {0}", field);

        P prop = field.getAnnotation(P.class);
        if (prop != null && analysePropertyField(prop, field)) {
            return;
        }
        T trig = field.getAnnotation(T.class);
        if (trig != null && analyseTriggerField(trig, field)) {
            return;
        }
        Out out = field.getAnnotation(Out.class);
        if (out != null && analyseOutputField(out, field)) {
            return;
        }
    }

    protected void analyseMethod(Method method) {
        T trig = method.getAnnotation(T.class);
        if (trig != null && analyseTriggerMethod(trig, method)) {
            return;
        }
    }

    protected boolean analyseOutputField(Out ann, Field field) {
//        LOG.log(Level.FINE, "Field is output");
        int index = ann.value();
        if (Output.class.isAssignableFrom(field.getType())) {
            DefaultOutput.Descriptor odsc = new DefaultOutput.Descriptor(
                    findID(field),
                    index,
                    field
            );
//            LOG.log(Level.FINE, "Adding port for output {0}", field.getName());
            addPort(odsc);
            return true;
        }
        return false;
    }

    protected boolean analyseTriggerField(T ann, Field field) {
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

    protected boolean analysePropertyField(P ann, Field field) {
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

    protected boolean analyseTriggerMethod(T ann, Method method) {
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

    protected String findID(Field field) {
        ID ann = field.getAnnotation(ID.class);
        if (ann != null) {
            String id = ann.value();
            if (ControlAddress.isValidID(id)) {
                return id;
            }
        }
        return javaNameToID(field.getName());
    }

    protected String findID(Method method) {
        ID ann = method.getAnnotation(ID.class);
        if (ann != null) {
            String id = ann.value();
            if (ControlAddress.isValidID(id)) {
                return id;
            }
        }
        return javaNameToID(method.getName());
    }

    private String javaNameToID(String javaName) {
        String ret = idRegex.matcher(javaName).replaceAll("-");
        return ret.toLowerCase();
    }

    private boolean shouldAddPort(AnnotatedElement element) {
        Port ann = element.getAnnotation(Port.class);
        if (ann != null) {
            return ann.value();
        }
        return true;
    }

}
