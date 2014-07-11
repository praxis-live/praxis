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
package net.neilcsmith.praxis.code.custom;

import net.neilcsmith.praxis.code.CodeContext;
import net.neilcsmith.praxis.code.CodeProperty;
import net.neilcsmith.praxis.compiler.ClassBodyContext;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.Control;
import net.neilcsmith.praxis.core.info.ArgumentInfo;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.core.types.PMap;
import net.neilcsmith.praxis.core.types.PString;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class CustomCodeProperty extends CodeProperty<CustomCodeDelegate> {

    private final static ControlInfo INFO = ControlInfo.createPropertyInfo(
            new ArgumentInfo[]{
                ArgumentInfo.create(PString.class,
                        PMap.create(
                                PString.KEY_MIME_TYPE, CustomBodyContext.MIME_TYPE,
                                ArgumentInfo.KEY_TEMPLATE, CustomBodyContext.TEMPLATE,
                                ClassBodyContext.KEY, CustomBodyContext.class.getName()
                        ))
            },
            new Argument[]{PString.EMPTY},
            PMap.EMPTY);
    
    @Override
    public ControlInfo getInfo() {
        return INFO;
    }

    @Override
    protected Task<CustomCodeDelegate> createTask(String code) {
        return new TaskImpl(code);
    }
    
    private class TaskImpl extends Task<CustomCodeDelegate> {

        
        private TaskImpl(String code) {
            super(new CustomBodyContext(), code);
        }

        @Override
        protected CodeContext<CustomCodeDelegate> createCodeContext(CustomCodeDelegate delegate) {
            return new CustomCodeContext(new CustomCodeConnector(delegate));
        }
    
    }
   
   

    public static class Descriptor extends CodeProperty.Descriptor<CustomCodeProperty> {
        
        public Descriptor(String id, int index) {
            super(id, index, CustomCodeProperty.class);
        }

        @Override
        public ControlInfo getInfo() {
            return INFO;
        }

        @Override
        protected CustomCodeProperty createControl() {
            return new CustomCodeProperty();
        }

    }

}
