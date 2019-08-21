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
package org.praxislive.code;

import java.util.stream.Stream;
import org.praxislive.core.ArgumentInfo;
import org.praxislive.core.ControlInfo;
import org.praxislive.core.services.Service;
import org.praxislive.core.types.PMap;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class CodeCompilerService implements Service {

    public final static String COMPILE = "compile";
    public final static ControlInfo COMPILE_INFO = 
            ControlInfo.createFunctionInfo(
            new ArgumentInfo[]{ArgumentInfo.of(PMap.class)},
            new ArgumentInfo[]{ArgumentInfo.of(PMap.class)},
            PMap.EMPTY);

    // parameter keys
    public final static String KEY_CLASS_BODY_CONTEXT =
            "class-body-context";
    public final static String KEY_CODE =
            "code";
    public final static String KEY_LOG_LEVEL = 
            "log-level";
    
    // response keys
    public final static String KEY_CLASSES =
            "classes";
    public final static String KEY_LOG =
            "log";
    
    @Override
    public Stream<String> controls() {
        return Stream.of(COMPILE);
    }

    @Override
    public ControlInfo getControlInfo(String control) {
        if (COMPILE.equals(control)) {
            return COMPILE_INFO;
        }
        throw new IllegalArgumentException();
    }
}
