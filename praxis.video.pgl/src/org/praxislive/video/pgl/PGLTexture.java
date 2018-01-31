

package org.praxislive.video.pgl;

import processing.opengl.PGraphicsOpenGL;
import processing.opengl.Texture;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
class PGLTexture extends Texture {

    public PGLTexture(PGraphicsOpenGL pg, int width, int height, Object params) {
        super(pg, width, height, params);
    }

    @Override
    protected boolean contextIsOutdated() {
        return super.contextIsOutdated();
    }

    @Override
    protected void dispose() {
        super.dispose();
    }
    
    
    
}
