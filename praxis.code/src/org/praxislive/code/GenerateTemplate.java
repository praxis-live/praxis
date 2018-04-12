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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for use on CodeDelegate subclasses to create a class body
 * template file during compilation. This relies on the compiler in use allowing
 * the annotation processor to load source files.
 * 
 * Add the token values in comments. All lines between {@code PXJ-BEGIN:} and
 * {@code PXJ-END:} will be copied into the template, excluding the lines
 * containing the template. It is advised to write an ID for the section (eg.
 * body, imports, etc.) after the tokens, but these are not checked in processing.
 * 
 * <pre>
 * {@code
 * @GenerateTemplate(Foo.TEMPLATE_PATH)
 * class Foo extends CoreCodeDelegate {
 * 
 *   final static String TEMPLATE_PATH = "resources/foo.pxj";
 *  
 *   // PXJ-BEGIN:body
 * 
 *   @Override
 *   public void update() {
 *     
 *   }
 * 
 *   // PXJ-END:body
 * 
 * } 
 * </pre>
 * 
 * @author Neil C Smith - http://www.neilcsmith.net
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface GenerateTemplate {
    
    /**
     * Value of the starting token - {@code PXJ-BEGIN:}
     */
    public final static String TOKEN_BEGIN = "PXJ-BEGIN:";

    /**
     * Value of the ending token - {@code PXJ-END:}
     */
    public final static String TOKEN_END = "PXJ-END:";
    
    /**
     * Path, relative to the Java source file, to generate the template. The
     * file should usually be given a {@code .pxj} extension. It can be stored
     * in a final static field in the class for use in the factory (see main example).
     * 
     * @return relative path to template file
     */
    public String value();
    
}
