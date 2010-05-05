/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 - Neil C Smith. All rights reserved.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details.
 *
 * You should have received a copy of the GNU General Public License version 2
 * along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 */

package net.neilcsmith.praxis.script.ast;

import java.util.List;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ComponentAddress;
import net.neilcsmith.praxis.core.ControlAddress;
import net.neilcsmith.praxis.core.PortAddress;
import net.neilcsmith.praxis.script.Namespace;

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
    public void writeResult(List<Argument> args) {
        if (namespace == null) {
            throw new IllegalStateException();
        }
        args.add(parseAddress());
    }

    private Argument parseAddress() {
        try {
            ComponentAddress ctxt = ComponentAddress.coerce(namespace.getVariable("_CTXT").getValue());
            if (address.charAt(1) == '/') {
                return parseComplexAddress(ctxt);
            } else {
                return ControlAddress.create(ctxt, address.substring(1));
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    private Argument parseComplexAddress(ComponentAddress ctxt) throws Exception {
        String full = ctxt.toString() + address.substring(1);
        if (full.lastIndexOf('.') > -1) {
            return ControlAddress.valueOf(full);
        } else if (full.lastIndexOf('!') > -1) {
            return PortAddress.valueOf(full);
        } else {
            return ComponentAddress.valueOf(full);
        }
    }

}
