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
import net.neilcsmith.praxis.code.CodeCompilerService;
import net.neilcsmith.praxis.code.CodeComponent;
import net.neilcsmith.praxis.code.CodeComponentFactoryService;
import net.neilcsmith.praxis.code.CodeContext;
import net.neilcsmith.praxis.code.CodeContextFactoryService;
import net.neilcsmith.praxis.code.CodeDelegate;
import net.neilcsmith.praxis.code.CodeFactory;
import net.neilcsmith.praxis.compiler.ClassBodyContext;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.Call;
import net.neilcsmith.praxis.core.ComponentFactory;
import net.neilcsmith.praxis.core.ComponentType;
import net.neilcsmith.praxis.core.ControlAddress;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.core.types.PArray;
import net.neilcsmith.praxis.core.types.PMap;
import net.neilcsmith.praxis.core.types.PReference;
import net.neilcsmith.praxis.impl.AbstractAsyncControl;
import net.neilcsmith.praxis.impl.AbstractRoot;
import net.neilcsmith.praxis.logging.LogBuilder;
import net.neilcsmith.praxis.logging.LogLevel;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class DefaultCodeFactoryService extends AbstractRoot {

    private final ComponentRegistry registry;
    
    public DefaultCodeFactoryService() {
        super(EnumSet.noneOf(Caps.class));
        registry = ComponentRegistry.getInstance();
        registerControl(CodeComponentFactoryService.NEW_INSTANCE, new NewInstanceControl());
        registerControl(CodeContextFactoryService.NEW_CONTEXT, new NewContextControl());
        registerInterface(CodeComponentFactoryService.class);
        registerInterface(CodeContextFactoryService.class);
    }

    
    private ControlAddress findCompilerService() throws Exception {
        return ControlAddress.create(findService(CodeCompilerService.class),
                CodeCompilerService.COMPILE);
    }
    
    
    private PMap createCompilerTask(ClassBodyContext<?> cbc, LogLevel logLevel, String source) {
        return PMap.create(
                CodeCompilerService.KEY_CLASS_BODY_CONTEXT,
                cbc.getClass().getName(), 
                CodeCompilerService.KEY_LOG_LEVEL,
                logLevel.asPString(),
                CodeCompilerService.KEY_CODE,
                source);
    }
    
    private CodeDelegate extractCodeDelegate(Argument response) throws Exception {
        PMap data = PMap.coerce(response);
        PMap classes = PMap.coerce(data.get(CodeCompilerService.KEY_CLASSES));
        ClassLoader classLoader = new PMapClassLoader(classes, Thread.currentThread().getContextClassLoader());
        return (CodeDelegate) classLoader.loadClass("$").newInstance();
    }
    
    private void extractCompilerLog(Argument response, LogBuilder logBuilder) throws Exception {
        PMap data = PMap.coerce(response);
        PArray log = PArray.coerce(data.get(CodeCompilerService.KEY_LOG));
        for (int i=0; i<log.getSize(); i+=2) {
            logBuilder.log(LogLevel.valueOf(log.get(i).toString()), log.get(i+1).toString());
        }
    }
    

    private class NewInstanceControl extends AbstractAsyncControl {

        @Override
        protected Call processInvoke(Call call) throws Exception {
            CodeFactory<?> codeFactory = findCodeFactory();
            return Call.createCall(
                    findCompilerService(),
                    getAddress(),
                    call.getTimecode(),
                    createCompilerTask(codeFactory.getClassBodyContext(),
                            LogLevel.ERROR,
                            codeFactory.getSourceTemplate()));
        }

        @Override
        protected Call processResponse(Call call) throws Exception {
            CodeFactory<CodeDelegate> codeFactory = findCodeFactory();
            CodeDelegate delegate = extractCodeDelegate(call.getArgs().get(0));
            CodeComponent<CodeDelegate> cmp = codeFactory.task().createComponent(delegate);
            return Call.createReturnCall(getActiveCall(), PReference.wrap(cmp));
        }
        
        private CodeFactory<CodeDelegate> findCodeFactory() throws Exception {
            ComponentType type = ComponentType.coerce(getActiveCall().getArgs().get(0));
            ComponentFactory cmpFactory = registry.getComponentFactory(type);
            return cmpFactory.getMetaData(type).getLookup().get(CodeFactory.class);
        }

        @Override
        public ControlInfo getInfo() {
            return CodeComponentFactoryService.NEW_INSTANCE_INFO;
        }

    }

    private class NewContextControl extends AbstractAsyncControl {

        @Override
        @SuppressWarnings("unchecked")
        protected Call processInvoke(Call call) throws Exception {
            CodeContextFactoryService.Task<CodeDelegate> task = findTask();
            CodeFactory<CodeDelegate> factory = task.getFactory();
            String src = task.getCode();
            if (src.trim().isEmpty()) {
                src = factory.getSourceTemplate();
            }
            return Call.createCall(
                    findCompilerService(),
                    getAddress(),
                    call.getTimecode(),
                    createCompilerTask(factory.getClassBodyContext(),
                            LogLevel.ERROR,
                            src));
        }

        @Override
        protected Call processResponse(Call call) throws Exception {
            CodeContextFactoryService.Task<CodeDelegate> task = findTask();
            CodeDelegate delegate = extractCodeDelegate(call.getArgs().get(0));
            LogBuilder log = new LogBuilder(task.getLogLevel());
            extractCompilerLog(call.getArgs().get(0), log);
            CodeContext<CodeDelegate> context = task.getFactory().task()
                    .attachPrevious(task.getPrevious())
                    .attachLogging(log)
                    .createContext(delegate);
            CodeContextFactoryService.Result<CodeDelegate> result =
                    new CodeContextFactoryService.Result<>(context, log);
            return Call.createReturnCall(getActiveCall(), PReference.wrap(result));
        }
        
        private CodeContextFactoryService.Task<CodeDelegate> findTask() throws Exception {
            return (CodeContextFactoryService.Task<CodeDelegate>)
                    PReference.coerce(getActiveCall().getArgs().get(0)).getReference();
        }

        @Override
        public ControlInfo getInfo() {
            return CodeContextFactoryService.NEW_CONTEXT_INFO;
        }

    }

}
