/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.neilcsmith.praxis.audio.components.reverb;

import net.neilcsmith.praxis.audio.impl.DefaultAudioInputPort;
import net.neilcsmith.praxis.audio.impl.DefaultAudioOutputPort;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.impl.FloatProperty;
import org.jaudiolibs.audioops.impl.FreeverbOp;
import org.jaudiolibs.pipes.impl.MultiChannelOpHolder;
import org.jaudiolibs.pipes.impl.Placeholder;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class Freeverb extends AbstractComponent {

    private static enum BindingTarget {

        RoomSize, Damp, Width, Wet, Dry
    };
    private final static double INITIAL_WET = 0;
    private final static double INITIAL_WIDTH = 0.5;
    private final static double INITIAL_ROOM_SIZE = 0.5;
    private final static double INITIAL_DAMP = 0.5;
    private final static double INITIAL_DRY = 0.5;
    private FreeverbOp op;

    public Freeverb() {

        initAudio();
        initControl();
    }

    private void initAudio() {
        try {
            op = new FreeverbOp();
            op.setWet((float) INITIAL_WET);
            op.setWidth((float) INITIAL_WIDTH);
            op.setRoomSize((float) INITIAL_ROOM_SIZE);
            op.setDamp((float) INITIAL_DAMP); 
            op.setDry((float) INITIAL_DRY);
            MultiChannelOpHolder holder = new MultiChannelOpHolder(op, 2);
            Placeholder in1 = new Placeholder();
            Placeholder in2 = new Placeholder();
            Placeholder out1 = new Placeholder();
            Placeholder out2 = new Placeholder();
            holder.addSource(in1);
            holder.addSource(in2);
            out1.addSource(holder);
            out2.addSource(holder);
            registerPort(Port.IN + "-1", new DefaultAudioInputPort(this, in1));
            registerPort(Port.IN + "-2", new DefaultAudioInputPort(this, in2));
            registerPort(Port.OUT + "-1", new DefaultAudioOutputPort(this, out1));
            registerPort(Port.OUT + "-2", new DefaultAudioOutputPort(this, out2));
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private void initControl() {
        FloatProperty roomSize = createProperty(BindingTarget.RoomSize, INITIAL_ROOM_SIZE);
        registerControl("room-size", roomSize);
        registerPort("room-size", roomSize.createPort());
        FloatProperty damp = createProperty(BindingTarget.Damp, INITIAL_DAMP);
        registerControl("damp", damp);
        registerPort("damp", damp.createPort());
        FloatProperty width = createProperty(BindingTarget.Width, INITIAL_WIDTH);
        registerControl("width", width);
        registerPort("width", width.createPort());
        FloatProperty wet = createProperty(BindingTarget.Wet, INITIAL_WET);
        registerControl("wet", wet);
        registerPort("wet", wet.createPort());
        FloatProperty dry = createProperty(BindingTarget.Dry, INITIAL_DRY);
        registerControl("dry", dry);
        registerPort("dry", dry.createPort());
    }
    
    private FloatProperty createProperty(BindingTarget target, double def) {
        return FloatProperty.create(new Binding(target, def), 0, 1, def);
    }

    private class Binding implements FloatProperty.Binding {

        private BindingTarget target;
        private double value;

        private Binding(BindingTarget target, double def) {
            this.target = target;
            this.value = def;
        }

        public void setBoundValue(long time, double value) {
            switch (target) {
                case RoomSize:
                    op.setRoomSize((float) value);
                    break;
                case Damp:
                    op.setDamp((float) value);
                    break;
                case Width:
                    op.setWidth((float) value);
                    break;
                case Wet:
                    op.setWet((float) value);
                    break;
                case Dry:
                    op.setDry((float) value);
                    break;
            }
        }

        public double getBoundValue() {
            switch (target) {
                case RoomSize:
                    return op.getRoomSize();
                case Damp:
                    return op.getDamp();
                case Width:
                    return op.getWidth();
                case Wet:
                    return op.getWet();
                case Dry:
                    return op.getDry();
            }
            throw new UnsupportedOperationException();
        }
    }
}
