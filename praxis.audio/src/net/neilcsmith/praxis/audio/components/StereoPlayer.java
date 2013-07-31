/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2013 Neil C Smith.
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
package net.neilcsmith.praxis.audio.components;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.audio.impl.DefaultAudioOutputPort;
import net.neilcsmith.praxis.audio.io.SampleTable;
import net.neilcsmith.praxis.core.ControlPort;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.core.types.PMap;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.impl.BooleanProperty;
import net.neilcsmith.praxis.impl.DefaultControlOutputPort;
import net.neilcsmith.praxis.impl.NumberProperty;
import net.neilcsmith.praxis.impl.FloatRangeProperty;
import net.neilcsmith.praxis.impl.TriggerControl;
import org.jaudiolibs.pipes.impl.MultiInOut;
import org.jaudiolibs.pipes.impl.Placeholder;

/**
 *
 * @author Neil C Smith
 */
public class StereoPlayer extends AbstractComponent {

    private static final Logger logger = Logger.getLogger(StereoPlayer.class.getName());
    private SamplePlayerUG[] players;
    private int tablesize;
    private ControlPort.Output readyPort;
    private ControlPort.Output errorPort;

    public StereoPlayer() {
        players = new SamplePlayerUG[2];
        players[0] = new SamplePlayerUG();
        players[1] = new SamplePlayerUG();    
        JointRenderUG join = new JointRenderUG(2,2);       
        join.addSource(players[0]);
        join.addSource(players[1]);     
        Placeholder outL = new Placeholder();
        Placeholder outR = new Placeholder();
        outL.addSource(join);
        outR.addSource(join);
        
        registerPort(Port.OUT + "-1", new DefaultAudioOutputPort(outL));
        registerPort(Port.OUT + "-2", new DefaultAudioOutputPort(outR));
        buildControls();
    }

    private void buildControls() {
        SampleTableLoader sample = new SampleTableLoader(new LoaderListener());
        registerControl("sample", sample);
        registerPort("sample", sample.createPort());
        NumberProperty position = NumberProperty.create(new PositionBinding(), 0, 1, 0,
                PMap.create(ControlInfo.KEY_TRANSIENT, true));
        registerControl("position", position);
        registerPort("position", position.createPort());
        NumberProperty in = NumberProperty.create(new InBinding(), 0, 1, 0);
        registerControl("start", in);
        registerPort("start", in.createPort());
        NumberProperty out = NumberProperty.create(new OutBinding(), 0, 1, 1);
        registerControl("end", out);
        registerPort("end", out.createPort());
        FloatRangeProperty range = FloatRangeProperty.create(new RangeBinding(),
                0, 1, 0, 1);
        registerControl("range", range);
        NumberProperty speed = NumberProperty.create(new SpeedBinding(), -4, 4, 1);
        registerControl("speed", speed);
        registerPort("speed", speed.createPort());
        registerControl("loop", BooleanProperty.create(new LoopingBinding(), false));
        TriggerControl play = TriggerControl.create(new PlayBinding());
        registerControl("play", play);
        registerPort("play", play.createPort());
        TriggerControl stop = TriggerControl.create(new StopBinding());
        registerControl("stop", stop);
        registerPort("stop", stop.createPort());
        BooleanProperty playing = BooleanProperty.create(new PlayingBinding(), false,
                PMap.create(ControlInfo.KEY_TRANSIENT, true));
        registerControl("playing", playing);
        readyPort = new DefaultControlOutputPort();
        registerPort("ready", readyPort);
        errorPort = new DefaultControlOutputPort();
        registerPort("error", errorPort);
    }

    private class PlayingBinding implements BooleanProperty.Binding {

        public void setBoundValue(long time, boolean value) {
            for (SamplePlayerUG player : players) {
                player.setPlaying(value);
            }       
        }

        public boolean getBoundValue() {
            return players[0].getPlaying();
        }
    }

    private class PlayBinding implements TriggerControl.Binding {

        public void trigger(long time) {
            for (SamplePlayerUG player : players) {
                player.setPlaying(true);
            } 
        }
    }

    private class StopBinding implements TriggerControl.Binding {

        public void trigger(long time) {
            for (SamplePlayerUG player : players) {
                player.setPlaying(false);
            } 
        }
    }

    private class SpeedBinding implements NumberProperty.Binding {

        public void setBoundValue(long time, double value) {
            for (SamplePlayerUG player : players) {
                player.setSpeed((float) value);
            }      
        }

        public double getBoundValue() {
            return players[0].getSpeed();
        }
    }

    private class PositionBinding implements NumberProperty.Binding {

        public void setBoundValue(long time, double value) {
            for (SamplePlayerUG player : players) {
                player.setPosition((float) (value * tablesize));
            }         
        }

        public double getBoundValue() {
            return players[0].getPosition() / tablesize;
        }
    }

    private class InBinding implements NumberProperty.Binding {

        public void setBoundValue(long time, double value) {
            int pos = (int) Math.round(value * tablesize);
            for (SamplePlayerUG player : players) {
                player.setIn(pos);
            }          
        }

        public double getBoundValue() {
            return (double) players[0].getIn() / tablesize;
        }
    }

    private class OutBinding implements NumberProperty.Binding {

        public void setBoundValue(long time, double value) {
            int pos = (int) Math.round(value * tablesize);
            for (SamplePlayerUG player : players) {
                player.setOut(pos);
            }            
        }

        public double getBoundValue() {
            return (double) players[0].getOut() / tablesize;
        }
    }

    private class RangeBinding implements FloatRangeProperty.Binding {

        public void setBoundLowValue(long time, double low) {
            int pos = (int) Math.round(low * tablesize);
            for (SamplePlayerUG player : players) {
                player.setIn(pos);
            }    
        }

        public void setBoundHighValue(long time, double high) {
            int pos = (int) Math.round(high * tablesize);
            for (SamplePlayerUG player : players) {
                player.setOut(pos);
            }     
        }

        public double getBoundLowValue() {
            double ret = (double) players[0].getIn() / tablesize;
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Returning Low = " + ret);
            }
            return ret;
        }

        public double getBoundHighValue() {
            double ret = (double) players[0].getOut() / tablesize;
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Returning High = " + ret);
            }
            return ret;
        }
    }

    private class LoopingBinding implements BooleanProperty.Binding {

        public void setBoundValue(long time, boolean value) {
            for (SamplePlayerUG player : players) {
                player.setLooping(value);
            }     
        }

        public boolean getBoundValue() {
            return players[0].getLooping();
        }
    }

    private class LoaderListener implements SampleTableLoader.Listener {

        public void tableLoaded(SampleTableLoader loader, long time) {
            SampleTable table = loader.getTable();
            if (table == null) {
                tablesize = 1;
            } else {
                tablesize = table.getSize();
            }
            for (int i=0; i<players.length; i++) {
                players[i].setSampleTable(table);
                if (i >= table.getChannelCount()) {
                    players[i].setChannel(0);
                } else {
                    players[i].setChannel(i);
                }
            }
            readyPort.send(time);
        }

        public void tableError(SampleTableLoader loader, long time) {
            errorPort.send(time);
        }
    }
    
    private class JointRenderUG extends MultiInOut {
        
        private JointRenderUG(int in, int out) {
            super(in, out);
        }
        
    }
}
