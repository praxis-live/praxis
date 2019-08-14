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

package org.praxislive.script.ast;

import java.util.List;
import org.praxislive.core.Value;
import org.praxislive.core.ComponentAddress;
import org.praxislive.core.ControlAddress;
import org.praxislive.core.PortAddress;
import org.praxislive.script.Env;
import org.praxislive.script.Namespace;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class AddressNode extends Node {

    private String address;
    private Namespace namespace;

    public AddressNode(String address) {
        if (address == null) {
            throw new NullPointerException();
        }
        this.address = address;
    }

    @Override
    public void init(Namespace namespace) {
        super.init(namespace);
        this.namespace = namespace;
    }

    @Override
    public void reset() {
        super.reset();
        this.namespace = null;
    }



    @Override
    public void writeResult(List<Value> args) {
        if (namespace == null) {
            throw new IllegalStateException();
        }
        args.add(parseAddress());
    }

    private Value parseAddress() {
        try {
            ComponentAddress ctxt = ComponentAddress.coerce(namespace.getVariable(Env.CONTEXT).getValue());
            if (address.charAt(1) == '/') {
                return parseComplexAddress(ctxt);
            } else {
                return ControlAddress.of(ctxt, address.substring(1));
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    private Value parseComplexAddress(ComponentAddress ctxt) throws Exception {
        String full = ctxt.toString() + address.substring(1);
        if (full.lastIndexOf('.') > -1) {
            return ControlAddress.parse(full);
        } else if (full.lastIndexOf('!') > -1) {
            return PortAddress.parse(full);
        } else {
            return ComponentAddress.parse(full);
        }
    }

}
