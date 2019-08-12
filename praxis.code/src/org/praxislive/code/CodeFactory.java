/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2018 Neil C Smith.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * version 3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License version 3
 * along with this work; if not, see http://www.gnu.org/licenses/
 *
 *
 * Please visit https://www.praxislive.org if you need additional information or
 * have any questions.
 */
package org.praxislive.code;

import java.util.Optional;
import org.praxislive.core.ComponentType;
import org.praxislive.logging.LogBuilder;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public abstract class CodeFactory<D extends CodeDelegate> {

    private final ComponentType type;
    private final ClassBodyContext<D> cbc;
    private final String template;
    private final Class<? extends D> defaultDelegateClass;

    protected CodeFactory(
            ClassBodyContext<D> cbc,
            ComponentType type,
            Class<? extends D> defaultCls,
            String template) {
        this.cbc = cbc;
        this.type = type;
        this.defaultDelegateClass = defaultCls;
        this.template = template;
    }
    
    protected CodeFactory(
            ClassBodyContext<D> cbc,
            ComponentType type,
            String template) {
        this(cbc, type, null, template);
    }

    @Deprecated
    protected CodeFactory(
            ClassBodyContext<D> cbc,
            String type,
            String template) {
        this(cbc, ComponentType.of(type), template);
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
    
    public final Optional<Class<? extends D>> getDefaultDelegateClass() {
        return Optional.ofNullable(defaultDelegateClass);
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

        public CodeComponent<D> createComponent(D delegate) {
            CodeComponent<D> cmp = new CodeComponent<>();
            cmp.install(createContext(delegate));
            return cmp;
        }
        
        public CodeContext<D> createContext(D delegate) {
            return createCodeContext(delegate);
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

        protected abstract CodeContext<D> createCodeContext(D delegate);

    }

}
