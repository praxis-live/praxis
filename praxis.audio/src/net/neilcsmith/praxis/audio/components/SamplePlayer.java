/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008/09 - Neil C Smith. All rights reserved.
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

package net.neilcsmith.praxis.audio.components;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.audio.DefaultAudioInputPort;
import net.neilcsmith.praxis.audio.DefaultAudioOutputPort;
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
public class SamplePlayer extends AbstractComponent {

    private static Logger logger = Logger.getLogger(SamplePlayer.class.getName());

    private net.neilcsmith.rapl.components.SamplePlayer player;
//    private BooleanProperty playing;
    private int tablesize;
    
    
    public SamplePlayer() {
        player = new net.neilcsmith.rapl.components.SamplePlayer();
        
        buildControls();
        registerPort("input", new DefaultAudioInputPort(this, player));
        registerPort("output", new DefaultAudioOutputPort(this, player));
    }
    
    private void buildControls() {
        registerControl("table", new SampleTableLoader(this, new LoaderListener()));
        FloatProperty position = FloatProperty.create(this, new PositionBinding(), 0, 1, 0);
        registerControl("position", position);
        registerPort("position", position.createPort());
        FloatProperty in = FloatProperty.create(this, new InBinding(), 0, 1, 0);
        registerControl("in", in);
        registerPort("in", in.createPort());
        FloatProperty out = FloatProperty.create(this, new OutBinding(), 0, 1, 1);
        registerControl("out", out);
        registerPort("out", out.createPort());
        FloatRangeProperty range = FloatRangeProperty.create(this, new RangeBinding(),
                0, 1, 0, 1);
        registerControl("range", range);
        FloatProperty speed = FloatProperty.create(this, new SpeedBinding(), -2048, 2048, 1);
        registerControl("speed", speed);
        registerPort("speed", speed.createPort());
        registerControl("loop", BooleanProperty.create(this, new LoopingBinding(), false));
        TriggerControl play = TriggerControl.create(this, new PlayBinding());
        registerControl("play", play);
        registerPort("play", play.getPort());
        TriggerControl stop = TriggerControl.create(this, new StopBinding());
        registerControl("stop", stop);
        registerPort("stop", stop.getPort());
        BooleanProperty playing = BooleanProperty.create(this, new PlayingBinding(), false);
        registerControl("playing", playing);
        registerPort("playing", playing.createPort());
        BooleanProperty record = BooleanProperty.create(this, new RecordingBinding(), false);
        registerControl("record", record);
        registerPort("record", record.createPort());
    }

    private class PlayingBinding implements BooleanProperty.Binding {

        public void setBoundValue(long time, boolean value) {
            player.setPlaying(value);
        }

        public boolean getBoundValue() {
            return player.getPlaying();
        }
        
    }

    private class RecordingBinding implements BooleanProperty.Binding {

        public void setBoundValue(long time, boolean value) {
            player.setRecording(value);
        }

        public boolean getBoundValue() {
            return player.getRecording();
        }

    }
    
    private class PlayBinding implements TriggerControl.Binding {

        public void trigger(long time) {
            player.setPlaying(true);
        }
        
    }
    
    private class StopBinding implements TriggerControl.Binding {

        public void trigger(long time) {
            player.setPlaying(false);
        }
        
    }
    
    private class SpeedBinding implements FloatProperty.Binding {

        public void setBoundValue(long time, double value) {
            player.setSpeed((float) value);
        }

        public double getBoundValue() {
            return player.getSpeed();
        }
        
    }
    
    private class PositionBinding implements FloatProperty.Binding {

        public void setBoundValue(long time, double value) {
            player.setPosition((float) (value * tablesize));
        }

        public double getBoundValue() {
            return player.getPosition() / tablesize;
        }
        
    }
    
    private class InBinding implements FloatProperty.Binding {

        public void setBoundValue(long time, double value) {
            int pos = (int) Math.round(value * tablesize);
            player.setIn(pos);
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Input : " + value + " Player Input = " + player.getIn()
                        + " Player Output = " + player.getOut());
            }
        }

        public double getBoundValue() {
            return (double) player.getIn() / tablesize;
        }
        
    }
    
    private class OutBinding implements FloatProperty.Binding {

        public void setBoundValue(long time, double value) {
            int pos = (int) Math.round(value * tablesize);
            player.setOut(pos);
        }

        public double getBoundValue() {
            return (double) player.getOut() / tablesize;
        }
        
    }

    private class RangeBinding implements FloatRangeProperty.Binding {

        public void setBoundLowValue(long time, double low) {
            int pos = (int) Math.round(low * tablesize);
            player.setIn(pos);
        }

        public void setBoundHighValue(long time, double high) {
            int pos = (int) Math.round(high * tablesize);
            player.setOut(pos);
        }

        public double getBoundLowValue() {
            double ret =  (double) player.getIn() / tablesize;
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Returning Low = " + ret);
            }
            return ret;
        }

        public double getBoundHighValue() {
            double ret = (double) player.getOut() / tablesize;
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Returning High = " + ret);
            }
            return ret;
        }

    }
    
    private class LoopingBinding implements BooleanProperty.Binding {

        public void setBoundValue(long time, boolean value) {
            player.setLooping(value);
        }

        public boolean getBoundValue() {
            return player.getLooping();
        }
        
    }
    
    private class LoaderListener implements SampleTableLoader.Listener {

        public void tableLoaded(SampleTableLoader loader) {
            SampleTable table = loader.getTable();
            if (table == null) {
                tablesize = 1;
            } else {
                tablesize = table.getSize();
            }
            player.setSampleTable(table);
        }

        public void tableError(SampleTableLoader loader) {
            
        }
        
    }
}
