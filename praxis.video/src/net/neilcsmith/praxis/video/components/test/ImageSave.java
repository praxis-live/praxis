/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 - Neil C Smith. All rights reserved.
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
 */
package net.neilcsmith.praxis.video.components.test;

import java.io.File;
import java.lang.ref.SoftReference;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.core.Root;
import net.neilcsmith.praxis.core.ServiceUnavailableException;
import net.neilcsmith.praxis.core.interfaces.Task;
import net.neilcsmith.praxis.core.interfaces.TaskListener;
import net.neilcsmith.praxis.core.types.PReference;
import net.neilcsmith.praxis.core.types.PUri;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.impl.AbstractRoot;
import net.neilcsmith.praxis.impl.TriggerControl;
import net.neilcsmith.praxis.impl.UriProperty;
import net.neilcsmith.praxis.video.DefaultVideoInputPort;
import net.neilcsmith.praxis.video.DefaultVideoOutputPort;
import net.neilcsmith.ripl.components.Delegator;
import net.neilcsmith.ripl.Surface;
import net.neilcsmith.ripl.delegates.AbstractDelegate;
import net.neilcsmith.ripl.impl.BufferedImageSurface;

/**
 *
 * @author Neil C Smith
 */
public class ImageSave extends AbstractComponent {

    private static Logger logger = Logger.getLogger(ImageSave.class.getName());
    private boolean triggered;
    private List<SoftReference<BufferedImageSurface>> pool;
    private ImageSaverListener listener;
    private UriProperty uri;
    private int uriIndex;

    public ImageSave() {
        pool = new ArrayList<SoftReference<BufferedImageSurface>>();
        listener = new ImageSaverListener();
        uri = UriProperty.create(this, PUri.valueOf(new File("image").toURI()));
        Delegator d = new Delegator(new SaveDelegate());
        registerPort(Port.IN, new DefaultVideoInputPort(this, d));
        registerPort(Port.OUT, new DefaultVideoOutputPort(this, d));
        registerControl("file", uri);
        TriggerControl trigger = TriggerControl.create(this, new TriggerBinding());
        registerControl("trigger", trigger);
        registerPort("trigger", trigger.createPort());

    }

    private class TriggerBinding implements TriggerControl.Binding {

        public void trigger(long time) {
            triggered = true;
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

    private class SaveDelegate extends AbstractDelegate {

        public void process(Surface surface) {
            if (triggered) {
                int width = surface.getWidth();
                int height = surface.getHeight();
                BufferedImageSurface bis = getBISurface(width, height);
                bis.copy(surface);
                Root root = getRoot();
                if (root != null) {
                    try {
                        ((AbstractRoot)root).submitTask(new ImageSaver(bis, uri.getValue().value(), uriIndex++), listener);
                    } catch (ServiceUnavailableException ex) {
                        Logger.getLogger(ImageSave.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                triggered = false;
            }

        }

        @Override
        public boolean forceRender() {
            return triggered;
        }
    }

    private class ImageSaver implements Task {

        private BufferedImageSurface bis;
        private URI filename;
        private int number;

        private ImageSaver(BufferedImageSurface bis, URI filename, int number) {
            this.bis = bis;
            this.filename = filename;
            this.number = number;
        }

        public Argument execute() throws Exception {
            bis.save("png", new File(new URI(filename.toString() + number + ".png")));
            return PReference.wrap(bis);
        }
    }

    private class ImageSaverListener implements TaskListener {

        public void taskCompleted(long time, long id, Argument arg) {
            PReference ref = (PReference) arg;
            BufferedImageSurface bis = (BufferedImageSurface) ref.getReference();
            release(bis);
        }

        public void taskError(long time, long id, Argument arg) {
            logger.warning("Unable to save image");
        }
    }
}
