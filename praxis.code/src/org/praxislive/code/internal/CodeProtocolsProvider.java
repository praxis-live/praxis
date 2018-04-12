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
package org.praxislive.code.internal;

import java.util.stream.Stream;
import org.praxislive.code.CodeCompilerService;
import org.praxislive.code.CodeComponentFactoryService;
import org.praxislive.code.CodeContextFactoryService;
import org.praxislive.core.Protocol;

/**
 *
 * @author Neil C Smith - http://www.neilcsmith.net
 */
public class CodeProtocolsProvider implements Protocol.TypeProvider {

    @Override
    public Stream<Protocol.Type> types() {
        return Stream.of(
                new Protocol.Type<>(CodeCompilerService.class),
                new Protocol.Type<>(CodeComponentFactoryService.class),
                new Protocol.Type<>(CodeContextFactoryService.class)
        );
    }
    
}
