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
package org.praxislive.code.services;

import java.util.Arrays;
import java.util.List;
import org.praxislive.core.Root;
import org.praxislive.core.RootHub;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class CodeServicesExtensionProvider implements RootHub.ExtensionProvider {

    @Override
    public List<Root> getExtensions() {
        return Arrays.asList(new Root[] {
            new DefaultCompilerService(),
            new DefaultCodeFactoryService()
        });
    }
    
}
