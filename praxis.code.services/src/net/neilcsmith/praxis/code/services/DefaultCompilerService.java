/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Neil C Smith.
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
package net.neilcsmith.praxis.code.services;

import java.util.EnumSet;
import java.util.Map;
import javax.tools.JavaCompiler;
import net.neilcsmith.praxis.code.CodeCompilerService;
import net.neilcsmith.praxis.compiler.ClassBodyCompiler;
import net.neilcsmith.praxis.compiler.ClassBodyContext;
import net.neilcsmith.praxis.compiler.JavaCompilerProvider;
import net.neilcsmith.praxis.compiler.MessageHandler;
import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.Lookup;
import net.neilcsmith.praxis.core.types.PArray;
import net.neilcsmith.praxis.core.types.PBytes;
import net.neilcsmith.praxis.core.types.PMap;
import net.neilcsmith.praxis.impl.AbstractRoot;
import net.neilcsmith.praxis.impl.SimpleControl;
import net.neilcsmith.praxis.logging.LogBuilder;
import net.neilcsmith.praxis.logging.LogLevel;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class DefaultCompilerService extends AbstractRoot {

    private final JavaCompiler compiler;

    public DefaultCompilerService() {
        super(EnumSet.noneOf(Caps.class));
        registerControl(CodeCompilerService.COMPILE, new CompileControl());
        registerInterface(CodeCompilerService.class);
        JavaCompilerProvider compilerProvider = Lookup.SYSTEM.get(JavaCompilerProvider.class);
        if (compilerProvider != null) {
            compiler = compilerProvider.getJavaCompiler();
        } else {
            throw new RuntimeException("No compiler found");
        }
    }

    private class CompileControl extends SimpleControl {

        private CompileControl() {
            super(CodeCompilerService.COMPILE_INFO);
        }

        @Override
        protected CallArguments process(long time, CallArguments args, boolean quiet) throws Exception {
            PMap map = PMap.coerce(args.get(0));
            String code = map.getString(CodeCompilerService.KEY_CODE, "");
            ClassBodyContext<?> cbc = getClassBodyContext(map);
            LogBuilder log = new LogBuilder(LogLevel.WARNING);
            Map<String, byte[]> classFiles
                    = ClassBodyCompiler.create(cbc)
                    .setCompiler(compiler)
                    .addMessageHandler(new LogMessageHandler(log))
                    .compile(code);
            PMap classes = convertClasses(classFiles);
            PMap response = PMap.create(CodeCompilerService.KEY_CLASSES, classes,
                    CodeCompilerService.KEY_LOG, PArray.valueOf(log.toCallArguments()));
            return CallArguments.create(response);
        }

        private ClassBodyContext<?> getClassBodyContext(PMap map) throws ClassNotFoundException,
                InstantiationException, IllegalAccessException {
            String cbcClass = map.getString(CodeCompilerService.KEY_CLASS_BODY_CONTEXT, null);
            return (ClassBodyContext<?>) Class.forName(cbcClass, true, Thread.currentThread().getContextClassLoader()).newInstance();
        }

        private LogLevel getLogLevel(PMap map) {
            String level = map.getString(CodeCompilerService.KEY_LOG_LEVEL, null);
            if (level != null) {
                return LogLevel.valueOf(level);
            } else {
                return LogLevel.ERROR;
            }
        }

        private PMap convertClasses(Map<String, byte[]> classes) {
            PMap.Builder bld = PMap.builder(classes.size());
            classes.entrySet().stream().forEach((type) -> {
                bld.put(type.getKey(), PBytes.valueOf(type.getValue()));
            });
            return bld.build();
        }

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
