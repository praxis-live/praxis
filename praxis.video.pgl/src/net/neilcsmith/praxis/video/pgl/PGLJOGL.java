
package net.neilcsmith.praxis.video.pgl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import processing.opengl.PGraphicsOpenGL;
import processing.opengl.PJOGL;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
class PGLJOGL extends PJOGL {

    boolean disposing;
    
    public PGLJOGL(PGraphicsOpenGL pg) {
        super(pg);
    }

    @Override
    public void dispose() {
        super.dispose();
        sketch = null;
        graphics = null;
        
        if (primaryPGL) {
            disposeResources();
        }
    }
    
    String[] preprocessFragmentSource(String[] fragment) {
        return preprocessFragmentSource(fragment, getGLSLVersion());
    }
    
    String[] preprocessVertexSource(String[] vertex) {
        return preprocessVertexSource(vertex, getGLSLVersion());
    }
    
    private void disposeResources() {
        try {
            for (Class<?> cls : PGraphicsOpenGL.class.getDeclaredClasses()) {
                if (cls.getSimpleName().startsWith("GLResource")) {
                    Field refListField = cls.getDeclaredField("refList");
                    refListField.setAccessible(true);
                    List<?> refList = (List<?>) refListField.get(null);
                    for (Object ref : refList.toArray()) {
                        Field pglField = ref.getClass().getDeclaredField("pgl");
                        pglField.setAccessible(true);
                        if (pglField.get(ref) == this) {
                            pglField.set(ref, null);
//                            Method disposeMethod = 
//                                    ref.getClass().getDeclaredMethod("dispose");
//                            disposeMethod.setAccessible(true);
//                            disposeMethod.invoke(ref);             
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(PGLJOGL.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
