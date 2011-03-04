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
 *
 * Parts of the API of this package, as well as some of the code, is derived from
 * the Processing project (http://processing.org)
 *
 * Copyright (c) 2004-09 Ben Fry and Casey Reas
 * Copyright (c) 2001-04 Massachusetts Institute of Technology
 *
 */
package net.neilcsmith.praxis.java;

import java.util.Random;
import net.neilcsmith.praxis.core.Argument;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class CodeDelegate {

    private CodeContext context;
    private boolean installable;
    private Random rnd;

    public CodeDelegate() {
        this(true);
    }

    public CodeDelegate(boolean installable) {
        rnd = new Random();
    }

    public void init(CodeContext context, long time) throws Exception {
        if (context == null) {
            throw new NullPointerException();
        }
        this.context = context;
        if (installable) {
            // add param listeners
        }
    }

    public void tick() {
    }

    public void dispose() {
    }

    public Param p(int idx) {
        return context.getParam(idx - 1);
    }

    public double d(int idx) {
        return context.getParam(idx - 1).getDouble(0);
    }

    public int i(int idx) {
        return context.getParam(idx - 1).getInt(0);
    }

    public boolean t(int idx) {
        return context.getTrigger(idx - 1).getAndReset();
    }

    public void send(int idx, Argument arg) {
        context.getOutput(idx - 1).send(arg);
    }

    public void send(int idx, double value) {
        context.getOutput(idx - 1).send(value);
    }

    public long getTime() {
        return context.getTime();
    }

    public final double random(double max) {
        return rnd.nextDouble() * max;
    }

    public final double random(double min, double max) {
        if (min >= max) {
            return min;
        }
        return random(max - min) + min;
    }

    public final double abs(double n) {
        return (n < 0) ? -n : n;
    }

    public final double sq(double a) {
        return a * a;
    }

    public final double sqrt(double a) {
        return Math.sqrt(a);
    }

    public final double log(double a) {
        return Math.log(a);
    }

    public final double exp(double a) {
        return Math.exp(a);
    }

    public final double pow(double a, double b) {
        return Math.pow(a, b);
    }

    public final double max(double a, double b) {
        return (a > b) ? a : b;
    }

    public final double max(double a, double b, double c) {
        return (a > b) ? ((a > c) ? a : c) : ((b > c) ? b : c);
    }

  public final int constrain(int amt, int low, int high) {
    return (amt < low) ? low : ((amt > high) ? high : amt);
  }

  public final double constrain(double amt, double low, double high) {
    return (amt < low) ? low : ((amt > high) ? high : amt);
  }


   public final double sin(double angle) {
    return Math.sin(angle);
  }

   public final double cos(double angle) {
    return Math.cos(angle);
  }

   public final double tan(double angle) {
    return Math.tan(angle);
  }


   public final double asin(double value) {
    return Math.asin(value);
  }

   public final double acos(double value) {
    return Math.acos(value);
  }

   public final double atan(double value) {
    return Math.atan(value);
  }

   public final double atan2(double a, double b) {
    return Math.atan2(a, b);
  }


    // PERLIN NOISE - copied from Processing core.
    // @TODO fully convert to double???
    private static final int PERLIN_YWRAPB = 4;
    private static final int PERLIN_YWRAP = 1 << PERLIN_YWRAPB;
    private static final int PERLIN_ZWRAPB = 8;
    private static final int PERLIN_ZWRAP = 1 << PERLIN_ZWRAPB;
    private static final int PERLIN_SIZE = 4095;
//  private static final float sinLUT[];
    private static final float cosLUT[];
    private static final float SINCOS_PRECISION = 0.5f;
    private static final int SINCOS_LENGTH = (int) (360f / SINCOS_PRECISION);

    static {
//    sinLUT = new float[SINCOS_LENGTH];
        cosLUT = new float[SINCOS_LENGTH];
        for (int i = 0; i < SINCOS_LENGTH; i++) {
//      sinLUT[i] = (float) Math.sin(i * Constants.DEG_TO_RAD * SINCOS_PRECISION);
            cosLUT[i] = (float) Math.cos(i * Constants.DEG_TO_RAD * SINCOS_PRECISION);
        }
    }
    private int perlin_octaves = 4; // default to medium smooth
    private float perlin_amp_falloff = 0.5f; // 50% reduction/octave
    private int perlin_TWOPI, perlin_PI;
    private float[] perlin_cosTable;
    private float[] perlin;
    private Random perlinRandom;

    /**
     * Computes the Perlin noise function value at point x.
     */
    public double noise(double x) {
        return noise(x, 0f, 0f);
    }

    /**
     * Computes the Perlin noise function value at the point x, y.
     */
    public double noise(double x, double y) {
        return noise(x, y, 0f);
    }

    /**
     * Computes the Perlin noise function value at x, y, z.
     */
    public double noise(double x, double y, double z) {
        if (perlin == null) {
            if (perlinRandom == null) {
                perlinRandom = new Random();
            }
            perlin = new float[PERLIN_SIZE + 1];
            for (int i = 0; i < PERLIN_SIZE + 1; i++) {
                perlin[i] = perlinRandom.nextFloat();
            }
            perlin_cosTable = cosLUT;
            perlin_TWOPI = perlin_PI = SINCOS_LENGTH;
            perlin_PI >>= 1;
        }

        if (x < 0) {
            x = -x;
        }
        if (y < 0) {
            y = -y;
        }
        if (z < 0) {
            z = -z;
        }

        int xi = (int) x, yi = (int) y, zi = (int) z;
        float xf = (float) (x - xi);
        float yf = (float) (y - yi);
        float zf = (float) (z - zi);
        float rxf, ryf;

        float r = 0;
        float ampl = 0.5f;

        float n1, n2, n3;

        for (int i = 0; i < perlin_octaves; i++) {
            int of = xi + (yi << PERLIN_YWRAPB) + (zi << PERLIN_ZWRAPB);

            rxf = noise_fsc(xf);
            ryf = noise_fsc(yf);

            n1 = perlin[of & PERLIN_SIZE];
            n1 += rxf * (perlin[(of + 1) & PERLIN_SIZE] - n1);
            n2 = perlin[(of + PERLIN_YWRAP) & PERLIN_SIZE];
            n2 += rxf * (perlin[(of + PERLIN_YWRAP + 1) & PERLIN_SIZE] - n2);
            n1 += ryf * (n2 - n1);

            of += PERLIN_ZWRAP;
            n2 = perlin[of & PERLIN_SIZE];
            n2 += rxf * (perlin[(of + 1) & PERLIN_SIZE] - n2);
            n3 = perlin[(of + PERLIN_YWRAP) & PERLIN_SIZE];
            n3 += rxf * (perlin[(of + PERLIN_YWRAP + 1) & PERLIN_SIZE] - n3);
            n2 += ryf * (n3 - n2);

            n1 += noise_fsc(zf) * (n2 - n1);

            r += n1 * ampl;
            ampl *= perlin_amp_falloff;
            xi <<= 1;
            xf *= 2;
            yi <<= 1;
            yf *= 2;
            zi <<= 1;
            zf *= 2;

            if (xf >= 1.0f) {
                xi++;
                xf--;
            }
            if (yf >= 1.0f) {
                yi++;
                yf--;
            }
            if (zf >= 1.0f) {
                zi++;
                zf--;
            }
        }
        return r;
    }

  
    private float noise_fsc(float i) {
        return 0.5f * (1.0f - perlin_cosTable[(int) (i * perlin_PI) % perlin_TWOPI]);
    }

    // make perlin noise quality user controlled to allow
    // for different levels of detail. lower values will produce
    // smoother results as higher octaves are surpressed
    public void noiseDetail(int lod) {
        if (lod > 0) {
            perlin_octaves = lod;
        }
    }

    public void noiseDetail(int lod, double falloff) {
        if (lod > 0) {
            perlin_octaves = lod;
        }
        if (falloff > 0) {
            perlin_amp_falloff = (float) falloff;
        }
    }

    public void noiseSeed(long what) {
        if (perlinRandom == null) {
            perlinRandom = new Random();
        }
        perlinRandom.setSeed(what);
        perlin = null;
    }
    // end of Perlin noise functions
}
