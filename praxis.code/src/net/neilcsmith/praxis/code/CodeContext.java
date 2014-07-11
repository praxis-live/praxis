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
 */
package net.neilcsmith.praxis.code;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.ComponentAddress;
import net.neilcsmith.praxis.core.Control;
import net.neilcsmith.praxis.core.ControlAddress;
import net.neilcsmith.praxis.core.InterfaceDefinition;
import net.neilcsmith.praxis.core.Lookup;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.core.info.ComponentInfo;
import net.neilcsmith.praxis.core.interfaces.ServiceManager;
import net.neilcsmith.praxis.core.interfaces.ServiceUnavailableException;
import net.neilcsmith.praxis.core.interfaces.TaskService;
import net.neilcsmith.praxis.util.ArrayUtils;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public abstract class CodeContext<T extends CodeDelegate> {

    private final static Logger LOG = Logger.getLogger(CodeContext.class.getName());
    
//    protected final static String INPUTS = "Inputs";
//    protected final static String OUTPUTS = "Outputs";
//    protected final static String PROPERTIES = "Properties";
//    protected final static String TRIGGERS = "Triggers";

    private final Map<String, ControlDescriptor> controls;
    private final Map<String, PortDescriptor> ports;
    private final ComponentInfo info;

    private final T delegate;

    private CodeComponent<T> cmp;

    private ClockListener[] clockListeners;

    public CodeContext(CodeConnector<T> connector) {
        clockListeners = new ClockListener[0];
        try {
            connector.process();
            controls = connector.extractControls();
            ports = connector.extractPorts();
            info = connector.extractInfo();
            delegate = connector.getDelegate();
        } catch (Exception e) {
            LOG.log(Level.FINE, "", e);
            throw e;
        }
    }

    protected void configure(CodeComponent<T> cmp, CodeContext<T> oldCtxt) {
        this.cmp = cmp;
        configureControls(oldCtxt);
        configurePorts(oldCtxt);
        hierarchyChanged();
        delegate.setContext(this);
    }
    
    private void configureControls(CodeContext<T> oldCtxt) {
        Map<String, ControlDescriptor> oldControls = oldCtxt == null ?
                Collections.<String, ControlDescriptor>emptyMap() : oldCtxt.controls;
        for (Map.Entry<String, ControlDescriptor> entry : controls.entrySet()) {
            ControlDescriptor oldCD = oldControls.remove(entry.getKey());
            if (oldCD != null) {
                entry.getValue().attach(this, oldCD.getControl());
            } else {
                entry.getValue().attach(this, null);
            }
        }
    }

    private void configurePorts(CodeContext<T> oldCtxt) {
        Map<String, PortDescriptor> oldPorts = oldCtxt == null ?
                Collections.<String, PortDescriptor>emptyMap() : oldCtxt.ports;
        for (Map.Entry<String, PortDescriptor> entry : ports.entrySet()) {
            PortDescriptor oldPD = oldPorts.remove(entry.getKey());
            if (oldPD != null) {
                entry.getValue().attach(this, oldPD.getPort());
            } else {
                entry.getValue().attach(this, null);
            }
        }
        for (PortDescriptor oldPD : oldPorts.values()) {
            oldPD.getPort().disconnectAll();
        }
    }
    
    protected void hierarchyChanged() {
        //no op hook
    }

    protected void dispose() {
        cmp = null;
        hierarchyChanged();
        controls.clear();
        ports.clear();
    }
    
    public CodeComponent<T> getComponent() {
        return cmp;
    }
    
    public T getDelegate() {
        return delegate;
    }

    protected Control getControl(String id) {
        ControlDescriptor cd = controls.get(id);
        return cd == null ? null : cd.getControl();
    }
    
    protected ControlDescriptor getControlDescriptor(String id) {
        return controls.get(id);
    }

    protected String[] getControlIDs() {
        Set<String> keySet = controls.keySet();
        return keySet.toArray(new String[keySet.size()]);
    }

    protected Port getPort(String id) {
        PortDescriptor pd = ports.get(id);
        return pd == null ? null : pd.getPort();
    }
    
    protected PortDescriptor getPortDescriptor(String id) {
        return ports.get(id);
    }

    protected String[] getPortIDs() {
        Set<String> keySet = ports.keySet();
        return keySet.toArray(new String[keySet.size()]);
    }

    protected ComponentInfo getInfo() {
        return info;
    }

    protected InterfaceDefinition[] getInterfaces() {
        return new InterfaceDefinition[0];
    }

    protected ControlAddress getAddress(Control control) {
        ComponentAddress ad = cmp == null ? null : cmp.getAddress();
        if (ad != null) {
            for (Map.Entry<String, ControlDescriptor> ce : controls.entrySet()) {
                if (ce.getValue().getControl() == control) {
                    return ControlAddress.create(ad, ce.getKey());
                }
            }
        }
        return null;
    }

    public Lookup getLookup() {
        return cmp == null ? Lookup.EMPTY : cmp.getLookup();
    }

    // @TODO implement caching?
    public ComponentAddress findService(InterfaceDefinition service)
            throws ServiceUnavailableException {
        ServiceManager sm = getLookup().get(ServiceManager.class);
        if (sm == null) {
            throw new ServiceUnavailableException("Can't find Service Manager");
        }
        return sm.findService(TaskService.INSTANCE);

    }

    public abstract long getTime();

    public void addClockListener(ClockListener listener) {
        clockListeners = ArrayUtils.add(clockListeners, listener);
    }

    public void removeClockListener(ClockListener listener) {
        clockListeners = ArrayUtils.remove(clockListeners, listener);
    }

    protected void processClock() {
        for (ClockListener l : clockListeners) {
            l.tick();
        }
    }

    

    public static interface ClockListener {

        public void tick();

    }


}
