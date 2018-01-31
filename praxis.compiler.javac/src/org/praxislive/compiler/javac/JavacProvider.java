
package org.praxislive.compiler.javac;

import javax.tools.JavaCompiler;
import org.praxislive.compiler.JavaCompilerProvider;
import org.praxislive.compiler.tools.javac.api.JavacTool;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class JavacProvider implements JavaCompilerProvider {

    @Override
    public JavaCompiler getJavaCompiler() {
        return JavacTool.create();
    }
    
}
