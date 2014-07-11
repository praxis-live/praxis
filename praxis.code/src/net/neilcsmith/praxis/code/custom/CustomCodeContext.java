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
package net.neilcsmith.praxis.code.custom;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.code.CodeContext;
import net.neilcsmith.praxis.core.ExecutionContext;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class CustomCodeContext extends CodeContext<CustomCodeDelegate> {

    private final static Logger LOG = Logger.getLogger(CustomCodeContext.class.getName());

    private final Driver driver;
    private final Method setupMethod;
    private final Method updateMethod;

    private ExecutionContext execCtxt;

    public CustomCodeContext(CustomCodeConnector connector) {
        super(connector);
        driver = new Driver();
        Method s = connector.extractSetupMethod();
        Method u = connector.extractUpdateMethod();
        setupMethod = s;
        updateMethod = u;
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
            if (ctxt != null) {
                ctxt.addStateListener(driver);
                ctxt.addClockListener(driver);
                driver.stateChanged(ctxt);
            }
            execCtxt = ctxt;
        }
    }

    @Override
    public long getTime() {
        return execCtxt.getTime();
    }

    private class Driver implements ExecutionContext.StateListener,
            ExecutionContext.ClockListener {

        boolean setupRequired;

        private Driver() {
            setupRequired = true;
        }

        @Override
        public void stateChanged(ExecutionContext source) {
            setupRequired = true;
        }

        @Override
        public void tick(ExecutionContext source) {
            processClock();
            if (setupRequired) {
                if (setupMethod != null) {
                    try {
                        LOG.log(Level.FINE, "Invoking setup method");
                        setupMethod.invoke(getDelegate());
                    } catch (Exception ex) {
                        // @TODO logging
                    } finally {
                        setupRequired = false;
                    }
                }
            }
            if (updateMethod != null) {
                try {
                    LOG.log(Level.FINE, "Invoking update method");
                    updateMethod.invoke(getDelegate());
                } catch (Exception ex) {
                    //@TODO logging
                }
            }

        }

    }

}
