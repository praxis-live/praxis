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
package net.neilcsmith.praxis.video.components;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.video.ClientRegistrationException;
import net.neilcsmith.praxis.video.impl.DefaultVideoInputPort;
import net.neilcsmith.praxis.video.VideoContext;
import net.neilcsmith.ripl.components.Placeholder;
import net.neilcsmith.ripl.Source;

/**
 *
 * @author Neil C Smith
 */
public class VideoOutput extends AbstractComponent {

    private Placeholder placeholder;
//    private VideoContext ctxt;
    private VideoContext.OutputClient client;

    public VideoOutput() {
        placeholder = new Placeholder();
        registerPort(Port.IN, new DefaultVideoInputPort(this, placeholder));
        client = new VideoContext.OutputClient() {

            public int getOutputCount() {
                return 1;
            }

            public Source getOutputSource(int index) {
                if (index == 0) {
                    return placeholder;
                } else {
                    throw new IndexOutOfBoundsException();
                }
            }
        };
    }

    @Override
    public void hierarchyChanged() {
        super.hierarchyChanged();
        VideoContext ctxt = getLookup().get(VideoContext.class);
        if (ctxt != null) {
            try {
                ctxt.registerVideoOutputClient(client);
            } catch (ClientRegistrationException ex) {
                Logger.getLogger(VideoOutput.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
