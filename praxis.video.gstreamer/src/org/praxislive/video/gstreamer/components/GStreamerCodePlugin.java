/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2018 Neil C Smith.
 * 
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * version 3 for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License version 3
 * along with this work; if not, see http://www.gnu.org/licenses/
 * 
 * 
 * Please visit https://www.praxislive.org if you need additional information or
 * have any questions.
 */
package org.praxislive.video.gstreamer.components;

import java.lang.reflect.Field;
import org.praxislive.code.CodeConnector;
import org.praxislive.code.ReferenceDescriptor;
import org.praxislive.code.userapi.Inject;
import org.praxislive.logging.LogLevel;
import org.praxislive.video.code.VideoCodeConnector;
import org.praxislive.video.gstreamer.VideoCapture;
import org.praxislive.video.gstreamer.VideoPlayer;

/**
 *
 * @author Neil C Smith - http://www.neilcsmith.net
 */
public class GStreamerCodePlugin implements CodeConnector.Plugin {

    @Override
    public boolean isSupportedConnector(CodeConnector<?> connector) {
        return connector instanceof VideoCodeConnector;
    }

    @Override
    public boolean analyseField(CodeConnector<?> connector, Field field) {
        Class<?> fieldType = field.getType();
        if (fieldType == VideoCapture.class || fieldType == VideoPlayer.class) {
            if (field.isAnnotationPresent(Inject.class) && initGStreamer(connector)) {
                ReferenceDescriptor dsc = fieldType == VideoCapture.class ?
                        GStreamerVideoCapture.Descriptor.create(connector, field) :
                        GStreamerVideoPlayer.Descriptor.create(connector, field);
                if (dsc != null) {
                    connector.addReference(dsc);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean initGStreamer(CodeConnector<?> connector) {
        try {
            GStreamerLibrary.getInstance().init();
            return true;
        } catch (Throwable th) {
            Exception ex = th instanceof Exception
                    ? (Exception) th : new IllegalStateException(th);
            connector.getLog().log(LogLevel.ERROR, ex, "Unable to initialize GStreamer library");
            return false;
        }
    }


}
