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

import java.util.stream.Stream;
import org.praxislive.core.ArgumentInfo;
import org.praxislive.core.ControlInfo;
import org.praxislive.core.services.Service;
import org.praxislive.core.types.PMap;
import org.praxislive.core.types.PReference;
import org.praxislive.logging.LogBuilder;
import org.praxislive.logging.LogLevel;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class CodeContextFactoryService implements Service {

    public final static String NEW_CONTEXT = "new-context";
    public final static ControlInfo NEW_CONTEXT_INFO =
            ControlInfo.createFunctionInfo(
            new ArgumentInfo[]{PReference.info(Task.class)},
            new ArgumentInfo[]{PReference.info(Result.class)},
            PMap.EMPTY);

    @Override
    public Stream<String> controls() {
        return Stream.of(NEW_CONTEXT);
    }
    
    @Override
    public ControlInfo getControlInfo(String control) {
        if (NEW_CONTEXT.equals(control)) {
            return NEW_CONTEXT_INFO;
        }
        throw new IllegalArgumentException();
    }
    
    public final static class Task<D extends CodeDelegate> {
        private final CodeFactory<D> factory;
        private final String code;
        private final LogLevel logLevel;
        private final Class<D> previous;
        
        public Task(CodeFactory<D> factory,
                String code,
                LogLevel logLevel,
                Class<D> previous) {
            this.factory = factory;
            this.code = code;
            this.logLevel = logLevel;
            this.previous = previous;
        }

        public CodeFactory<D> getFactory() {
            return factory;
        }

        public String getCode() {
            return code;
        }

        public LogLevel getLogLevel() {
            return logLevel;
        }

        public Class<D> getPrevious() {
            return previous;
        }
        
    }
    
    public final static class Result<D extends CodeDelegate> {

        private final CodeContext<D> context;
        private final LogBuilder log;

        public Result(CodeContext<D> context, LogBuilder log) {
            this.context = context;
            this.log = log;
        }

        public CodeContext<D> getContext() {
            return context;
        }

        public LogBuilder getLog() {
            return log;
        }
        
    }
    
}