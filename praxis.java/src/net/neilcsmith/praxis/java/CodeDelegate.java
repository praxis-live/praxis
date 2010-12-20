/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Neil C Smith.
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

package net.neilcsmith.praxis.java;

import net.neilcsmith.praxis.core.Argument;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class CodeDelegate {

    private CodeContext context;
    private long time;
    private boolean installable;

    public CodeDelegate() {
        this(true);
    }

    public CodeDelegate(boolean installable) {

    }

    public void init(CodeContext context, long time) throws Exception {
        if (context == null) {
            throw new NullPointerException();
        }
        this.context = context;
        if (installable) {
            // add param listeners
        }
    }

    public void tick(long time) {
        this.time = time;
    }

    public void dispose() {}

    public Param p(int idx) {
        return context.getParam(idx - 1);
    }

    public double d(int idx) {
        return context.getParam(idx - 1).getDouble(0);
    }

    public int i(int idx) {
        return context.getParam(idx - 1).getInt(0);
    }

    public boolean t(int idx) {
        return context.getTrigger(idx - 1).getAndReset();
    }

    public void send(int idx, Argument arg) {
        context.getOutput(idx - 1).send(arg);
    }

    public void send(int idx, double value) {
        context.getOutput(idx - 1).send(value);
    }

    public long getTime() {
        return time;
    }



}
