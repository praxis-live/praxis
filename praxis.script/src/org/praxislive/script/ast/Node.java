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
import org.praxislive.script.ExecutionException;
import org.praxislive.script.Namespace;

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

    public void writeNextCommand(List<Value> args) 
            throws ExecutionException {
        throw new ExecutionException();
    }

    public void postResponse(List<Value> args)
            throws ExecutionException {
        throw new ExecutionException();
    }

    public abstract void writeResult(List<Value> args)
            throws ExecutionException;

    public void reset() {
    }
}
