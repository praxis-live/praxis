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
import net.neilcsmith.praxis.script.ExecutionException;
import net.neilcsmith.praxis.script.Namespace;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public abstract class Node {

    public void init(Namespace namespace) {
    }

    public boolean isDone() {
        return true;
    }

    public void writeNextCommand(List<Argument> args) 
            throws ExecutionException {
        throw new ExecutionException();
    }

    public void postResponse(List<Argument> args)
            throws ExecutionException {
        throw new ExecutionException();
    }

    public abstract void writeResult(List<Argument> args)
            throws ExecutionException;

    public void reset() {
    }
}
