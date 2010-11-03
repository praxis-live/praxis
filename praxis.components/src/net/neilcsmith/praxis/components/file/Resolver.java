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

package net.neilcsmith.praxis.components.file;

import java.io.File;
import java.net.URI;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ControlPort;
import net.neilcsmith.praxis.core.types.PUri;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.impl.ArgumentInputPort;
import net.neilcsmith.praxis.impl.DefaultControlOutputPort;
import net.neilcsmith.praxis.impl.UriProperty;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class Resolver extends AbstractComponent {

    private UriProperty base;
    private ControlPort.Output out;
    private ControlPort.Output error;

    public Resolver() {
        build();
    }

    private void build() {
        registerPort("in", ArgumentInputPort.create( new ArgumentInputPort.Binding() {

            public void receive(long time, Argument arg) {
                resolve(time, arg);
            }
        }));
        base = UriProperty.create( PUri.valueOf(new File("").toURI()));
        registerControl("base", base);
        out = new DefaultControlOutputPort(this);
        error = new DefaultControlOutputPort(this);
        registerPort("out", out);
        registerPort("error", error);
    }
    
    private void resolve(long time, Argument arg) {
        try {
            URI res = base.getValue().value();
            res = res.resolve(new URI(null, null, arg.toString(), null));
            out.send(time, PUri.valueOf(res));
        } catch (Exception ex) {
            error.send(time, arg);
        }
    }
}
