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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public abstract class QueuedCodeContext<D extends CodeDelegate> extends CodeContext<D> {

    private final List<Invoker> invokers;

    public QueuedCodeContext(CodeConnector<D> connector) {
        super(connector);
        invokers = new ArrayList<>();
    }

    @Override
    protected void invoke(long time, Invoker invoker) {
        if (isActive() && !invokers.contains(invoker)) {
            invokers.add(invoker);
        }
    }

    protected void runInvokeQueue() {
        for (int i = 0; i < invokers.size(); i++) {
            invokers.get(i).invoke();
        }
        invokers.clear();
    }

}
