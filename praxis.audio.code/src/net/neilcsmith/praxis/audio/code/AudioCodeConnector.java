/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2018 Neil C Smith.
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
package net.neilcsmith.praxis.audio.code;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import net.neilcsmith.praxis.audio.code.userapi.AudioIn;
import net.neilcsmith.praxis.audio.code.userapi.AudioOut;
import net.neilcsmith.praxis.audio.code.userapi.AudioTable;
import net.neilcsmith.praxis.audio.code.userapi.Table;
import net.neilcsmith.praxis.audio.code.userapi.UGen;
import net.neilcsmith.praxis.code.CodeConnector;
import net.neilcsmith.praxis.code.CodeFactory;
import net.neilcsmith.praxis.code.ResourceProperty;
import net.neilcsmith.praxis.code.userapi.In;
import net.neilcsmith.praxis.code.userapi.Out;
import net.neilcsmith.praxis.code.userapi.P;
import org.jaudiolibs.pipes.Pipe;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class AudioCodeConnector<D extends AudioCodeDelegate> extends CodeConnector<D> {

    private final Class<? extends AudioCodeDelegate> previousClass;
    private final List<AudioInPort.Descriptor> ins;
    private final List<AudioOutPort.Descriptor> outs;
    private final List<UGenDescriptor> ugens;
    
    public AudioCodeConnector(CodeFactory.Task<D> task,
            D delegate,
            Class<? extends AudioCodeDelegate> previousClass) {
        super(task, delegate);
        this.previousClass = previousClass;
        ins = new ArrayList<>();
        outs = new ArrayList<>();
        ugens = new ArrayList<>();
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void analyseField(Field field) {
        
        if (AudioIn.class.isAssignableFrom(field.getType())) {
            In in = field.getAnnotation(In.class);
            if (in != null) {
                AudioInPort.Descriptor aid =
                        AudioInPort.createDescriptor(this, in, field);
                if (aid != null) {
                    addPort(aid);
                    ins.add(aid);
                    return;
                }
            }
        }
        
        if (AudioOut.class.isAssignableFrom(field.getType())) {
            Out out = field.getAnnotation(Out.class);
            if (out != null) {
                AudioOutPort.Descriptor aod =
                        AudioOutPort.createDescriptor(this, out, field);
                if (aod != null) {
                    addPort(aod);
                    outs.add(aod);
                    return;
                }
            }
        }
        
        if (field.isAnnotationPresent(UGen.class) &&
                Pipe.class.isAssignableFrom(field.getType())) {
            UGenDescriptor ugd = UGenDescriptor.create(this, field);
            if (ugd != null) {
                ugens.add(ugd);
                return;
            }
        }
        
        if (Table.class.isAssignableFrom(field.getType())) {
            P p = field.getAnnotation(P.class);
            if (p != null) {
                ResourceProperty.Descriptor<AudioTable> ipd =
                        ResourceProperty.Descriptor.create(this, p, field, TableLoader.getDefault());
                if (ipd != null) {
                    addControl(ipd);
                    if (shouldAddPort(field)) {
                        addPort(ipd.createPortDescriptor());
                    }
                    return;
                }
            }
        }
        
        super.analyseField(field);
    }
    
    UGenDescriptor[] extractUGens() {
        return ugens.toArray(new UGenDescriptor[ugens.size()]);
    }
    
    AudioInPort.Descriptor[] extractIns() {
        return ins.toArray(new AudioInPort.Descriptor[ins.size()]);
    }
    
    AudioOutPort.Descriptor[] extractOuts() {
        return outs.toArray(new AudioOutPort.Descriptor[outs.size()]);
    }
    
    Class<? extends AudioCodeDelegate> getPreviousClass() {
        return previousClass;
    }
    
}
