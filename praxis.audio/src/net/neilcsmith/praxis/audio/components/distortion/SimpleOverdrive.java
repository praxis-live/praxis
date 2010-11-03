/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Neil C Smith. All rights reserved.
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
 *
 */

package net.neilcsmith.praxis.audio.components.distortion;

import net.neilcsmith.praxis.audio.DefaultAudioInputPort;
import net.neilcsmith.praxis.audio.DefaultAudioOutputPort;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.impl.FloatProperty;
import net.neilcsmith.rapl.components.distortion.Overdrive;

/**
 *
 * @author Neil C Smith
 */
public class SimpleOverdrive extends AbstractComponent {

    private Overdrive overdrive;
    private FloatProperty drive;
    private FloatProperty mix;

    public SimpleOverdrive() {
        overdrive = new Overdrive();
        drive = FloatProperty.create( new DriveBinding(), 0);
        registerControl("drive", drive);
        registerPort("drive", drive.createPort());
        mix = FloatProperty.create( new MixBinding(), 0);
        registerControl("mix", mix);
        registerPort("mix", mix.createPort());
        registerPort(Port.IN, new DefaultAudioInputPort(this, overdrive));
        registerPort(Port.OUT, new DefaultAudioOutputPort(this, overdrive));

    }

    private class DriveBinding implements FloatProperty.Binding {

        public void setBoundValue(long time, double value) {
            overdrive.setDrive((float) value);
        }

        public double getBoundValue() {
            return overdrive.getDrive();
        }

    }

    private class MixBinding implements FloatProperty.Binding {

        public void setBoundValue(long time, double value) {
            overdrive.setMix((float) value);
        }

        public double getBoundValue() {
            return overdrive.getMix();
        }

    }
}
