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
package net.neilcsmith.praxis.impl;

import java.util.logging.Logger;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.Component;
import net.neilcsmith.praxis.core.info.ArgumentInfo;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.core.types.PMap;
import net.neilcsmith.praxis.core.types.PResource;

/**
 *
 * @author Neil C Smith
 */
public class UriProperty extends AbstractSingleArgProperty {

    private static Logger logger = Logger.getLogger(UriProperty.class.getName());
    private Binding binding;


    private UriProperty(Binding binding, ControlInfo info) {
        super(info);
        this.binding = binding;
    }

    @Override
    protected void set(long time, Argument value) throws Exception {
        binding.setBoundValue(time, PResource.coerce(value));
    }

    @Override
    protected void set(long time, double value) throws Exception {
        throw new IllegalArgumentException();
    }

    @Override
    protected Argument get() {
        return binding.getBoundValue();
    }
    


    public PResource getValue() {
        return binding.getBoundValue();
    }



    public static UriProperty create(
             PResource def) {
        return create(null, def, null);
    }

    public static UriProperty create(Binding binding, PResource def) {
         return create(binding, def, null);
    }

    public static UriProperty create(
            Binding binding, PResource def, PMap properties) {

        if (binding == null) {
            binding = new DefaultBinding(def);
        }

        ArgumentInfo[] arguments = new ArgumentInfo[]{PResource.info()};

        Argument[] defaults = new Argument[]{def};
        ControlInfo info = ControlInfo.createPropertyInfo(arguments, defaults, properties);
        return new UriProperty(binding, info);
    }

    public static interface Binding {

        public void setBoundValue(long time, PResource value);

        public PResource getBoundValue();
    }

    private static class DefaultBinding implements Binding {

        private PResource value;

        private DefaultBinding(PResource value) {
            this.value = value;
        }

        @Override
        public void setBoundValue(long time, PResource value) {
            this.value = value;
        }

        @Override
        public PResource getBoundValue() {
            return value;
        }
    }
}
