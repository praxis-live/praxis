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

import net.neilcsmith.praxis.compiler.ClassBodyCompiler;
import net.neilcsmith.praxis.compiler.ClassBodyContext;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.Control;
import net.neilcsmith.praxis.core.interfaces.TaskService;
import net.neilcsmith.praxis.core.types.PReference;
import net.neilcsmith.praxis.core.types.PString;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public abstract class CodeProperty<T extends CodeDelegate>
        extends AbstractAsyncProperty<CodeProperty.Result> {

    private CodeContext<T> context;

    public CodeProperty() {
        super(PString.EMPTY, Result.class, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void attach(CodeContext<?> context) {
        super.attach(context);
        this.context = (CodeContext<T>) context;
    }
    

    @Override
    protected final Task<T> createTask(CallArguments keys) throws Exception {
        String code = keys.get(0).toString();
        return createTask(code);
    }

    protected abstract Task<T> createTask(String code);

    @Override
    @SuppressWarnings("unchecked")
    protected void valueChanged(long time) {
        Result<T> r = getValue();
        if (r != null) {
            context.getComponent().install(r.getCodeContext());
        }
    }

    public static abstract class Task<T extends CodeDelegate> implements TaskService.Task {

        private final ClassBodyContext<T> cbc;
        private final String code;

        protected Task(ClassBodyContext<T> cbc, String code) {
            if (cbc == null || code == null) {
                throw new NullPointerException();
            }
            this.cbc = cbc;
            this.code = code;
        }

        @Override
        public final Argument execute() throws Exception {
            Class<T> cls = compile(code);
            T delegate = createDelegate(cls);
            CodeContext<T> ctxt = createCodeContext(delegate);
            return PReference.wrap(new Result<T>(ctxt));
        }

        protected Class<T> compile(String code) throws Exception {
            return ClassBodyCompiler.getDefault().compile(
                    cbc, code);
        }

        protected T createDelegate(Class<T> cls)
                throws Exception {
            return cls.newInstance();
        }

        protected abstract CodeContext<T> createCodeContext(T delegate);

    }

    public static class Result<T extends CodeDelegate> {

        private final CodeContext<T> context;

        public Result(CodeContext<T> context) {
            if (context == null) {
                throw new NullPointerException();
            }
            this.context = context;
        }

        public CodeContext<T> getCodeContext() {
            return context;
        }

    }

    public static abstract class Descriptor<P extends CodeProperty<? extends CodeDelegate>>
            extends ControlDescriptor {

        private final Class<P> controlCls;
        private P control;

        public Descriptor(String id, int index, Class<P> cls) {
            super(id, Category.Property, index);
            this.controlCls = cls;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void attach(CodeContext<?> context, Control previous) {
            if (controlCls.isInstance(previous)) {
                control = (P) previous;
            } else {
                control = createControl();
            }
            control.attach(context);
        }

        @Override
        public Control getControl() {
            return control;
        }

        protected abstract P createControl();

    }

}
