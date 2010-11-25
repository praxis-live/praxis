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

package net.neilcsmith.rapl.ops;

import net.neilcsmith.rapl.core.Buffer;

/**
 *
 * @author Neil C Smith
 */
public class OverdriveOp implements AudioOpOld {
    
    private float drive;
    private int intAmount = 1;
    
    public void setDrive(float amount) {
        if (amount < 0) {
            throw new IllegalArgumentException();
        }
        this.drive = amount;
        this.intAmount = (int) (amount * 49) + 1;
    }
    
    public float getDrive() {
        return drive;
    }

    public void processReplace(Buffer bufferIn, Buffer bufferOut) {
        int len = bufferOut.getSize();
        int amt = this.intAmount;
        float[] in = bufferIn.getData();
        float[] out = bufferOut.getData();
        for (int i=0; i < len; i++) {
            out[i] = process(in[i], amt);
        }
    }

    public void processAdd(Buffer bufferIn, Buffer bufferOut) {
        int len = bufferOut.getSize();
        int amt = this.intAmount;
        float[] in = bufferIn.getData();
        float[] out = bufferOut.getData();
        for (int i=0; i < len; i++) {
            out[i] += process(in[i], amt);
        }
    }

    public void reset() {
        // noop
    }
    
    /*
 * Created on Jan 16, 2005
 *
 * Copyright (c) 2005 Peter Johan Salomonsen (http://www.petersalomonsen.com)
 * 
 * http://www.frinika.com
 * 
 * This file is part of Frinika.
 * 
 * Frinika is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.

 * Frinika is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Frinika; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */


/**
 * @author Peter Johan Salomonsen
 *
 */

    static final float divisor[];
    
    static
    {
        divisor = new float[128];
        for(int n = 0;n < divisor.length;n++)
        {
            divisor[n] = (float) (Math.log(n * 2) / Math.log(2));
        }
    }

    static final float atan[];
    static
    {
        atan = new float[65536];
        {
            for(float n=-10f;n<10f;n+=20f/65536f) {
                atan[(int) ((n*3276.8)+32768)] = (float) Math.atan(n);
            }
        }
    }
    
    static final float process(float signal, int amount)
    {
        signal*=amount;
        if(signal>=10) {
            signal = 10 - (20 / 65536);
        }
        else if(signal<-10) {
            signal = -10;
        }
     
        return atan[(int)((signal*3276.7)+32768)] / divisor[amount];      
    }
// 
//    public static void main(String[] args)
//    {
//        long start = System.currentTimeMillis();
//        for(float n=-10f;n<10f;n+=20f/65536f) {
//            process(n, 20);
//        }
//        System.out.println((System.currentTimeMillis()-start));
//    }


}
