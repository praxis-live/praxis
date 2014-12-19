/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Neil C Smith.
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
 */
package net.neilcsmith.praxis.video.code;

import java.lang.reflect.Field;
import java.util.logging.Logger;
import net.neilcsmith.praxis.code.CodeConnector;
import net.neilcsmith.praxis.code.CodeFactory;
import net.neilcsmith.praxis.code.userapi.In;
import net.neilcsmith.praxis.code.userapi.Out;
import net.neilcsmith.praxis.code.userapi.Output;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.video.code.userapi.PImage;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class VideoCodeConnector<T extends VideoCodeDelegate> extends CodeConnector<T> {
    
    private final static Logger LOG = Logger.getLogger(VideoCodeConnector.class.getName());
    
    public final static String SETUP = "setup";
    public final static String DRAW = "draw";
    
    private VideoOutputPort.Descriptor output;
    

    public VideoCodeConnector(CodeFactory.Task<T> contextCreator,
            T delegate) {
        super(contextCreator, delegate);
    }
    
    VideoOutputPort.Descriptor extractOutput() {
        return output;
    }

    @Override
    protected void addDefaultPorts() {
        super.addDefaultPorts();
        output = new VideoOutputPort.Descriptor(Port.OUT, Integer.MIN_VALUE);
        addPort(output);
    }

    @Override
    protected void analyseField(Field field) {
        if (PImage.class.isAssignableFrom(field.getType())) {
            In ann = field.getAnnotation(In.class);
            if (ann != null) {
                field.setAccessible(true);
                addPort(new VideoInputPort.Descriptor(findID(field), ann.value(), field));
                return;
            }
        }
        if (Output.class.isAssignableFrom(field.getType())) {
            LOG.warning("Output fields not currently supported in video components");
            return;
        }
        super.analyseField(field);
    }

}
