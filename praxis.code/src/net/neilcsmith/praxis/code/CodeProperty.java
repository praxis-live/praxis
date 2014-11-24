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

import net.neilcsmith.praxis.compiler.ClassBodyContext;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.Control;
import net.neilcsmith.praxis.core.info.ArgumentInfo;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.core.interfaces.TaskService;
import net.neilcsmith.praxis.core.types.PMap;
import net.neilcsmith.praxis.core.types.PReference;
import net.neilcsmith.praxis.core.types.PString;
import net.neilcsmith.praxis.logging.LogBuilder;
import net.neilcsmith.praxis.logging.LogLevel;


class CodeProperty<D extends CodeDelegate>
        extends AbstractAsyncProperty<CodeProperty.Result> {
    
    public final static String MIME_TYPE = "text/x-praxis-java";

    private final CodeFactory<D> factory;
    private final ControlInfo info;
    private CodeContext<D> context;

    private CodeProperty(CodeFactory<D> factory, ControlInfo info) {
        super(PString.EMPTY, Result.class, null);
        this.factory = factory;
        this.info = info;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void attach(CodeContext<?> context) {
        super.attach(context);
        this.context = (CodeContext<D>) context;
    }

    @Override
    protected final Task<D> createTask(CallArguments keys) throws Exception {
        String code = keys.get(0).toString();
        return new Task<>(factory, code, context.getLogLevel());
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void valueChanged(long time) {
        Result<D> r = getValue();
        if (r != null) {
            context.getComponent().install(r.context);
            context.log(r.log);
        }
    }

    @Override
    public ControlInfo getInfo() {
        return info;
    }

    static class Task<T extends CodeDelegate> implements TaskService.Task {

        private final CodeFactory<T> factory;
        private final String code;
        private final LogLevel logLevel;

        private Task(CodeFactory<T> factory, String code, LogLevel logLevel) {
            this.factory = factory;
            this.code = code;
            this.logLevel = logLevel;
        }

        @Override
        public final Argument execute() throws Exception {
            String src = code.trim();
            CodeContext<T> ctxt;
            LogBuilder log = new LogBuilder(logLevel);
            if (src.isEmpty()) {
                ctxt = factory.task().createDefaultCodeContext();
            } else {
                ctxt = factory.task()
                        .attachLogging(log)
                        .createCodeContext(src);
            }
            return PReference.wrap(new Result<T>(ctxt, log));
        }

    }

    static class Result<T extends CodeDelegate> {

        private final CodeContext<T> context;
        private final LogBuilder log;

        private Result(CodeContext<T> context, LogBuilder log) {
            this.context = context;
            this.log = log;
        }

    }

    static class Descriptor<D extends CodeDelegate>
            extends ControlDescriptor {

        private final CodeFactory<D> factory;
        private final ControlInfo info;
        private CodeProperty<D> control;

        public Descriptor(CodeFactory<D> factory, int index) {
            super("code", Category.Property, index);
            this.factory = factory;
            this.info = createInfo(factory);
        }

        private ControlInfo createInfo(CodeFactory<D> factory) {
            return ControlInfo.createPropertyInfo(
                    new ArgumentInfo[]{
                        ArgumentInfo.create(PString.class,
                                PMap.create(
                                        PString.KEY_MIME_TYPE, MIME_TYPE,
                                        ArgumentInfo.KEY_TEMPLATE, factory.getSourceTemplate(),
                                        ClassBodyContext.KEY, factory.getClassBodyContext().getClass().getName()
                                ))
                    },
                    new Argument[]{PString.EMPTY},
                    PMap.EMPTY);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void attach(CodeContext<?> context, Control previous) {
            if (previous instanceof CodeProperty
                    && ((CodeProperty<?>) previous).factory == factory) {
                control = (CodeProperty) previous;
            } else {
                control = new CodeProperty(factory, info);
            }
            control.attach(context);
        }

        @Override
        public Control getControl() {
            return control;
        }

        @Override
        public ControlInfo getInfo() {
            return info;
        }

    }

}
