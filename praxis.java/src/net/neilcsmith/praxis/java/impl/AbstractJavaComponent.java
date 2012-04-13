/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Neil C Smith.
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

package net.neilcsmith.praxis.java.impl;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ControlPort;
import net.neilcsmith.praxis.core.ExecutionContext;
import net.neilcsmith.praxis.core.types.PString;
import net.neilcsmith.praxis.impl.AbstractClockComponent;
import net.neilcsmith.praxis.impl.ArgumentProperty;
import net.neilcsmith.praxis.impl.DefaultControlOutputPort;
import net.neilcsmith.praxis.impl.ListenerUtils;
import net.neilcsmith.praxis.impl.TriggerControl;
import net.neilcsmith.praxis.java.CodeContext;
import net.neilcsmith.praxis.java.CodeDelegate;
import net.neilcsmith.praxis.java.Output;
import net.neilcsmith.praxis.java.Param;
import net.neilcsmith.praxis.java.Trigger;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public abstract class AbstractJavaComponent extends AbstractClockComponent {

    private Clock clock;
    private Param[] params;
    private Trigger[] triggers;
    private Output[] outputs;
    private CodeDelegate delegate;
    private DelegateContext context;
    private long time;

    protected AbstractJavaComponent() {
        // in case subclasses don't build
        params = new Param[0];
        triggers = new Trigger[0];
        outputs = new Output[0];
        clock = new Clock();
    }

    // allow subclasses to build after ins, outs, code controls, etc.
    protected void buildParams(String prefix, int count, int ports) {
        if (count < 1) {
            params = new Param[0];
        } else {
            params = new Param[count];
            for (int i=0; i < count; i++) {
                Param param = new Param(clock);
                params[i] = param;
                ArgumentProperty control = ArgumentProperty.create(param, PString.EMPTY);
                registerControl(prefix + (i + 1), control);
                if (i < ports) {
                    registerPort(prefix + (i + 1), control.createPort());
                }
            }
        }
    }

    protected void buildTriggers(String prefix, int count, int ports) {
        if (count < 1) {
            triggers = new Trigger[0];
        } else {
            triggers = new Trigger[count];
            for (int i=0; i < count; i++) {
                Trigger trigger = new Trigger();
                triggers[i] = trigger;
                TriggerControl control = TriggerControl.create(trigger);
                registerControl(prefix + (i + 1), control);
                if (i < ports) {
                    registerPort(prefix + (i + 1), control.createPort());
                }
            }
        }
    }

    protected void buildOutputs(String prefix, int count) {
        if (count < 1) {
            outputs = new Output[0];
        } else {
            outputs = new Output[count];
            for (int i=0; i < count; i++) {
                ControlPort.Output out = new DefaultControlOutputPort(this);
                outputs[i] = new OutputImpl(out);
                registerPort(prefix + (i + 1), out);
            }
        }
    }

    protected CodeContext getCodeContext() {
        if (context == null) {
            context = new DelegateContext();
        }
        return context;
    }

    protected void setDelegate(CodeDelegate delegate) {
        uninstallDelegate();
        if (delegate == null) {
            this.delegate = null;
            return;
        }
        try {
            delegate.init(getCodeContext(), time);
            this.delegate = delegate;
        } catch (Exception ex) {
            Logger.getLogger(AbstractJavaComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    private void uninstallDelegate() {
        if (delegate != null) {
            delegate.dispose();
            delegate = null;
        }
    }

    @Override
    public void stateChanged(ExecutionContext source) {
        super.stateChanged(source);
        time = source.getTime();
    }

    public void tick(ExecutionContext source) {
        time = source.getTime();
        clock.process();
        if (delegate != null) {
            delegate.tick();
        }
    }


    private class DelegateContext extends CodeContext {

        @Override
        public Param getParam(int index) {
            return params[index];
        }

        @Override
        public int getParamCount() {
            return params.length;
        }

        @Override
        public Trigger getTrigger(int index) {
            return triggers[index];
        }

        @Override
        public int getTriggerCount() {
            return triggers.length;
        }

        @Override
        public Output getOutput(int index) {
            return outputs[index];
        }

        @Override
        public int getOutputCount() {
            return outputs.length;
        }

        @Override
        public long getTime() {
            return time;
        }

    }

    private class OutputImpl implements Output {

        private ControlPort.Output out;

        private OutputImpl(ControlPort.Output out) {
            this.out = out;
        }

        public void send(Argument arg) {
            out.send(time, arg);
        }

        public void send(double value) {
            out.send(time, value);
        }

    }


    private class Clock implements Param.Clock {

        private Param.ClockListener[] listeners;

        private Clock() {
            listeners = new Param.ClockListener[0];
        }

        public long getTime() {
            return time;
        }

        private void process() {
            for (Param.ClockListener l : listeners) {
                l.tick();
            }
        }

        public void connect(Param.ClockListener p) {
            listeners = ListenerUtils.add(listeners, p);
        }

        public void disconnect(Param.ClockListener p) {
            listeners = ListenerUtils.remove(listeners, p);
        }

    }

}
