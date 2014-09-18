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

package net.neilcsmith.praxis.hub.net;

import de.sciss.net.OSCPacketCodec;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.types.PNumber;
import net.neilcsmith.praxis.core.types.PString;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
class PraxisPacketCodec extends OSCPacketCodec {

    public PraxisPacketCodec() {
        super(MODE_FAT_V1);
    }
    
    Argument toArgument(Object o) {
        if (o instanceof Double) {
            return PNumber.valueOf((Double) o);
        } else if (o instanceof Integer) {
            return PNumber.valueOf((Integer) o);
        } else {
            return PString.valueOf(o);
        }
    }

    Object toOSCObject(Argument arg) {
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
