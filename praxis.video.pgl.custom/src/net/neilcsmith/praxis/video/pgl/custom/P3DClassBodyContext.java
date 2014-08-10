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

package net.neilcsmith.praxis.video.pgl.custom;

import net.neilcsmith.praxis.compiler.ClassBodyContext;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class P3DClassBodyContext extends ClassBodyContext<P3DCodeDelegate> {
    
    public final static String MIME_TYPE = "text/x-praxis-java";
    public final static String TEMPLATE =
            "\n@Override\npublic void setup() {\n  \n}\n@Override\npublic void draw() {\n  \n}";
            
    
    private final static String[] IMPORTS = {
        "java.util.*",
        "net.neilcsmith.praxis.code.userapi.*",
        "net.neilcsmith.praxis.video.pgl.custom.userapi.*",
        "static net.neilcsmith.praxis.code.userapi.Constants.*",
        "static net.neilcsmith.praxis.video.pgl.custom.userapi.Constants.*"
    };
    
    public P3DClassBodyContext() {
        super(P3DCodeDelegate.class);
    }

    @Override
    public String[] getDefaultImports() {
        return IMPORTS.clone();
    }
    
    
    
}
