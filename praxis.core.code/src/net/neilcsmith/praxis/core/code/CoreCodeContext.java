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
 *
 */
package net.neilcsmith.praxis.core.code;

import java.util.logging.Logger;
import net.neilcsmith.praxis.code.CodeContext;
import net.neilcsmith.praxis.core.ExecutionContext;
import net.neilcsmith.praxis.logging.LogLevel;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class CoreCodeContext extends CodeContext<CoreCodeDelegate> {

    private final static Logger LOG = Logger.getLogger(CoreCodeContext.class.getName());

    private final Driver driver;
    private final boolean hasUpdateMethod;

    private ExecutionContext execCtxt;

    public CoreCodeContext(CoreCodeConnector connector) {
        super(connector);
        hasUpdateMethod = connector.hasUpdateMethod();
        driver = new Driver();
    }

    @Override
    protected void hierarchyChanged() {
        super.hierarchyChanged();
        ExecutionContext ctxt = getLookup().get(ExecutionContext.class);
        if (execCtxt != ctxt) {
            if (execCtxt != null) {
                execCtxt.removeStateListener(driver);
                execCtxt.removeClockListener(driver);
            }
            execCtxt = ctxt;
            if (ctxt != null) {
                ctxt.addStateListener(driver);
                if (hasUpdateMethod) {
                    ctxt.addClockListener(driver);
                }
                if (ctxt.getState() == ExecutionContext.State.ACTIVE) {
                    update(ctxt.getTime());
                    try {
                        getDelegate().setup();
                    } catch (Exception e) {
                        getLog().log(LogLevel.ERROR, e, "Exception thrown during setup()");
                    }
                }
            }
        }
    }

    private class Driver implements ExecutionContext.StateListener,
            ExecutionContext.ClockListener {

        @Override
        public void stateChanged(ExecutionContext source) {
            if (source.getState() == ExecutionContext.State.ACTIVE) {
                update(source.getTime());
                try {
                    getDelegate().setup();
                } catch (Exception e) {
                    getLog().log(LogLevel.ERROR, e, "Exception thrown during setup()");
                }
                try {
                    getDelegate().starting();
                } catch (Exception e) {
                    getLog().log(LogLevel.ERROR, e, "Exception thrown during starting()");
                }
            } else {
                update(source.getTime());
                try {
                    getDelegate().stopping();
                } catch (Exception e) {
                    getLog().log(LogLevel.ERROR, e, "Exception thrown during stopping()");
                }
            }
            flush();
        }

        @Override
        public void tick(ExecutionContext source) {
            update(source.getTime());
            try {
                getDelegate().update();
            } catch (Exception e) {
                getLog().log(LogLevel.ERROR, e, "Exception thrown during update()");
            }
            flush();
        }

    }

}
