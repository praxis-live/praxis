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

package net.neilcsmith.praxis.code;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class CoreCodeConnector extends CodeConnector<CoreCodeDelegate> {
    
    private final static Logger LOG = Logger.getLogger(CoreCodeConnector.class.getName());
    
    public final static String SETUP = "setup";
    public final static String UPDATE = "update";
    
    private Method setupMethod;
    private Method updateMethod;
    
    public CoreCodeConnector(CoreCodeFactory factory, CoreCodeDelegate delegate) {
        super(factory, delegate);
    }
    
    protected Method extractSetupMethod() {
        return setupMethod;
    }
    
    protected Method extractUpdateMethod() {
        return updateMethod;
    }

    @Override
    protected void analyseMethod(Method method) {
        LOG.log(Level.FINE, "Analysing method : {0}", method);
        try {
            if (method.getParameterTypes().length == 0
                    && method.getReturnType().equals(Void.TYPE)) {
                if (SETUP.equals(method.getName())) {
                    LOG.log(Level.FINE, "Adding setup method");
                    method.setAccessible(true);
                    setupMethod = method;
                    return;
                } else if (UPDATE.equals(method.getName())) {
                    LOG.log(Level.FINE, "Adding update method");
                    method.setAccessible(true);
                    updateMethod = method;
                    return;
                }
            }
        } catch (SecurityException securityException) {
        }
        super.analyseMethod(method);
    }
    
    
    
    
    
}
