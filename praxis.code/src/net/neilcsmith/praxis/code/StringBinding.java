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

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.info.ArgumentInfo;
import net.neilcsmith.praxis.core.types.PArray;
import net.neilcsmith.praxis.core.types.PMap;
import net.neilcsmith.praxis.core.types.PString;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
class StringBinding extends PropertyControl.Binding {

    private final Set<PString> allowed;
    private final PString mime;
    private final PString def;

    private PString value;

    StringBinding() {
        this("","");
    }
    
    StringBinding(String mime, String def) {
        allowed = null;
        this.mime = mime == null ? PString.EMPTY : PString.valueOf(mime);
        this.def = def == null ? PString.EMPTY : PString.valueOf(def);
    }
    
    StringBinding(String[] allowedValues, String def) {
        if (allowedValues.length == 0) {
            throw new IllegalArgumentException();
        }
        boolean foundDef = false;
        allowed = new LinkedHashSet<>(allowedValues.length);
        for (String s : allowedValues) {
            allowed.add(PString.valueOf(s));
            if (s.equals(def)) {
                foundDef = true;
            }
        }
        this.def = foundDef ? PString.valueOf(def) :
                PString.valueOf(allowedValues[0]);
        mime = PString.EMPTY;
    }

    @Override
    public void set(long time, Argument value) throws Exception {
        set(PString.coerce(value));
    }

    @Override
    public void set(long time, double value) throws Exception {
        set(PString.valueOf(Double.toString(value)));
    }

    private void set(PString value) throws Exception {
        if (allowed == null || allowed.contains(value)) {
            this.value = value;
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public Argument get() {
        return value;
    }

    @Override
    public ArgumentInfo getArgumentInfo() {
        PMap keys = PMap.EMPTY;
        if (allowed != null) {
            keys = PMap.create(PString.KEY_ALLOWED_VALUES, PArray.valueOf(allowed));
        } else if (!mime.isEmpty()) {
            keys = PMap.create(PString.KEY_MIME_TYPE, mime);
        }
        return ArgumentInfo.create(PString.class, keys);
    }

    @Override
    public Argument getDefaultValue() {
        return PString.valueOf(def);
    }

}
