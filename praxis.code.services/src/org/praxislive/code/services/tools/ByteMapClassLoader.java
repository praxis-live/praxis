
package org.praxislive.code.services.tools;

import java.util.Map;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
class ByteMapClassLoader extends ClassLoader {
    
    private final Map<String, byte[]> classes;
    
    ByteMapClassLoader(Map<String, byte[]> classes) {
        this.classes = classes;
    }
    
    ByteMapClassLoader(Map<String, byte[]> classes, ClassLoader parent) {
        super(parent);
        this.classes = classes;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] data = classes.get(name);
        if (data == null) {
            throw new ClassNotFoundException(name);
        }
        return defineClass(name, data, 0, data.length);
    }
    
    
    
}
