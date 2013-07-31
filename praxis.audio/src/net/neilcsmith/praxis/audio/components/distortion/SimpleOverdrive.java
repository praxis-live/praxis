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
 *
 */

package net.neilcsmith.praxis.audio.components.distortion;

import net.neilcsmith.praxis.audio.impl.DefaultAudioInputPort;
import net.neilcsmith.praxis.audio.impl.DefaultAudioOutputPort;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.impl.NumberProperty;
import org.jaudiolibs.audioops.impl.ContainerOp;
import org.jaudiolibs.audioops.impl.gpl.OverdriveOp;
import org.jaudiolibs.pipes.impl.OpHolder;

/**
 *
 * @author Neil C Smith
 */
public class SimpleOverdrive extends AbstractComponent {

    private ContainerOp container;
    private OverdriveOp overdrive;
    private NumberProperty drive;
    private NumberProperty mix;

    public SimpleOverdrive() {
        overdrive = new OverdriveOp();
        container = new ContainerOp(overdrive);
        OpHolder cmp = new OpHolder(container);
        registerPort(Port.IN, new DefaultAudioInputPort(this, cmp));
        registerPort(Port.OUT, new DefaultAudioOutputPort(this, cmp));
        drive = NumberProperty.create( new DriveBinding(), 0, 1, 0);
        registerControl("drive", drive);
        registerPort("drive", drive.createPort());
        mix = NumberProperty.create( new MixBinding(), 0, 1, 0);
        registerControl("mix", mix);
        registerPort("mix", mix.createPort());
        
    }

    private class DriveBinding implements NumberProperty.Binding {

        public void setBoundValue(long time, double value) {
            overdrive.setDrive((float) value);
        }

        public double getBoundValue() {
            return overdrive.getDrive();
        }

    }

    private class MixBinding implements NumberProperty.Binding {

        public void setBoundValue(long time, double value) {
            container.setMix((float) value);
        }

        public double getBoundValue() {
            return container.getMix();
        }

    }
}
