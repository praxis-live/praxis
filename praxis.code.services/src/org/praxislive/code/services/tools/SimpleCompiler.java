
/*
 * Forked from Janino - An embedded Java[TM] compiler
 *
 * Copyright 2018 Neil C Smith
 * Copyright (c) 2001-2010, Arno Unkrig
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *       following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 *       following disclaimer in the documentation and/or other materials provided with the distribution.
 *    3. The name of the author may not be used to endorse or promote products derived from this software without
 *       specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.praxislive.code.services.tools;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;

import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

class SimpleCompiler {

    private MessageHandler messageHandler;
    private Map<String, byte[]> classes;
    private JavaCompiler compiler;
    private List<String> options;

    public Map<String, byte[]> getCompiledClasses() {
        return classes;
    }

    public ClassLoader getClassLoader() {
        this.assertCooked();
        return new ByteMapClassLoader(classes, Thread.currentThread().getContextClassLoader());
    }

    protected void cook(final Reader r) throws CompilationException, IOException {
        this.assertNotCooked();

        // Create one Java source file in memory, which will be compiled later.
        final String code = new BufferedReader(r).lines().collect(Collectors.joining("\n"));
        JavaFileObject compilationUnit = new SimpleJavaFileObject(URI.create("simplecompiler"), Kind.SOURCE) {

            @Override
            public boolean isNameCompatible(String simpleName, Kind kind) {
                return true;
            }

            @Override
            public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
                return new StringReader(code);
            }

            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
                return code;
            }

            @Override
            public String toString() {
                return String.valueOf(this.uri);
            }
        };

        if (compiler == null) {
            compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null) {
                throw new CompilationException(
                        "JDK Java compiler not available - probably you're running a JRE, not a JDK",
                        null
                );
            }
        }

        // Get the original FM, which reads class files through this JVM's BOOTCLASSPATH and
        // CLASSPATH.
        final JavaFileManager fm = compiler.getStandardFileManager(null, null, null);

        // Wrap it so that the output files (in our case class files) are stored in memory rather
        // than in files.
        final ByteArrayJavaFileManager fileManager = new ByteArrayJavaFileManager(fm);

        // Run the compiler.
        try {
            final CompilationException[] caughtCompilationException = new CompilationException[1];
            if (!compiler.getTask(
                    null, // out
                    fileManager, // fileManager
                    new DiagnosticListener<JavaFileObject>() { // diagnosticListener

                        @Override
                        public void report(Diagnostic<? extends JavaFileObject> diagnostic) {

                            String message = "[" + diagnostic.getLineNumber() + ":" + diagnostic.getColumnNumber()
                                    + "] " + diagnostic.getMessage(null) + " (" + diagnostic.getCode() + ")";

                            try {
                                switch (diagnostic.getKind()) {
                                    case ERROR:
                                        if (SimpleCompiler.this.messageHandler != null) {
                                            messageHandler.handleError(message);
                                        }
                                        throw new CompilationException(message);
                                    case MANDATORY_WARNING:
                                    case WARNING:
                                        if (messageHandler != null) {
                                            messageHandler.handleWarning(message);
                                        }
                                        break;
                                    case NOTE:
                                    case OTHER:
                                    default:
                                        break;

                                }
                            } catch (CompilationException ce) {
                                if (caughtCompilationException[0] == null) {
                                    caughtCompilationException[0] = ce;
                                }
                            }
                        }
                    },
                    options == null ? Arrays.asList(new String[]{"-Xlint:all"}) : options, // options
                    null, // classes
                    Collections.singleton(compilationUnit) // compilationUnits
            ).call()) {
                if (caughtCompilationException[0] != null) {
                    throw caughtCompilationException[0];
                }
                throw new CompilationException("Compilation failed", null);
            }
        } catch (RuntimeException rte) {

            // Unwrap the compilation exception and throw it.
            for (Throwable t = rte.getCause(); t != null; t = t.getCause()) {
                if (t instanceof CompilationException) {
                    throw (CompilationException) t; // SUPPRESS CHECKSTYLE AvoidHidingCause
                }
                if (t instanceof IOException) {
                    throw (IOException) t; // SUPPRESS CHECKSTYLE AvoidHidingCause
                }
            }
            throw rte;
        }

        classes = fileManager.extractClassData();

    }

    public void setMessageHandler(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    public void setCompiler(JavaCompiler compiler) {
        this.compiler = compiler;
    }
    
    public void setOptions(List<String> options) {
        this.options = options;
    }

    /**
     * Throw an {@link IllegalStateException} if this {@link Cookable} is not
     * yet cooked.
     */
    protected void assertCooked() {
        if (this.classes == null) {
            throw new IllegalStateException("Not yet cooked");
        }
    }

    /**
     * Throw an {@link IllegalStateException} if this {@link Cookable} is
     * already cooked.
     */
    protected void assertNotCooked() {
        if (this.classes != null) {
            throw new IllegalStateException("Already cooked");
        }
    }

}
