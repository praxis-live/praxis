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
import net.neilcsmith.praxis.core.Task;
import net.neilcsmith.praxis.core.types.PReference;
import net.neilcsmith.praxis.core.types.PString;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.impl.ArgumentProperty;
import net.neilcsmith.praxis.impl.FloatProperty;
import net.neilcsmith.praxis.impl.ResourceLoader;
import net.neilcsmith.praxis.impl.TriggerControl;
import org.codehaus.janino.ClassBodyEvaluator;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public abstract class AbstractJaninoComponent extends AbstractComponent {

    private static final String[] imports = {
        "net.neilcsmith.ripl.*",
        "net.neilcsmith.ripl.ops.*",
        "java.awt.*",
        "java.awt.image.*"
    };
    private double[] floats;
    private Argument[] params;
    private boolean[] triggers;
    private JaninoVideoDelegate installedDelegate;

    public AbstractJaninoComponent() {
        setupFloats(8);
        setupParams(4);
        setupTriggers(4);
        setupCodeControl();
    }

    private void setupFloats(int count) {
        floats = new double[count];
        for (int i = 0; i < count; i++) {
            registerControl("f" + (i + 1), FloatProperty.create(this, new FloatBinding(i), 0));
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
            registerControl("t" + (i + 1), TriggerControl.create(this, new TriggerBinding(i)));
        }
    }

    private void setupCodeControl() {
        registerControl("code", new DelegateCompiler());
    }

    void preDraw() {
    }

    void postDraw() {
        resetTriggers();
    }

    private void resetTriggers() {
        Arrays.fill(triggers, false);
    }

    private void install(JaninoVideoDelegate delegate) {
        if (installedDelegate != null) {
            installedDelegate.dispose();
        }
        if (delegate != null) {
            delegate.install(floats, params, triggers);
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
