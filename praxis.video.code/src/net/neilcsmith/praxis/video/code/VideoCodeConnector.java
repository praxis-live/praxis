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
import java.util.ArrayList;
import java.util.List;
import net.neilcsmith.praxis.code.CodeConnector;
import net.neilcsmith.praxis.code.CodeFactory;
import net.neilcsmith.praxis.code.ResourceProperty;
import net.neilcsmith.praxis.code.userapi.In;
import net.neilcsmith.praxis.code.userapi.P;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.video.code.userapi.OffScreen;
import net.neilcsmith.praxis.video.code.userapi.PImage;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class VideoCodeConnector<T extends VideoCodeDelegate> extends CodeConnector<T> {

//    public final static String SETUP = "setup";
//    public final static String DRAW = "draw";
    
    private VideoOutputPort.Descriptor output;
    private List<OffScreenGraphicsInfo> offscreen;

    public VideoCodeConnector(CodeFactory.Task<T> contextCreator,
            T delegate) {
        super(contextCreator, delegate);
        offscreen = new ArrayList<>();
    }
    
    VideoOutputPort.Descriptor extractOutput() {
        return output;
    }

    OffScreenGraphicsInfo[] extractOffScreenInfo() {
        return offscreen.toArray(new OffScreenGraphicsInfo[offscreen.size()]);
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
            In in = field.getAnnotation(In.class);
            if (in != null) {
                addPort(new VideoInputPort.Descriptor(findID(field), in.value(), field));
                return;
            }
            
            P p = field.getAnnotation(P.class);
            if (p != null) {
                ResourceProperty.Descriptor<PImage> ipd =
                        ResourceProperty.Descriptor.create(this, p, field, ImageLoader.getDefault());
                if (ipd != null) {
                    addControl(ipd);
                    if (shouldAddPort(field)) {
                        addPort(ipd.createPortDescriptor());
                    }
                    return;
                }
            }
        }
        
        if (field.isAnnotationPresent(OffScreen.class)) {
            OffScreenGraphicsInfo osgi = OffScreenGraphicsInfo.create(field);
            if (osgi != null) {
                offscreen.add(osgi);
                return;
            }
        }
        
        super.analyseField(field);
    }
    

    

}
