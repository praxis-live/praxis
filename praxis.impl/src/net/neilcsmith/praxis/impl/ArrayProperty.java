/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 - Neil C Smith. All rights reserved.
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
package net.neilcsmith.praxis.impl;

import java.util.logging.Logger;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.Component;
import net.neilcsmith.praxis.core.info.ArgumentInfo;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.core.types.PArray;

/**
 *
 * @author Neil C Smith
 */
public class ArrayProperty extends AbstractSingleArgProperty {

    private static Logger logger = Logger.getLogger(ArrayProperty.class.getName());
    private Binding binding;


    private ArrayProperty(Component component, Binding binding, ControlInfo info) {
        super(component, info);
        this.binding = binding;
    }


    public PArray getValue() {
        return binding.getBoundValue();
    }

    @Override
    protected void set(long time, Argument value) throws Exception {
        binding.setBoundValue(time, PArray.coerce(value));
    }

    @Override
    protected void set(long time, double value) throws Exception {
        throw new IllegalArgumentException();
    }

    @Override
    protected Argument get() {
        return binding.getBoundValue();
    }

    

    public static ArrayProperty create(Component component) {
        return create(component, null, PArray.EMPTY);
    }

    public static ArrayProperty create(Component component, PArray def) {
        return create(component, null, def);
    }

    public static ArrayProperty create(Component component, Binding binding, PArray def) {

        if (binding == null) {
            binding = new DefaultBinding(def);
        }
        ArgumentInfo[] arguments = new ArgumentInfo[]{PArray.info()};

        Argument[] defaults = new Argument[]{def};
        ControlInfo info = ControlInfo.createPropertyInfo(arguments, defaults, null);
        return new ArrayProperty(component, binding, info);
    }

    public static interface Binding {

        public void setBoundValue(long time, PArray value);

        public PArray getBoundValue();
    }

    private static class DefaultBinding implements Binding {

        private PArray value;

        private DefaultBinding(PArray value) {
            this.value = value;
        }

        @Override
        public void setBoundValue(long time, PArray value) {
            this.value = value;
        }

        @Override
        public PArray getBoundValue() {
            return value;
        }
    }

}
