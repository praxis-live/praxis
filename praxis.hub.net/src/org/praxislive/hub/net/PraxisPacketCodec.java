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

package org.praxislive.hub.net;

import de.sciss.net.OSCPacketCodec;
import org.praxislive.core.Value;
import org.praxislive.core.types.PNumber;
import org.praxislive.core.types.PString;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
class PraxisPacketCodec extends OSCPacketCodec {

    public PraxisPacketCodec() {
        super(MODE_FAT_V1);
    }
    
    Value toArgument(Object o) {
        if (o instanceof Double) {
            return PNumber.of((Double) o);
        } else if (o instanceof Integer) {
            return PNumber.of((Integer) o);
        } else {
            return PString.of(o);
        }
    }

    Object toOSCObject(Value arg) {
        if (arg instanceof PNumber) {
            PNumber n = (PNumber) arg;
            if (n.isInteger()) {
                return n.toIntValue();
            } else {
                return n.value();
            }
        } else {
            return arg.toString();
        }

    }
    
}
