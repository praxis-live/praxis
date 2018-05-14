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
package org.praxislive.tinkerforge;

import org.praxislive.code.CodeUtils;
import org.praxislive.code.ClassBodyContext;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class TFBodyContext extends ClassBodyContext<TFCodeDelegate> {

    public final static String TEMPLATE
            = CodeUtils.load(TFBodyContext.class, "resources/tf_template.pxj");

    private final static String[] IMPORTS = CodeUtils.join(
            CodeUtils.defaultImports(), new String[]{
                "com.tinkerforge.*",
                "org.praxislive.tinkerforge.userapi.*",
                "static org.praxislive.tinkerforge.userapi.Constants.*"
            });

    public TFBodyContext() {
        super(TFCodeDelegate.class);
    }

    @Override
    public String[] getDefaultImports() {
        return IMPORTS.clone();
    }

}
