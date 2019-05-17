/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2019 Neil C Smith.
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
package org.praxislive.code.services.tools;

import java.io.File;
import java.io.StringReader;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.lang.model.SourceVersion;
import javax.tools.JavaCompiler;
import org.praxislive.code.ClassBodyContext;

/**
 *
 * @author Neil C Smith
 */
public class ClassBodyCompiler {

    public final static String DEFAULT_CLASS_NAME = "$";

    private final ClassBodyContext<?> classBodyContext;
    private final Set<File> extClasspath;
    private final String defClasspath;

    private MessageHandler messageHandler;
    private JavaCompiler compiler;
    private SourceVersion release;

    private ClassBodyCompiler(ClassBodyContext<?> classBodyContext) {
        this.classBodyContext = classBodyContext;
        this.release = SourceVersion.RELEASE_8;
        this.extClasspath = new LinkedHashSet<>();
        this.defClasspath = System.getProperty("env.class.path", "");
    }

    public ClassBodyCompiler addMessageHandler(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
        return this;
    }

    public ClassBodyCompiler extendClasspath(Set<File> libs) {
        extClasspath.addAll(libs);
        return this;
    }

    public ClassBodyCompiler setCompiler(JavaCompiler compiler) {
        this.compiler = compiler;
        return this;
    }
    
    public ClassBodyCompiler setRelease(SourceVersion release) {
        this.release = release;
        return this;
    }

    public Map<String, byte[]> compile(String code) throws CompilationException {
        try {
            ClassBodyEvaluator cbe = new ClassBodyEvaluator();
            cbe.setCompiler(compiler);
            cbe.setExtendedClass(classBodyContext.getExtendedClass());
            cbe.setImplementedInterfaces(classBodyContext.getImplementedInterfaces());
            cbe.setDefaultImports(classBodyContext.getDefaultImports());
            if (messageHandler != null) {
                cbe.setMessageHandler(messageHandler);
            }
            if (compiler.isSupportedOption("--release") == 1) {
                cbe.setOptions(Arrays.asList("-Xlint:all", "-proc:none",
                        "--release", String.valueOf(release.ordinal()),
                        "-classpath", buildClasspath()));
            } else {
                cbe.setOptions(Arrays.asList("-Xlint:all", "-proc:none",
                        "-classpath", buildClasspath()));
            }
            cbe.cook(new StringReader(code));
            return cbe.getCompiledClasses();
        } catch (CompilationException ex) {
            throw new CompilationException(ex);
        } catch (Exception ex) {
            throw new CompilationException(ex);
        }
    }
    private String buildClasspath() {
        if (extClasspath.isEmpty()) {
            return defClasspath;
        } else {
            return extClasspath.stream()
                    .map(f -> f.getAbsolutePath())
                    .collect(Collectors.joining(File.pathSeparator, 
                            "", File.pathSeparator + defClasspath));
        }
    }

    public static ClassBodyCompiler create(ClassBodyContext<?> classBodyContext) {
        return new ClassBodyCompiler(classBodyContext);
    }

}
