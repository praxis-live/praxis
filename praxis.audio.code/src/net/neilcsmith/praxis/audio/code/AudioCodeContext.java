/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Neil C Smith.
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
package net.neilcsmith.praxis.audio.code;

import net.neilcsmith.praxis.code.CodeComponent;
import net.neilcsmith.praxis.code.CodeContext;
import net.neilcsmith.praxis.core.ExecutionContext;
import net.neilcsmith.praxis.logging.LogLevel;
import org.jaudiolibs.pipes.Pipe;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class AudioCodeContext<D extends AudioCodeDelegate> extends CodeContext<D> {

    private final Driver driver;
    private final boolean hasUpdateMethod;

    private final UGenDescriptor[] ugens;
    private final AudioInPort.Descriptor[] ins;
    private final AudioOutPort.Descriptor[] outs;

    private ExecutionContext execCtxt;

    public AudioCodeContext(AudioCodeConnector<D> connector) {
        super(connector);
        driver = new Driver();
        hasUpdateMethod = true;
        ugens = connector.extractUGens();
        ins = connector.extractIns();
        outs = connector.extractOuts();
    }

    @Override
    protected void configure(CodeComponent<D> cmp, CodeContext<D> oldCtxt) {
        super.configure(cmp, oldCtxt);
        // audio ins and outs attached in super call because they're ports
        for (UGenDescriptor ugd : ugens) {
            ugd.attach(this, oldCtxt);
        }
    }

    @Override
    protected void hierarchyChanged() {
        super.hierarchyChanged();
        ExecutionContext ctxt = getLookup().get(ExecutionContext.class);
        if (execCtxt != ctxt) {
            if (execCtxt != null) {
                execCtxt.removeStateListener(driver);
                execCtxt.removeClockListener(driver);
            }
            execCtxt = ctxt;
            if (ctxt != null) {
                ctxt.addStateListener(driver);
                if (hasUpdateMethod) {
                    ctxt.addClockListener(driver);
                }
                if (ctxt.getState() == ExecutionContext.State.ACTIVE) {
                    update(ctxt.getTime());
                    setupDelegate();
                }
            }
        }
    }

    private void setupDelegate() {
        setupPorts();
        setupUGens();
        try {
            getDelegate().setup();
        } catch (Exception e) {
            getLog().log(LogLevel.ERROR, e, "Exception thrown during setup()");
        }
    }

    private void updateDelegate() {
        try {
            getDelegate().update();
        } catch (Exception e) {
            getLog().log(LogLevel.ERROR, e, "Exception thrown during update()");
        }
    }
    
    private void setupUGens() {
        for (UGenDescriptor ugd : ugens) {
            Pipe ug = ugd.getUGen();
            Utils.disconnect(ug);
            if (ug instanceof Resettable) {
                ((Resettable)ug).reset();
            }
        }
    }
    
    private void setupPorts() {
        for (AudioInPort.Descriptor aipd : ins) {
            Utils.disconnectSinks(aipd.getPort().getPipe());
        }
        for (AudioOutPort.Descriptor aopd : outs) {
            AudioOutPort.AudioOutPipe pipe = aopd.getPort().getPipe();
            Utils.disconnectSources(pipe);
            pipe.triggerSwitch();
        }
    }
    
    private void resetPorts() {
        for (AudioOutPort.Descriptor aopd : outs) {
            AudioOutPort.AudioOutPipe pipe = aopd.getPort().getPipe();
            pipe.resetSwitch();
        }
    }

    private class Driver implements ExecutionContext.StateListener,
            ExecutionContext.ClockListener {

        @Override
        public void stateChanged(ExecutionContext source) {
            if (source.getState() == ExecutionContext.State.ACTIVE) {
                update(source.getTime());
                setupDelegate();
            } else {
                update(source.getTime());
                resetPorts();
            }
            flush();
        }

        @Override
        public void tick(ExecutionContext source) {
            update(source.getTime());
            updateDelegate();
            flush();
        }

    }

}
