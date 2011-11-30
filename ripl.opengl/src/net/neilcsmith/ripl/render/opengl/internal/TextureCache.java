/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.neilcsmith.ripl.render.opengl.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class TextureCache {
    
    private final static Logger LOGGER = Logger.getLogger(TextureCache.class.getName());
    
    private static ThreadLocal<CacheImpl> cache = new ThreadLocal<CacheImpl>() {

        @Override
        protected CacheImpl initialValue() {
            return new CacheImpl();
        }

    };
    
    private TextureCache() {}
    
    public static Texture acquire(int width, int height) {
        return cache.get().acquire(width, height);
    }
    
    public static void release(Texture texture) {
        cache.get().release(texture);
    }
    
    
    private static class CacheImpl {
        
        private Texture[] textures;
        
        private CacheImpl() {
            textures = new Texture[8];
        }
        
        private Texture acquire(int width, int height) {
            Texture tex;
            for (int i=0; i < textures.length; i++) {
                tex = textures[i];
                if (tex != null && tex.getWidth() >= width && tex.getHeight() >= height) {
                    textures[i] = null;
                    return tex;
                }
            }
            LOGGER.log(Level.FINE, "Creating new texture of size {0}x{1}", new Object[]{width, height});
            tex = new Texture(width, height);
            return tex;
        }
        
        private void release(Texture texture) {
            for (int i=0; i < textures.length; i++) {
                if (textures[i] == null) {
                    textures[i] = texture;
                    return;
                }
            }
            Texture t;
            for (int i=0; i < textures.length; i++) {
                t = textures[i];
                if (t.getWidth() < texture.getWidth() ||
                        t.getHeight() < texture.getHeight()) {
                    t.dispose();
                    textures[i] = texture;
                    return;
                }
            }
            texture.dispose();
        }
        
    }
    
}
