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

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.ref.SoftReference;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.Task;
import net.neilcsmith.praxis.core.TaskListener;
import net.neilcsmith.praxis.core.types.PReference;
import net.neilcsmith.praxis.core.types.PUri;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.impl.TriggerControl;
import net.neilcsmith.praxis.impl.UriProperty;
import net.neilcsmith.praxis.video.DefaultVideoInputPort;
import net.neilcsmith.praxis.video.DefaultVideoOutputPort;
import net.neilcsmith.ripl.components.Delegator;
import net.neilcsmith.ripl.Surface;
import net.neilcsmith.ripl.delegates.Delegate;

/**
 *
 * @author Neil C Smith
 */
public class ImageSave extends AbstractComponent {
    
    private static Logger logger = Logger.getLogger(ImageSave.class.getName());
    
    private boolean triggered;
    private List<SoftReference<BufferedImage>> pool;
    private ImageSaverListener listener;
    private UriProperty uri;
    private int uriIndex;
    
    public ImageSave() {
        pool = new ArrayList<SoftReference<BufferedImage>>();
        listener = new ImageSaverListener();
        uri = UriProperty.create(this, PUri.valueOf(new File("image").toURI()));
        Delegator d = new Delegator(new SaveDelegate());
        registerPort("input", new DefaultVideoInputPort(this, d));
        registerPort("output", new DefaultVideoOutputPort(this, d));        
        registerControl("file", uri);
        TriggerControl trigger = TriggerControl.create(this, new TriggerBinding());
        registerControl("trigger", trigger);
        registerPort("trigger", trigger.getPort());
        
    }
    
    
    private class TriggerBinding implements TriggerControl.Binding {

        public void trigger(long time) {
            triggered = true;
        }
        
    }
    
    private BufferedImage getImage(int width, int height) {
        if (pool.isEmpty()) {
            return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        } else {
            Iterator<SoftReference<BufferedImage>> itr = pool.iterator();
            BufferedImage image = null;
            while (itr.hasNext()) {
                SoftReference<BufferedImage> ref = itr.next();
                BufferedImage im = ref.get();
                if (im == null || im.getWidth() != width || im.getHeight() != height) {
                    itr.remove();
                    continue;
                }
                Graphics2D g2d = im.createGraphics();
                g2d.setComposite(AlphaComposite.Clear);
                g2d.fillRect(0, 0, width, height);
                image = im;
            }
            if (image == null) {
                image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            }
            return image;
        }
    }
    
    private void releaseImage(BufferedImage image) {
        pool.add(new SoftReference<BufferedImage>(image));
    }
    
    private class SaveDelegate extends Delegate {

        public void process(Surface input, Surface surface) {
//            if (triggered) {
////                if (rendering) {
//                    int width = surface.getWidth();
//                    int height = surface.getHeight();
//                    BufferedImage image = getImage(width, height);
//                    Graphics2D g2d = image.createGraphics();
////                    ImageData sData = surface.getImageData();
//                    Image sImg = surface.getImage();
////                    Bounds sBnds = sData.getBounds();
////                    g2d.drawImage(sImg, 0, 0, width, height,
////                            sBnds.getX(), sBnds.getY(),
////                            sBnds.getX() + sBnds.getWidth(),
////                            sBnds.getY() + sBnds.getHeight(), null);
//                    g2d.drawImage(sImg, 0, 0, null); // @TODO fix to honour image bounds
//                    Root root = getRoot();
//                    if (root != null) {
//                        try {
//                            root.submitTask(new ImageSaver(image, uri.getValue().value(), uriIndex++), listener);
//                        } catch (ServiceUnavailableException ex) {
//                            Logger.getLogger(ImageSave.class.getName()).log(Level.SEVERE, null, ex);
//                        }
//                    }
//                    triggered = false;
////                }
//            }
        }

        @Override
        public boolean forceRender() {
            return triggered;
        }
        
    }
    
    private class ImageSaver implements Task {
        
        private BufferedImage image;
        private URI filename;
        private int number;
        
        private ImageSaver(BufferedImage image, URI filename, int number) {
            this.image = image;
            this.filename = filename;
            this.number = number;
        }

        public Argument execute() throws Exception {
            File file = new File(new URI(filename.toString() + number + ".png"));
            ImageIO.write(image, "png", file);
            return PReference.wrap(image);
        }
        
    }
    
    private class ImageSaverListener implements TaskListener {

        public void taskCompleted(long time, long id, Argument arg) {
            PReference ref = (PReference) arg;
            BufferedImage image = (BufferedImage) ref.getReference();
            releaseImage(image);
        }

        public void taskError(long time, long id, Argument arg) {
            logger.warning("Unable to save image");
        }
        
    }
    
}
