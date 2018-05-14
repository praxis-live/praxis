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
 *
 */
package org.praxislive.audio.code.userapi;

import org.praxislive.audio.code.Resettable;
import org.jaudiolibs.audioops.impl.IIRFilterOp;
import org.jaudiolibs.pipes.impl.OpHolder;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public final class IIRFilter extends OpHolder<IIRFilterOp> implements Resettable {

    public enum Type {
        LP6, LP12, LP24, HP12, HP24, BP12, NP12
    }

    public final static Type LP6 = Type.LP6;
    public final static Type LP12 = Type.LP12;
    public final static Type LP24 = Type.LP24;
    public final static Type HP12 = Type.HP12;
    public final static Type HP24 = Type.HP24;
    public final static Type BP12 = Type.BP12;
    public final static Type NP12 = Type.NP12;

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

    public IIRFilter type(Type type) {
        switch (type) {
            case LP6:
                filter.setFilterType(IIRFilterOp.Type.LP6);
                break;
            case LP12:
                filter.setFilterType(IIRFilterOp.Type.LP12);
                break;
            case LP24:
                filter.setFilterType(IIRFilterOp.Type.LP24);
                break;
            case HP12:
                filter.setFilterType(IIRFilterOp.Type.HP12);
                break;
            case HP24:
                filter.setFilterType(IIRFilterOp.Type.HP24);
                break;
            case BP12:
                filter.setFilterType(IIRFilterOp.Type.BP12);
                break;
            case NP12:
                filter.setFilterType(IIRFilterOp.Type.NP12);
                break;
        }
        return this;
    }

    public Type type() {
        switch (filter.getFilterType()) {
            case LP6:
                return Type.LP6;
            case LP12:
                return Type.LP12;
            case LP24:
                return Type.LP24;
            case HP12:
                return Type.HP12;
            case HP24:
                return Type.HP24;
            case BP12:
                return Type.BP12;
            case NP12:
                return Type.NP12;
        }
        return LP6;
    }

    @Override
    public void reset() {
        resonance(0);
        frequency(20000);
        type(LP6);
    }

}
