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
import net.neilcsmith.praxis.compiler.MessageHandler;
import net.neilcsmith.praxis.core.ComponentType;
import net.neilcsmith.praxis.logging.LogBuilder;
import net.neilcsmith.praxis.logging.LogLevel;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public abstract class CodeFactory<D extends CodeDelegate> {

    private final ComponentType type;
    private final ClassBodyContext<D> cbc;
    private final String template;

    protected CodeFactory(
            ClassBodyContext<D> cbc,
            ComponentType type,
            String template) {

        this.cbc = cbc;
        this.type = type;
        this.template = template;
    }

    protected CodeFactory(
            ClassBodyContext<D> cbc,
            String type,
            String template) {
        this(cbc, ComponentType.create(type), template);
    }

    public final ComponentType getComponentType() {
        return type;
    }

    public final ClassBodyContext<D> getClassBodyContext() {
        return cbc;
    }

    public final String getSourceTemplate() {
        return template;
    }

    public CodeComponent<D> createComponent() throws Exception {
        CodeContext<D> ctxt = task().createDefaultCodeContext();
        CodeComponent<D> cmp = new CodeComponent<>();
        cmp.install(ctxt);
        return cmp;
    }

    public abstract Task<D> task();

   

    public static abstract class Task<D extends CodeDelegate> {

        private final CodeFactory<D> factory;
        
        private LogBuilder log;
        private Class<D> previous;

        public Task(CodeFactory<D> factory) {
            this.factory = factory;
        }

        public Task<D> attachLogging(LogBuilder log) {
            this.log = log;
            return this;
        }
        
        public Task<D> attachPrevious(Class<D> previous) {
            this.previous = previous;
            return this;
        }
        
        public CodeContext<D> createCodeContext(String source) throws Exception {
            return createCodeContext(createDelegate(source));
        }
        
        public CodeContext<D> createDefaultCodeContext() throws Exception {
            return createCodeContext(createDefaultDelegate());
        }

        protected LogBuilder getLog() {
            return log;
        }
        
        protected Class<D> getPrevious() {
            return previous;
        }
        
        protected CodeFactory<D> getFactory() {
            return factory;
        }
        
        protected D createDelegate(String source) throws Exception {
            Class<D> cls = compile(source);
            return cls.newInstance();
        }

        protected D createDefaultDelegate() throws Exception {
            Class<D> cls = compile(factory.template);
            //@TODO cache result
            return cls.newInstance();
        }

        protected Class<D> compile(String source) throws Exception {
            MessageHandler handler = null;
            if (log != null) {
                handler = new LogMessageHandler(log);
            }
            return ClassBodyCompiler.getDefault().compile(factory.cbc, handler, source);
        }
        
         protected abstract CodeContext<D> createCodeContext(D delegate);

    }

    private static class LogMessageHandler implements MessageHandler {

        private final LogBuilder log;

        private LogMessageHandler(LogBuilder log) {
            this.log = log;
        }

        @Override
        public void handleError(String msg) {
            log.log(LogLevel.ERROR, msg);
        }

        @Override
        public void handleWarning(String msg) {
            log.log(LogLevel.WARNING, msg);
        }

    }

}
