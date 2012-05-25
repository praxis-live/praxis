/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Neil C Smith.
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
 * along with this work; if not, see http://www.gnu.org/licenses/
 * 
 *
 * Linking this work statically or dynamically with other modules is making a
 * combined work based on this work. Thus, the terms and conditions of the GNU
 * General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this work give you permission
 * to link this work with independent modules to produce an executable,
 * regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that
 * you also meet, for each linked independent module, the terms and conditions of
 * the license of that module. An independent module is a module which is not
 * derived from or based on this work. If you modify this work, you may extend
 * this exception to your version of the work, but you are not obligated to do so.
 * If you do not wish to do so, delete this exception statement from your version.
 *
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 * 
 * 
 * Derived from code in Gervill / OpenJDK
 * 
 * Copyright 2007 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 *
 * 
 * 
 */
package org.jaudiolibs.audioops.impl;

import org.jaudiolibs.audioops.AudioOp;

/**
 *
 * @author Neil C Smith
 */
public class ChorusOp implements AudioOp {

    private final LFODelayOp lfoDelay;
    private float depth;

    public ChorusOp() {
        lfoDelay = new LFODelayOp();
        lfoDelay.setRange(0.5f);
    }

    public void setDepth(float depth) {
        lfoDelay.setDelay(depth / 1000);
        this.depth = depth;
    }

    public float getDepth() {
        return depth;
    }

    public void setRate(float rate) {
        lfoDelay.setRate(rate);
    }

    public float getRate() {
        return lfoDelay.getRate();
    }

    public void setFeedback(float feedback) {
        lfoDelay.setFeedback(feedback);
    }

    public float getFeedback() {
        return lfoDelay.getFeedback();
    }

    public void setPhase(float phase) {
        lfoDelay.setPhase(phase);
    }

    public float getPhase() {
        return lfoDelay.getPhase();
    }

    public void reset(int skipped) {
        lfoDelay.reset(skipped);
    }

    public void processReplace(int buffersize, float[][] outputs, float[][] inputs) {
        lfoDelay.processReplace(buffersize, outputs, inputs);
    }

    public void processAdd(int buffersize, float[][] outputs, float[][] inputs) {
        lfoDelay.processAdd(buffersize, outputs, inputs);
    }

    public boolean isInputRequired(boolean outputRequired) {
        return lfoDelay.isInputRequired(outputRequired);
    }

    public void initialize(float samplerate, int maxBufferSize) {
        lfoDelay.initialize(samplerate, maxBufferSize);
    }
}
