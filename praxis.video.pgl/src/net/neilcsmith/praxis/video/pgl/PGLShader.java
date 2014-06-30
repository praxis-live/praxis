

package net.neilcsmith.praxis.video.pgl;

import processing.opengl.PShader;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
class PGLShader extends PShader {

    PGLShader(PGLContext context, String vertex, String fragment) {
        super(context.primary().parent);
        setVertexShader(new String[]{vertex});
        setFragmentShader(new String[]{fragment});
        setType(TEXTURE);
    }

    @Override
    protected void dispose() {
        super.dispose();
    }
    
    
    
    
}
