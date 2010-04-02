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
package net.neilcsmith.jnajack.examples;

import java.nio.FloatBuffer;
import net.neilcsmith.jnajack.util.SimpleAudioClient;

/**
 *
 * @author Neil C Smith
 */
public class PassThrough implements SimpleAudioClient.Processor {


    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        SimpleAudioClient client = SimpleAudioClient.create("pass-through", new String[] {"input-L", "input-R"},
                new String[]{"output-L", "output-R"}, new PassThrough());
        client.activate();
        while (true) {
            Thread.sleep(1000);
        }
    }

    public void setup(float samplerate, int buffersize) {

    }

    public void process(FloatBuffer[] inputs, FloatBuffer[] outputs) {
        outputs[0].put(inputs[0]);
        outputs[1].put(inputs[1]);

    }

    public void shutdown() {
        System.out.println("Pass Through shutdown");
    }
}
