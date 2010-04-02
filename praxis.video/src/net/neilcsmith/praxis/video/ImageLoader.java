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
package net.neilcsmith.praxis.video;

import java.awt.image.BufferedImage;
import java.net.URI;
import javax.imageio.ImageIO;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.Component;
import net.neilcsmith.praxis.core.Task;
import net.neilcsmith.praxis.core.types.PReference;
import net.neilcsmith.praxis.core.types.PUri;
import net.neilcsmith.praxis.impl.ResourceLoader;

/**
 *
 * @author Neil C Smith
 */
public class ImageLoader extends ResourceLoader<BufferedImage> {
    
    private Binding binding;

    private ImageLoader(Component host, Binding binding) {
        super(host, BufferedImage.class);
        if (binding == null) {
            throw new NullPointerException();
        }
        this.binding = binding;
    }

    @Override
    public Task getLoadTask(Argument uri) {
        if (uri == null) {
            throw new NullPointerException();
        }
        return new Loader(uri);
    }
    
//        @Override
//    protected void setResource(BufferedImage resource) {
//        binding.setImage(resource);
//    }



    public static interface Binding {
        
        public void setImage(BufferedImage image);
    }

    private class Loader implements Task {

        private Argument uri;

        private Loader(Argument uri) {
            this.uri = uri;
        }

        public Argument execute() throws Exception {
            URI loc = PUri.coerce(uri).value();
            BufferedImage im = ImageIO.read(loc.toURL());
            return PReference.wrap(im);
        }
    }
    
    public static ImageLoader create(Component host, Binding binding) {
        if (host == null || binding == null) {
            throw new NullPointerException();
        }
        return new ImageLoader(host, binding);
    }

    @Override
    protected void resourceLoaded() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void resourceError() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


}
