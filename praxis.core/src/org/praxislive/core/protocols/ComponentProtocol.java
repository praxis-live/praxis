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
package org.praxislive.core.protocols;

import java.util.stream.Stream;
import org.praxislive.core.ArgumentInfo;
import org.praxislive.core.ComponentInfo;
import org.praxislive.core.ControlInfo;
import org.praxislive.core.Info;
import org.praxislive.core.Protocol;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class ComponentProtocol implements Protocol {
    
    public final static ComponentProtocol INSTANCE = new ComponentProtocol();
    public final static String INFO = "info";
    public final static ControlInfo INFO_INFO = ControlInfo.createReadOnlyPropertyInfo(
            new ArgumentInfo[]{ComponentInfo.info()},
            null);
    ;
    public final static ComponentInfo API_INFO = Info.component(cmp -> cmp
            .protocol(ComponentProtocol.class)
            .control(INFO, INFO_INFO)
    );
    
    @Override
    public Stream<String> controls() {
        return Stream.of(INFO);
    }
    
    @Override
    public ControlInfo getControlInfo(String control) {
        if (INFO.equals(control)) {
            return INFO_INFO;
        }
        throw new IllegalArgumentException();
    }
    
}
