/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Neil C Smith.
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
package net.neilcsmith.praxis.audio.code.userapi;

import net.neilcsmith.praxis.audio.code.Resettable;
import org.jaudiolibs.audioops.impl.IIRFilterOp;
import org.jaudiolibs.pipes.impl.OpHolder;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public final class IIRFilter extends OpHolder<IIRFilterOp> implements Resettable {

    public final static IIRFilterOp.Type LP6 = IIRFilterOp.Type.LP6;
    public final static IIRFilterOp.Type LP12 = IIRFilterOp.Type.LP12;
    public final static IIRFilterOp.Type LP24 = IIRFilterOp.Type.LP24;
    public final static IIRFilterOp.Type HP12 = IIRFilterOp.Type.HP12;
    public final static IIRFilterOp.Type HP24 = IIRFilterOp.Type.HP24;
    public final static IIRFilterOp.Type BP12 = IIRFilterOp.Type.BP12;
    public final static IIRFilterOp.Type NP12 = IIRFilterOp.Type.NP12;

    private final IIRFilterOp filter;

    public IIRFilter() {
        this.filter = new IIRFilterOp();
        reset();
        setOp(filter);
    }

    public IIRFilter frequency(double frequency) {
        filter.setFrequency((float) Utils.constrain(frequency, 20, 20000));
        return this;
    }

    public double frequency() {
        return filter.getFrequency();
    }

    public IIRFilter resonance(double db) {
        filter.setResonance((float) Utils.constrain(db, 0, 30));
        return this;
    }

    public double resonance() {
        return filter.getResonance();
    }

    public IIRFilter type(IIRFilterOp.Type type) {
        filter.setFilterType(type);
        return this;
    }

    public IIRFilterOp.Type type() {
        return filter.getFilterType();
    }

    @Override
    public void reset() {
        filter.setResonance(0);
        filter.setFrequency(20000);
        filter.setFilterType(LP6);
    }

}
