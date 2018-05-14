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
package org.praxislive.video.pgl.code;

import org.praxislive.code.CodeUtils;
import org.praxislive.code.ClassBodyContext;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class P3DClassBodyContext extends ClassBodyContext<P3DCodeDelegate> {

    public final static String TEMPLATE
            = CodeUtils.load(P3DClassBodyContext.class, "resources/p3d_template.pxj");

    private final static String[] IMPORTS = CodeUtils.join(
            CodeUtils.defaultImports(), new String[]{
                "org.praxislive.video.pgl.code.userapi.*",
                "static org.praxislive.video.pgl.code.userapi.Constants.*"
            });
    
    public P3DClassBodyContext() {
        super(P3DCodeDelegate.class);
    }

    @Override
    public String[] getDefaultImports() {
        return IMPORTS.clone();
    }

}
