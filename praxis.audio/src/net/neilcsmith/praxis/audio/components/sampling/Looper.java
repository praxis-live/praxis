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

package net.neilcsmith.praxis.audio.components.sampling;

import net.neilcsmith.praxis.audio.components.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.audio.impl.DefaultAudioInputPort;
import net.neilcsmith.praxis.audio.impl.DefaultAudioOutputPort;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.core.types.PMap;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.impl.BooleanProperty;
import net.neilcsmith.praxis.impl.FloatProperty;
import net.neilcsmith.praxis.impl.FloatRangeProperty;
import net.neilcsmith.praxis.impl.TriggerControl;
import net.neilcsmith.rapl.util.SampleTable;

/**
 *
 * @author Neil C Smith
 */
public class Looper extends AbstractComponent {

    private static Logger logger = Logger.getLogger(Looper.class.getName());

    private net.neilcsmith.rapl.components.sampling.Looper looper;    
    
    public Looper() {
        looper = new net.neilcsmith.rapl.components.sampling.Looper();
        looper.setLoopSize(1);    
        registerPort(Port.IN, new DefaultAudioInputPort(this, looper));
        registerPort(Port.OUT, new DefaultAudioOutputPort(this, looper));
        buildControls();
    }
    
    private void buildControls() {
        FloatProperty loopSize = FloatProperty.create(new LoopSizeBinding(), 0, 60, 1);
        registerControl("loop-size", loopSize);
        FloatProperty position = FloatProperty.create(new PositionBinding(), 0, 1, 0,
                PMap.create(ControlInfo.KEY_TRANSIENT, true));
        registerControl("position", position);
        registerPort("position", position.createPort());
        FloatProperty in = FloatProperty.create( new InBinding(), 0, 1, 0);
        registerControl("start", in);
        registerPort("start", in.createPort());
        FloatProperty out = FloatProperty.create( new OutBinding(), 0, 1, 1);
        registerControl("end", out);
        registerPort("end", out.createPort());
        FloatRangeProperty range = FloatRangeProperty.create( new RangeBinding(),
                0, 1, 0, 1);
        registerControl("range", range);
        FloatProperty speed = FloatProperty.create( new SpeedBinding(), -4, 4, 1);
        registerControl("speed", speed);
        registerPort("speed", speed.createPort());
//        registerControl("loop", BooleanProperty.create(this, new LoopingBinding(), false));
        TriggerControl play = TriggerControl.create( new PlayBinding());
        registerControl("play", play);
        registerPort("play", play.createPort());
        TriggerControl stop = TriggerControl.create( new StopBinding());
        registerControl("stop", stop);
        registerPort("stop", stop.createPort());
        TriggerControl record = TriggerControl.create( new RecordBinding());
        registerControl("record", record);
        registerPort("record", record.createPort());
        BooleanProperty playing = BooleanProperty.create(this, new PlayingBinding(), false,
                PMap.create(ControlInfo.KEY_TRANSIENT, true));
        registerControl("playing", playing);
        BooleanProperty recording = BooleanProperty.create(this, new RecordingBinding(), false,
                PMap.create(ControlInfo.KEY_TRANSIENT, true));
        registerControl("recording", recording);
    }

    private class PlayingBinding implements BooleanProperty.Binding {

        public void setBoundValue(long time, boolean value) {
            looper.setPlaying(value);
        }

        public boolean getBoundValue() {
            return looper.getPlaying();
        }
        
    }

    
    
    private class PlayBinding implements TriggerControl.Binding {

        public void trigger(long time) {
            looper.setPlaying(true);
            looper.setRecording(false);
        }
        
    }
    
    private class StopBinding implements TriggerControl.Binding {

        public void trigger(long time) {
            looper.setPlaying(false);
            looper.setRecording(false);
        }
        
    }
    
    private class RecordBinding implements TriggerControl.Binding {

        public void trigger(long time) {
            looper.setRecording(true);
        }
        
    }
    
    private class RecordingBinding implements BooleanProperty.Binding {

        public void setBoundValue(long time, boolean value) {
            looper.setRecording(value);
        }

        public boolean getBoundValue() {
            return looper.getRecording();
        }

    }
    
    private class SpeedBinding implements FloatProperty.Binding {

        public void setBoundValue(long time, double value) {
            looper.setSpeed((float) value);
        }

        public double getBoundValue() {
            return looper.getSpeed();
        }
        
    }
    
    private class PositionBinding implements FloatProperty.Binding {

        public void setBoundValue(long time, double value) {
            looper.setPosition((float) (value * looper.getTableSize()));
        }

        public double getBoundValue() {
            return looper.getPosition() / looper.getTableSize();
        }
        
    }
    
    private class InBinding implements FloatProperty.Binding {

        public void setBoundValue(long time, double value) {
            int pos = (int) Math.round(value * looper.getTableSize());
            looper.setIn(pos);
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Input : " + value + " Player Input = " + looper.getIn()
                        + " Player Output = " + looper.getOut());
            }
        }

        public double getBoundValue() {
            return (double) looper.getIn() / looper.getTableSize();
        }
        
    }
    
    private class OutBinding implements FloatProperty.Binding {

        public void setBoundValue(long time, double value) {
            int pos = (int) Math.round(value * looper.getTableSize());
            looper.setOut(pos);
        }

        public double getBoundValue() {
            return (double) looper.getOut() / looper.getTableSize();
        }
        
    }

    private class RangeBinding implements FloatRangeProperty.Binding {

        public void setBoundLowValue(long time, double low) {
            int pos = (int) Math.round(low * looper.getTableSize());
            looper.setIn(pos);
        }

        public void setBoundHighValue(long time, double high) {
            int pos = (int) Math.round(high * looper.getTableSize());
            looper.setOut(pos);
        }

        public double getBoundLowValue() {
            double ret =  (double) looper.getIn() / looper.getTableSize();
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Returning Low = " + ret);
            }
            return ret;
        }

        public double getBoundHighValue() {
            double ret = (double) looper.getOut() / looper.getTableSize();
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Returning High = " + ret);
            }
            return ret;
        }

    }
    
//    private class LoopingBinding implements BooleanProperty.Binding {
//
//        public void setBoundValue(long time, boolean value) {
//            looper.setLooping(value);
//        }
//
//        public boolean getBoundValue() {
//            return looper.getLooping();
//        }
//        
//    }
    
    private class LoopSizeBinding implements FloatProperty.Binding {

        public void setBoundValue(long time, double value) {
            looper.setLoopSize((float) value);
        }

        public double getBoundValue() {
            return looper.getLoopSize();
        }
        
    }
    
}
