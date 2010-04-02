package net.neilcsmith.rapl.components.test;


import net.neilcsmith.rapl.core.Buffer;
import net.neilcsmith.rapl.core.impl.AbstractOut;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Neil C Smith
 */
public class Sine extends AbstractOut {

    
    private final static float TWOPI = (float) (2*Math.PI);
    
    private float phase = 0;
    private float volume = 0.6f;
    private float freq = 440;
    private float srate = 0;
    
    public Sine() {
        this(440);
    }
    
    public Sine(float freq) {
        super(1);
        this.freq = freq;
    }
    
    public void setFrequency(float frequency) {
        this.freq = frequency;
    }
    
    public float getFrequency() {
        return this.freq;
    }
    
    @Override
    protected void process(Buffer buffer, boolean rendering) {
       if (srate != buffer.getSampleRate()) {
           srate = buffer.getSampleRate();
           phase = 0;
       }
       float[] out = buffer.getData();
       int bufsz = buffer.getSize();
       for (int i=0; i < bufsz; i++) {
           phase += TWOPI * freq / srate;
//           buffer.set(i, (float) (volume * Math.sin(phase)));
           out[i] = volume * (float) Math.sin(phase);
       }
       while (phase > TWOPI) {
           phase -= TWOPI;
       }
    }

}
