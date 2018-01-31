/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2017 Neil C Smith.
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
package org.praxislive.code;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.praxislive.core.Call;
import org.praxislive.core.CallArguments;
import org.praxislive.core.ComponentAddress;
import org.praxislive.core.Control;
import org.praxislive.core.ControlAddress;
import org.praxislive.core.ExecutionContext;
import org.praxislive.core.InterfaceDefinition;
import org.praxislive.core.Lookup;
import org.praxislive.core.PacketRouter;
import org.praxislive.core.Port;
import org.praxislive.core.info.ComponentInfo;
import org.praxislive.core.interfaces.ServiceUnavailableException;
import org.praxislive.core.interfaces.Services;
import org.praxislive.logging.LogBuilder;
import org.praxislive.logging.LogLevel;
import org.praxislive.util.ArrayUtils;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public abstract class CodeContext<D extends CodeDelegate> {

    private final Map<String, ControlDescriptor> controls;
    private final Map<String, PortDescriptor> ports;
    private final Map<String, RefDescriptor> refs;
    private final ComponentInfo info;

    private final D delegate;
    private final LogBuilder log;
    private final Driver driver;
    private final boolean requireClock;

    private ExecutionContext execCtxt;
    private ExecutionContext.State execState = ExecutionContext.State.NEW;
    private CodeComponent<D> cmp;
    private long time;
    private ClockListener[] clockListeners;

    protected CodeContext(CodeConnector<D> connector) {
        this(connector, false);
    }

    protected CodeContext(CodeConnector<D> connector, boolean requireClock) {
        this.driver = new Driver();
        clockListeners = new ClockListener[0];
        time = System.nanoTime() - 10 * 1000_000_000;
        // @TODO what is maximum allowed amount a root can be behind system time?
        try {
            connector.process();
            controls = connector.extractControls();
            ports = connector.extractPorts();
            refs = connector.extractRefs();
            info = connector.extractInfo();
            delegate = connector.getDelegate();
            log = new LogBuilder(LogLevel.ERROR);
            this.requireClock = requireClock || connector.requiresClock();
        } catch (Exception e) {
            Logger.getLogger(CodeContext.class.getName()).log(Level.FINE, "", e);
            throw e;
        }
    }

    void setComponent(CodeComponent<D> cmp) {
        this.cmp = cmp;
        delegate.setContext(this);
    }

    void handleConfigure(CodeComponent<D> cmp, CodeContext<D> oldCtxt) {
        configureControls(oldCtxt);
        configurePorts(oldCtxt);
        configureRefs(oldCtxt);
        configure(cmp, oldCtxt);
    }
    
    protected void configure(CodeComponent<D> cmp, CodeContext<D> oldCtxt) {
    }

    private void configureControls(CodeContext<D> oldCtxt) {
        Map<String, ControlDescriptor> oldControls = oldCtxt == null
                ? Collections.<String, ControlDescriptor>emptyMap() : oldCtxt.controls;
        for (Map.Entry<String, ControlDescriptor> entry : controls.entrySet()) {
            ControlDescriptor oldCD = oldControls.remove(entry.getKey());
            if (oldCD != null) {
                entry.getValue().attach(this, oldCD.getControl());
//                oldCD.dispose();
            } else {
                entry.getValue().attach(this, null);
            }
        }
    }

    private void configurePorts(CodeContext<D> oldCtxt) {
        Map<String, PortDescriptor> oldPorts = oldCtxt == null
                ? Collections.<String, PortDescriptor>emptyMap() : oldCtxt.ports;
        for (Map.Entry<String, PortDescriptor> entry : ports.entrySet()) {
            PortDescriptor oldPD = oldPorts.remove(entry.getKey());
            if (oldPD != null) {
                entry.getValue().attach(this, oldPD.getPort());
//                oldPD.dispose();
            } else {
                entry.getValue().attach(this, null);
            }
        }
        for (PortDescriptor oldPD : oldPorts.values()) {
            oldPD.getPort().disconnectAll();
        }
    }
    
    private void configureRefs(CodeContext<D> oldCtxt) {
        Map<String, RefDescriptor> oldRefs = oldCtxt == null
                ? Collections.EMPTY_MAP : oldCtxt.refs;
        refs.forEach( (id, ref) -> ref.attach(this, oldRefs.remove(id)));
        oldRefs.forEach( (id, ref) -> ref.dispose() );
    }

    final void handleHierarchyChanged() {
        hierarchyChanged();
        
        LogLevel level = getLookup().get(LogLevel.class);
        if (level == null) {
            level = LogLevel.ERROR;
        }
        log.setLevel(level);
        
        ExecutionContext ctxt = cmp == null ? null : cmp.getExecutionContext();
        if (execCtxt != ctxt) {
            if (execCtxt != null) {
                execCtxt.removeStateListener(driver);
                execCtxt.removeClockListener(driver);
            }
            execCtxt = ctxt;
            if (ctxt != null) {
                ctxt.addStateListener(driver);
                if (requireClock) {
                    ctxt.addClockListener(driver);
                }
                handleStateChanged(ctxt, false);
            }
        }
    }

    protected void hierarchyChanged() {
    }

    final void handleStateChanged(ExecutionContext source, boolean full) {
        if (execState == source.getState()) {
            return;
        }
        reset(full);
        update(source.getTime());
        execState = source.getState();
        if (execState == ExecutionContext.State.ACTIVE) {
            starting(source, full);
        } else {
            stopping(source, full);
        }
        flush();
    }
    
    protected void starting(ExecutionContext source, boolean fullStart) {
        starting(source);
    }

    protected void starting(ExecutionContext source) {
    }
    
    protected void stopping(ExecutionContext source, boolean fullStop) {
        stopping(source);
    }
    
    protected void stopping(ExecutionContext source) {
    }

    final void handleTick(ExecutionContext source) {
        update(source.getTime());
        tick(source);
        flush();
    }

    protected void tick(ExecutionContext source) {
    }
    
    @Deprecated
    protected final void reset() {
        reset(false);
    }
    
    protected final void reset(boolean full) {
        controls.values().forEach(cd -> cd.reset(full));
        ports.values().forEach(pd -> pd.reset(full));
        refs.values().forEach(rd -> rd.reset(full));
    }
    

    final void handleDispose() {
        cmp = null;
        handleHierarchyChanged();
        refs.values().forEach(RefDescriptor::dispose);
        refs.clear();
        controls.clear();
        ports.clear();
        dispose();
    }

    protected void dispose() {
    }

    public CodeComponent<D> getComponent() {
        return cmp;
    }

    public D getDelegate() {
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
    public ComponentAddress findService(Class<? extends InterfaceDefinition> type)
            throws ServiceUnavailableException {
        Services sm = getLookup().get(Services.class);
        if (sm == null) {
            throw new ServiceUnavailableException("Can't find Service Manager");
        }
        return sm.findService(type);
    }

    public long getTime() {
        return time;
    }

    public void addClockListener(ClockListener listener) {
        clockListeners = ArrayUtils.add(clockListeners, listener);
    }

    public void removeClockListener(ClockListener listener) {
        clockListeners = ArrayUtils.remove(clockListeners, listener);
    }

    protected ExecutionContext getExecutionContext() {
        return cmp == null ? null : cmp.getExecutionContext();
    }
    
    @Deprecated
    protected boolean isActive() {
        ExecutionContext ctxt = getExecutionContext();
        return ctxt == null ? false : ctxt.getState() == ExecutionContext.State.ACTIVE;
    }
    
    protected boolean checkActive() {
        if (execState == ExecutionContext.State.ACTIVE) {
            return true;
        }
        if (execCtxt != null) {
            if (execCtxt.getState() == ExecutionContext.State.ACTIVE) {
                handleStateChanged(execCtxt, true);
                return execState == ExecutionContext.State.ACTIVE;
            }
        }
        return false;
    }

    protected void update(long time) {
        if (time - this.time > 0) {
            this.time = time;
            for (ClockListener l : clockListeners) {
                l.tick();
            }
        }
    }

    public void invoke(long time, Invoker invoker) {
        if (checkActive()) {
            update(time);
            try {
                invoker.invoke();
            } catch (Exception ex) {
                log.log(LogLevel.ERROR, ex);
            }
            flush();
        }
    }

    void invoke(long time, Method method, Object... params) {
        if (checkActive()) {
            update(time);
            try {
                method.invoke(getDelegate(), params);
            } catch (Exception ex) {
                if (ex instanceof InvocationTargetException) {
                    Throwable t = ex.getCause();
                    ex = t instanceof Exception ? (Exception) t : ex;
                }
                StringBuilder sb = new StringBuilder("Exception thrown from ");
                sb.append(method.getName());
                sb.append('(');
                Class<?>[] types = method.getParameterTypes();
                for (int i = 0; i < types.length; i++) {
                    sb.append(types[i].getSimpleName());
                    if (i < (types.length - 1)) {
                        sb.append(',');
                    }
                }
                sb.append(')');
                log.log(LogLevel.ERROR, ex, sb.toString());
            }
            flush();
        }
    }

    protected void flush() {
        if (!log.isEmpty()) {
            log(log.toCallArguments());
            log.clear();
        }
    }

    public LogBuilder getLog() {
        return log;
    }

    protected LogLevel getLogLevel() {
        return log.getLevel();
    }

    protected void log(LogBuilder log) {
        if (log.isEmpty()) {
            return;
        }
        log(log.toCallArguments());
    }

    private void log(CallArguments args) {
        PacketRouter router = cmp.getPacketRouter();
        ControlAddress to = cmp.getLogToAddress();
        ControlAddress from = cmp.getLogFromAddress();
        if (router == null || to == null) {
            return;
        }
        router.route(Call.createCall(to, from, time, args));
    }

    public static interface ClockListener {

        public void tick();

    }

    public static interface Invoker {

        public void invoke();

    }

    private class Driver implements ExecutionContext.StateListener,
            ExecutionContext.ClockListener {

        @Override
        public void stateChanged(ExecutionContext source) {
            handleStateChanged(source, true);
        }

        @Override
        public void tick(ExecutionContext source) {
            handleTick(source);
        }

    }

}
