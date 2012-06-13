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
 * 
 * Ported from http://code.google.com/p/libtimefilter/
 * 
 * 
 * libtimefilter - A library for accurate time stamping
 * author: Olivier Guilyardi <olivier samalyse com>
 *
 * Copyright (c) 2009, Samalyse SARL - All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * * Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * 
 * * Neither the name of Samalyse SARL nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.jaudiolibs.audioservers.javasound;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
class TimeFilter {

    // Delay Locked Loop
    double tper;
    double b;
    double c;
    double e2;
    double t0;
    double t1;
    // Stats
    double filter_time;
    double system_time;
    double device_time;
    double filter_period_error;
    double system_period_error;
    long ncycles;

    TimeFilter(double period, double bandwidth) {
        double o;
        tper = period;
        o = 2 * Math.PI * bandwidth * tper;
        b = Math.sqrt(2 * o);
        c = o * o;
        t0 = 0;
    }

    double update(double time) {
        double e;

        if (t0 == 0) {
            // init loop
            e2 = tper;
            t0 = time;
            t1 = t0 + e2;

            // init stats
            device_time = time;
            system_period_error = filter_period_error = 0;
            ncycles = 0;
        } else {
            // calculate loop error
            e = time - t1;

            // update loop
            t0 = t1;
            t1 += b * e + e2;
            e2 += c * e;

            // update stats
            filter_period_error = t0 - filter_time - tper;
            system_period_error = time - this.system_time - tper;
            device_time += tper;
            ncycles++;
        }

        this.system_time = time;
        filter_time = t0;
        
        return filter_time;
    }
}
