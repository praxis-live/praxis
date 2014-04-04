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

package net.neilcsmith.praxis.compiler;

import org.praxislive.compiler.ClassBodyEvaluator;

/**
 *
 * @author Neil C Smith
 */
public class ClassBodyCompiler {
    
    private final static ClassBodyCompiler INSTANCE = new ClassBodyCompiler();
    
    private ClassBodyCompiler() {
        
    }
    
    public <T> Class<T> compile(ClassBodyContext<T> context, String code)
            throws CompilationException {
        try {
            ClassBodyEvaluator cbe = new ClassBodyEvaluator();
            cbe.setExtendedClass(context.getExtendedClass());
            cbe.setImplementedInterfaces(context.getImplementedInterfaces());
            cbe.setDefaultImports(context.getDefaultImports());
            cbe.cook(code);
            return (Class<T>) cbe.getClazz();
        } catch (Exception ex) {
            throw new CompilationException(ex);
        }  
    }
    
    public static ClassBodyCompiler getDefault() {
        return INSTANCE;
    }
    
}
