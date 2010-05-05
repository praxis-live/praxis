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
package net.neilcsmith.praxis.script.commands;

import java.io.File;
import java.net.URI;
import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.types.PUri;
import net.neilcsmith.praxis.script.Context;
import net.neilcsmith.praxis.script.ExecutionException;
import net.neilcsmith.praxis.script.Namespace;
import net.neilcsmith.praxis.script.Variable;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class FileCommand extends AbstractInlineCommand {

    public CallArguments process(Context context, Namespace namespace, CallArguments args) throws ExecutionException {
        if (args.getCount() != 1) {
            throw new ExecutionException();
        }
        try {
            Variable pwd = namespace.getVariable("PWD");
            URI base;
            if (pwd == null) {
                base = new File("").toURI();
            } else {
                base = PUri.coerce(pwd.getValue()).value();
            }
            URI path = base.resolve(new URI(null, null, args.getArg(0).toString(), null));
            return CallArguments.create(PUri.valueOf(path));
        } catch (Exception ex) {
            throw new ExecutionException(ex);
        }

    }
}
