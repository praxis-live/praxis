/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2013 Neil C Smith.
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
package org.praxislive.video.components.test;

import java.io.File;
import java.lang.ref.SoftReference;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.praxislive.core.Value;
import org.praxislive.core.Port;
import org.praxislive.core.interfaces.ServiceUnavailableException;
import org.praxislive.core.interfaces.TaskService;
import org.praxislive.core.types.PReference;
import org.praxislive.core.types.PResource;
import org.praxislive.impl.AbstractComponent;
import org.praxislive.impl.TaskControl;
import org.praxislive.impl.TriggerControl;
import org.praxislive.impl.UriProperty;
import org.praxislive.video.impl.DefaultVideoInputPort;
import org.praxislive.video.impl.DefaultVideoOutputPort;
import org.praxislive.video.pipes.VideoPipe;
import org.praxislive.video.pipes.impl.SingleInOut;
import org.praxislive.video.render.Surface;
import org.praxislive.video.render.utils.BufferedImageSurface;


/**
 *
 * @author Neil C Smith
 */
public class ImageSave extends AbstractComponent {

    private static Logger logger = Logger.getLogger(ImageSave.class.getName());
    private boolean triggered;
    private long triggerTime;
    private List<SoftReference<BufferedImageSurface>> pool;
    private SaveCallback callback;
    private UriProperty uri;
    private int uriIndex;
    private TaskControl tasks;

    public ImageSave() {
        pool = new ArrayList<SoftReference<BufferedImageSurface>>();
        callback = new SaveCallback();
        uri = UriProperty.create(PResource.valueOf(new File("image").toURI()));
        SavePipe savePipe = new SavePipe();
        registerPort(Port.IN, new DefaultVideoInputPort(savePipe));
        registerPort(Port.OUT, new DefaultVideoOutputPort(savePipe));
        registerControl("file", uri);
        TriggerControl trigger = TriggerControl.create(new TriggerBinding());
        registerControl("trigger", trigger);
        registerPort("trigger", trigger.createPort());
        tasks = new TaskControl();
        registerControl("__tasks", tasks);

    }

    private class TriggerBinding implements TriggerControl.Binding {

        public void trigger(long time) {
            triggered = true;
            triggerTime = time;
        }
    }

    private BufferedImageSurface getBISurface(int width, int height) {
        if (pool.isEmpty()) {
            return new BufferedImageSurface(width, height, false);
        } else {
            Iterator<SoftReference<BufferedImageSurface>> itr = pool.iterator();
            BufferedImageSurface ret = null;
            while (itr.hasNext()) {
                SoftReference<BufferedImageSurface> ref = itr.next();
                BufferedImageSurface im = ref.get();
                if (im == null || im.getWidth() != width || im.getHeight() != height) {
                    itr.remove();
                    continue;
                }
                im.clear();
                ret = im;
            }
            if (ret == null) {
                ret = new BufferedImageSurface(width, height, false);
            }
            return ret;
        }
    }

    private void release(BufferedImageSurface surface) {
        pool.add(new SoftReference<BufferedImageSurface>(surface));
    }

    private class SavePipe extends SingleInOut {

        public void process(Surface surface, boolean rendering) {
            if (triggered) {
                int width = surface.getWidth();
                int height = surface.getHeight();
                BufferedImageSurface bis = getBISurface(width, height);
                bis.copy(surface);
                try {
                    tasks.submitTask(triggerTime,
                            new ImageSaver(bis, uri.getValue().value(), uriIndex++),
                            callback);
                } catch (ServiceUnavailableException ex) {
                    Logger.getLogger(ImageSave.class.getName()).log(Level.SEVERE, null, ex);
                }
                triggered = false;
            }
        }

        @Override
        protected boolean isRenderRequired(VideoPipe source, long time) {
            if (triggered) {
                return true;
            } else {
                return super.isRenderRequired(source, time);
            }
        }
        
    }

    private class ImageSaver implements TaskService.Task {

        private BufferedImageSurface bis;
        private URI filename;
        private int number;

        private ImageSaver(BufferedImageSurface bis, URI filename, int number) {
            this.bis = bis;
            this.filename = filename;
            this.number = number;
        }

        public Value execute() throws Exception {
            bis.save("png", new File(new URI(filename.toString() + number + ".png")));
            return PReference.wrap(bis);
        }
    }

    private class SaveCallback implements TaskControl.Callback {

        public void taskCompleted(long time, long id, Value arg) {
            PReference ref = (PReference) arg;
            BufferedImageSurface bis = (BufferedImageSurface) ref.getReference();
            release(bis);
        }

        public void taskError(long time, long id, Value arg) {
            logger.warning("Unable to save image");
        }
    }
}
