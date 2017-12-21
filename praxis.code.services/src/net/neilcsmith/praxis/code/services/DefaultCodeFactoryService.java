/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2017 Neil C Smith.
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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
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
import net.neilcsmith.praxis.core.Lookup;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.core.types.PArray;
import net.neilcsmith.praxis.core.types.PMap;
import net.neilcsmith.praxis.core.types.PReference;
import net.neilcsmith.praxis.core.types.PResource;
import net.neilcsmith.praxis.impl.AbstractAsyncControl;
import net.neilcsmith.praxis.impl.AbstractRoot;
import net.neilcsmith.praxis.logging.LogBuilder;
import net.neilcsmith.praxis.logging.LogLevel;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class DefaultCodeFactoryService extends AbstractRoot {

    private final static ConcurrentMap<ClassCacheKey, Class<? extends CodeDelegate>> CODE_CACHE
            = new ConcurrentHashMap<>();

    private final ComponentRegistry registry;
    private final Set<PResource> libs;
    
    private LibraryClassloader libClassloader;

    public DefaultCodeFactoryService() {
        super(EnumSet.noneOf(Caps.class));
        registry = ComponentRegistry.getInstance();
        libs = new LinkedHashSet<>();
        registerControl(CodeComponentFactoryService.NEW_INSTANCE, new NewInstanceControl());
        registerControl(CodeContextFactoryService.NEW_CONTEXT, new NewContextControl());
        registerInterface(CodeComponentFactoryService.class);
        registerInterface(CodeContextFactoryService.class);
    }

    @Override
    protected void activating() {
        libClassloader = new LibraryClassloader(Thread.currentThread().getContextClassLoader());
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

    private Class<? extends CodeDelegate> extractCodeDelegateClass(Argument response) throws Exception {
        PMap data = PMap.coerce(response);
        PMap classes = PMap.coerce(data.get(CodeCompilerService.KEY_CLASSES));
        PArray.from(data.get(DefaultCompilerService.EXT_CLASSPATH)).ifPresent(this::processExtClasspath);
        ClassLoader classLoader = new PMapClassLoader(classes, libClassloader);
        return (Class<? extends CodeDelegate>) classLoader.loadClass("$");
    }
    
    private void processExtClasspath(PArray extCP) {
        if (extCP.isEmpty()) {
            return;
        }
        List<PResource> extLibs = extCP.stream()
                .map(v -> PResource.from(v).orElseThrow(() -> new IllegalArgumentException()))
                .collect(Collectors.toCollection(ArrayList::new));
        
        extLibs.removeAll(libs);
        Lookup lkp = getLookup();
        extLibs.forEach(res -> {
            URI lib = res.resolve(lkp).stream()
                    .filter(uri -> !"file".equals(uri.getScheme()) || new File(uri).exists())
                    .findFirst().orElseThrow(() -> new IllegalArgumentException("Can't find library : " + res ));
            try {
                libClassloader.addURL(lib.toURL());
            } catch (MalformedURLException ex) {
                throw new IllegalArgumentException(ex);
            }
            libs.add(res);
        });
    }

    private void extractCompilerLog(Argument response, LogBuilder logBuilder) throws Exception {
        PMap data = PMap.coerce(response);
        PArray log = PArray.coerce(data.get(CodeCompilerService.KEY_LOG));
        for (int i = 0; i < log.getSize(); i += 2) {
            logBuilder.log(LogLevel.valueOf(log.get(i).toString()), log.get(i + 1).toString());
        }
    }

    private class NewInstanceControl extends AbstractAsyncControl {

        @Override
        protected Call processInvoke(Call call) throws Exception {
            CodeFactory<CodeDelegate> codeFactory = findCodeFactory();
            ClassBodyContext<?> cbc = codeFactory.getClassBodyContext();
            String src = codeFactory.getSourceTemplate();
            Class<? extends CodeDelegate> cls = CODE_CACHE.get(new ClassCacheKey(cbc, src));
            if (cls != null) {
                return Call.createReturnCall(call,
                        PReference.wrap(createComponent(codeFactory, cls)));
            } else {
                return Call.createCall(
                        findCompilerService(),
                        getAddress(),
                        call.getTimecode(),
                        createCompilerTask(cbc, LogLevel.ERROR, src));
            }

        }

        @Override
        protected Call processResponse(Call call) throws Exception {
            CodeFactory<CodeDelegate> codeFactory = findCodeFactory();
            Class<? extends CodeDelegate> cls = extractCodeDelegateClass(call.getArgs().get(0));
            CodeDelegate delegate = cls.newInstance();
            CodeComponent<CodeDelegate> cmp = codeFactory.task().createComponent(delegate);
            CODE_CACHE.putIfAbsent(new ClassCacheKey(codeFactory.getClassBodyContext(), codeFactory.getSourceTemplate()), cls);
            return Call.createReturnCall(getActiveCall(), PReference.wrap(cmp));
        }

        private CodeFactory<CodeDelegate> findCodeFactory() throws Exception {
            ComponentType type = ComponentType.coerce(getActiveCall().getArgs().get(0));
            ComponentFactory cmpFactory = registry.getComponentFactory(type);
            return cmpFactory.getMetaData(type).getLookup().get(CodeFactory.class);
        }

        private CodeComponent<CodeDelegate> createComponent(
                CodeFactory<CodeDelegate> codeFactory,
                Class<? extends CodeDelegate> delegateClass) throws Exception {
            return codeFactory.task().createComponent(delegateClass.newInstance());
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
            ClassBodyContext<CodeDelegate> cbc = factory.getClassBodyContext();
            String src = task.getCode();
            Class<? extends CodeDelegate> cls;
            if (src.trim().isEmpty()) {
                src = factory.getSourceTemplate();
                cls = CODE_CACHE.get(new ClassCacheKey(cbc, src));
            } else {
                // @TODO weak code cache for user code
                cls = null;
            }
            if (cls != null) {
                LogBuilder log = new LogBuilder(task.getLogLevel());
                CodeDelegate delegate = cls.newInstance();
                return Call.createReturnCall(call,
                        PReference.wrap(createContext(task, log, delegate)));
            } else {
                return Call.createCall(
                        findCompilerService(),
                        getAddress(),
                        call.getTimecode(),
                        createCompilerTask(cbc, LogLevel.ERROR, src));
            }

        }

        @Override
        protected Call processResponse(Call call) throws Exception {
            CodeContextFactoryService.Task<CodeDelegate> task = findTask();
            Class<? extends CodeDelegate> cls = extractCodeDelegateClass(call.getArgs().get(0));
            CodeDelegate delegate = cls.newInstance();
            LogBuilder log = new LogBuilder(task.getLogLevel());
            extractCompilerLog(call.getArgs().get(0), log);
            return Call.createReturnCall(getActiveCall(),
                    PReference.wrap(createContext(task, log, delegate)));
        }

        private CodeContextFactoryService.Task<CodeDelegate> findTask() throws Exception {
            return (CodeContextFactoryService.Task<CodeDelegate>) PReference.coerce(getActiveCall().getArgs().get(0)).getReference();
        }

        private CodeContextFactoryService.Result<CodeDelegate> createContext(
                CodeContextFactoryService.Task<CodeDelegate> task,
                LogBuilder log,
                CodeDelegate delegate) {
            CodeContext<CodeDelegate> context = task.getFactory().task()
                    .attachPrevious(task.getPrevious())
                    .attachLogging(log)
                    .createContext(delegate);
            return new CodeContextFactoryService.Result<>(context, log);
        }

        @Override
        public ControlInfo getInfo() {
            return CodeContextFactoryService.NEW_CONTEXT_INFO;
        }

    }
    
    
    private static class LibraryClassloader extends URLClassLoader {
        
        public LibraryClassloader(ClassLoader parent) {
            super(new URL[0], parent);
        }

        @Override
        protected void addURL(URL url) {
            super.addURL(url);
        }
    }
    

}
