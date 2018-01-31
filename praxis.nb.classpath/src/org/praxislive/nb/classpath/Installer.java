
package org.praxislive.nb.classpath;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.ModuleInstall;

public class Installer extends ModuleInstall {
    
    private final static Logger LOG = Logger.getLogger(Installer.class.getName());

    @Override
    public void restored() {
        LOG.log(Level.FINE, "Initializing compile classpath");
        File modDir = InstalledFileLocator.getDefault()
                .locate("modules", "org.praxislive.core", false);
        if (modDir != null && modDir.isDirectory()) {
            
            List<File> modules = new ArrayList<>();
            for (File module : modDir.listFiles()) {
                if (module.getName().endsWith(".jar")) {
                    module = module.getAbsoluteFile();
                    LOG.log(Level.FINE, "Adding {0} to compile classpath", module);
                    modules.add(module);
                }
            }
            
            if (modules.isEmpty()) {
                return;
            }
            
            boolean first = true;
            StringBuilder sb = new StringBuilder();
            for (File module : modules) {
                if (!first) {
                    sb.append(File.pathSeparator);
                }
                sb.append(module.getAbsolutePath());
                first = false;
            }
            
            String compileClasspath = sb.toString();
            LOG.log(Level.FINE, "Setting compile classpath to :\n{0}", compileClasspath);
            System.setProperty("env.class.path", compileClasspath);
        }
    }

}
