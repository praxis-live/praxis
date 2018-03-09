/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2018 Neil C Smith.
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
package org.praxislive.core.components;

import org.praxislive.code.GenerateTemplate;

// default imports
import java.util.*;
import org.praxislive.core.Value;
import org.praxislive.core.ValueFormatException;
import org.praxislive.core.types.*;
import org.praxislive.code.userapi.*;
import org.praxislive.core.code.CoreCodeDelegate;
import static org.praxislive.code.userapi.Constants.*;

/**
 *
 * @author Neil C Smith - http://www.neilcsmith.net
 */
@GenerateTemplate(CoreVariable.TEMPLATE_PATH)
public class CoreVariable extends CoreCodeDelegate {
    
    final static String TEMPLATE_PATH = "resources/variable.pxj";

    // PXJ-BEGIN:body
    
    @P(1) Property value;

    @Out(1) Output out;
        
    @T(1) void trigger() {
        out.send(value.get());
    }
    
    // PXJ-END:body
    
}
