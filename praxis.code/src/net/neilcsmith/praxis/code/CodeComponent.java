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
package net.neilcsmith.praxis.code;

import net.neilcsmith.praxis.core.Component;
import net.neilcsmith.praxis.core.ComponentAddress;
import net.neilcsmith.praxis.core.Container;
import net.neilcsmith.praxis.core.Control;
import net.neilcsmith.praxis.core.InterfaceDefinition;
import net.neilcsmith.praxis.core.Lookup;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.core.VetoException;
import net.neilcsmith.praxis.core.info.ComponentInfo;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public abstract class CodeComponent<T extends CodeDelegate> implements Component {

    private Container parent;
    private CodeContext<T> codeCtxt;
    private ComponentAddress address;
    
    protected CodeComponent(CodeContext<T> codeCtxt) {
        if (codeCtxt == null) {
            throw new NullPointerException();
        }
        this.codeCtxt = codeCtxt;
        codeCtxt.configure(this, null);
    }

    @Override
    public Container getParent() {
        return parent;
    }

    @Override
    public final void parentNotify(Container parent) throws VetoException {
        if (parent == null) {
            if (this.parent != null) {
                this.parent = null;
                disconnectAll();
            }
        } else {
            if (this.parent != null) {
                throw new VetoException();
            }
            this.parent = parent;
        }
    }
    
    private void disconnectAll() {
        for (String portID : getPortIDs()) {
            getPort(portID).disconnectAll();
        }
    }

    @Override
    public void hierarchyChanged() {
        if (parent != null) {
            address = parent.getAddress(this);
        } else {
            address = null;
        }
        codeCtxt.hierarchyChanged();
    }

    @Override
    public Control getControl(String id) {
        return codeCtxt.getControl(id);
    }

    @Override
    public String[] getControlIDs() {
        return codeCtxt.getControlIDs();
    }

    @Override
    public Port getPort(String id) {
        return codeCtxt.getPort(id);
    }

    @Override
    public String[] getPortIDs() {
        return codeCtxt.getPortIDs();
    }

    @Override
    public ComponentInfo getInfo() {
        return codeCtxt.getInfo();
    }

    @Override
    public InterfaceDefinition[] getInterfaces() {
        return codeCtxt.getInterfaces();
    }

    protected Lookup getLookup() {
        if (parent != null) {
            return parent.getLookup();
        } else {
            return Lookup.EMPTY;
        }
    }
    
    protected void install(CodeContext<T> cc) {
        cc.configure(this, codeCtxt);
        codeCtxt.dispose();
        codeCtxt = cc;
    }
    
    protected ComponentAddress getAddress() {
        return address;
    }
    
}
