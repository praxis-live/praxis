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
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.script.ExecutionException;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class LineNode extends CompositeNode {

    private final static Logger log = Logger.getLogger(LineNode.class.getName());

    private Argument[] result;

    public LineNode(List<Node> children) {
        super(children);
    }

    @Override
    protected boolean isThisDone() {
        return (result != null);
    }

    @Override
    protected void writeThisNextCommand(List<Argument> args) 
            throws ExecutionException {
        if (result == null) {
            for (Node child : getChildren()) {
                child.writeResult(args);
            }
            log.finest("LineNode writing command : " + args.toString());
        } else {
            throw new IllegalStateException();
        }

    }

    @Override
    protected void postThisResponse(List<Argument> args) {
        if (result == null) {
            result = args.toArray(new Argument[args.size()]);
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public void writeResult(List<Argument> args) {
        if (result != null) {
            for (Argument arg : result) {
                args.add(arg);
            }
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public void reset() {
        super.reset();
        result = null;
    }
}
