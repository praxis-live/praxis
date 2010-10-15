/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 - Neil C Smith. All rights reserved.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details.
 *
 * You should have received a copy of the GNU General Public License version 2
 * along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 */
package net.neilcsmith.praxis.video.janino;

import java.util.Arrays;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.Root;
import net.neilcsmith.praxis.core.Root.State;
import net.neilcsmith.praxis.core.interfaces.Task;
import net.neilcsmith.praxis.core.types.PReference;
import net.neilcsmith.praxis.core.types.PString;
import net.neilcsmith.praxis.impl.AbstractControlFrameComponent;
import net.neilcsmith.praxis.impl.AbstractRoot;
import net.neilcsmith.praxis.impl.ArgumentProperty;
import net.neilcsmith.praxis.impl.DefaultControlOutputPort;
import net.neilcsmith.praxis.impl.FloatProperty;
import net.neilcsmith.praxis.impl.ResourceLoader;
import net.neilcsmith.praxis.impl.TriggerControl;
import org.codehaus.janino.ClassBodyEvaluator;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public abstract class AbstractJaninoComponent extends AbstractControlFrameComponent {

    private static final String[] imports = {
        "net.neilcsmith.ripl.*",
        "net.neilcsmith.ripl.ops.*",
        "java.awt.*",
        "java.awt.image.*"
    };
    private double[] floats;
    private Argument[] params;
    private boolean[] triggers;
    private Argument[] outs;
    private DefaultControlOutputPort[] outPorts;
    private JaninoVideoDelegate installedDelegate;

    public AbstractJaninoComponent() {
        setupFloats(8);
        setupParams(4);
        setupTriggers(4);
        setupOuts(4);
        setupCodeControl();
    }

    private void setupFloats(int count) {
        floats = new double[count];
        for (int i = 0; i < count; i++) {
            FloatProperty f = FloatProperty.create(this, new FloatBinding(i), 0);
            String id = "f" + (i + 1);
            registerControl(id, f);
            registerPort(id, f.createPort());
        }
    }

    private void setupParams(int count) {
        params = new Argument[count];
        Arrays.fill(params, PString.EMPTY);
        for (int i = 0; i < count; i++) {
            registerControl("p" + (i + 1), ArgumentProperty.create(this, new ParamBinding(i), PString.EMPTY));
        }
    }

    private void setupTriggers(int count) {
        triggers = new boolean[count];
        for (int i = 0; i < count; i++) {
            TriggerControl t = TriggerControl.create(this, new TriggerBinding(i));
            String id = "t" + (i + 1);
            registerControl(id, t);
            registerPort(id, t.createPort());
        }
    }
    
    private void setupOuts(int count) {
        outs = new Argument[count];
        outPorts = new DefaultControlOutputPort[count];
        for (int i = 0; i < count; i++) {
            DefaultControlOutputPort port = new DefaultControlOutputPort(this);
            outPorts[i] = port;
            String id = "send-" + (i + 1);
            registerPort(id, port);
        }
    }

    private void setupCodeControl() {
        registerControl("code", new DelegateCompiler());
    }

    @Override
    public void rootStateChanged(AbstractRoot source, State state) {
        Arrays.fill(outs, null);
    }

    public void nextControlFrame(AbstractRoot source) {
        for (int i=0; i < outs.length; i++) {
            Argument arg = outs[i];
            if (arg != null) {
                outPorts[i].send(source.getTime(), arg);
            }
            outs[i] = null;
        }
    }

    private void install(JaninoVideoDelegate delegate) {
        if (installedDelegate != null) {
            installedDelegate.dispose();
        }
        if (delegate != null) {
            delegate.install(floats, params, triggers, outs);
        }
        installDelegate(delegate);
        installedDelegate = delegate;
    }

    abstract void installDelegate(JaninoVideoDelegate delegate);

    private class DelegateCompiler extends ResourceLoader<JaninoVideoDelegate> {

        private DelegateCompiler() {
            super(AbstractJaninoComponent.this, JaninoVideoDelegate.class);
        }

        @Override
        protected Task getLoadTask(Argument code) {
            return new CompilerTask(code.toString());
        }

        @Override
        protected void resourceLoaded() {
            install(getResource());
        }

        @Override
        protected void resourceError() {
            Logger.getLogger(getClass().getName()).warning("Error loading class");
        }
    }

    private class CompilerTask implements Task {

        private final String code;

        private CompilerTask(String code) {
            this.code = code;
        }

        public Argument execute() throws Exception {
            ClassBodyEvaluator compiler = new ClassBodyEvaluator();
            compiler.setExtendedType(JaninoVideoDelegate.class);
            compiler.setDefaultImports(imports.clone());
            compiler.cook(code);
            JaninoVideoDelegate delegate = (JaninoVideoDelegate) compiler.getClazz().newInstance();
            return PReference.wrap(delegate);
        }
    }

    private class FloatBinding implements FloatProperty.Binding {

        private int idx;

        private FloatBinding(int idx) {
            this.idx = idx;
        }

        public void setBoundValue(long time, double value) {
            floats[idx] = value;
        }

        public double getBoundValue() {
            return floats[idx];
        }
    }

    private class ParamBinding implements ArgumentProperty.Binding {

        private int idx;

        private ParamBinding(int idx) {
            this.idx = idx;
        }

        public void setBoundValue(long time, Argument value) {
            params[idx] = value;
        }

        public Argument getBoundValue() {
            return params[idx];
        }
    }

    private class TriggerBinding implements TriggerControl.Binding {

        private int idx;

        private TriggerBinding(int idx) {
            this.idx = idx;
        }

        public void trigger(long time) {
            triggers[idx] = true;
        }
    }
}
