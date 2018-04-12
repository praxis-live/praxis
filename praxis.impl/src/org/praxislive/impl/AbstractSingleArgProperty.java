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
package org.praxislive.impl;

import org.praxislive.core.Value;
import org.praxislive.core.CallArguments;
import org.praxislive.core.ControlPort;
import org.praxislive.core.ArgumentInfo;
import org.praxislive.core.ControlInfo;
import org.praxislive.core.types.PMap;
import org.praxislive.core.types.PString;

/**
 *
 * @author Neil C Smith
 */
public abstract class AbstractSingleArgProperty extends AbstractProperty {

    protected AbstractSingleArgProperty(ControlInfo info) {
        super(info);
    }

    public InputPort createPort() {
        return new InputPort();
    }

    protected void setArguments(long time, CallArguments args) throws Exception {
        set(time, args.get(0));
    }

    protected CallArguments getArguments() {
        return CallArguments.create(get());
    }

    protected abstract void set(long time, Value value) throws Exception;

    protected abstract void set(long time, double value) throws Exception;

    protected abstract Value get();

    public class InputPort extends AbstractControlInputPort {

        @Override
        public void receive(long time, double value) {
            try {
                if (isLatest(time)) {
                    set(time, value);
                    setLatest(time);
                }
            } catch (Exception ex) {
                // @TODO logging
            }
        }

        @Override
        public void receive(long time, Value value) {
            try {
                if (isLatest(time)) {
                    set(time, value);
                    setLatest(time);
                }
            } catch (Exception ex) {
                // @TODO logging
            }
        }
    }
    
    public static abstract class Builder<B extends Builder<B>> extends AbstractProperty.Builder<B> {
        
        private Class<? extends Value> typeClass;
        private PMap.Builder argProps;
        
        protected Builder() {
            this(Value.class);
        }
        
        protected Builder(Class<? extends Value> typeClass) {
            if (typeClass == null) {
                throw new NullPointerException();
            }
            this.typeClass = typeClass;
        }
       
        @SuppressWarnings("unchecked")
        protected B putArgumentProperty(String key, Value value) {
            if (argProps == null) {
                argProps = PMap.builder();
            }
            argProps.put(PString.valueOf(key), value);
            return (B) this;
        }
        
        @SuppressWarnings("unchecked")
        protected B argumentType(Class<? extends Value> typeClass) {
            if (typeClass == null) {
                throw new NullPointerException();
            }
            this.typeClass = typeClass;
            return (B) this;
        }

        @Override
        protected ControlInfo buildInfo() {
            PMap props = argProps == null ? PMap.EMPTY : argProps.build();
            arguments(ArgumentInfo.create(typeClass, props));
            return super.buildInfo();
        }
        
        
        
    }
    
}
