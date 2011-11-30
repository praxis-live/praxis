/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Neil C Smith.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License version 3
 * along with this work; if not, see http://www.gnu.org/licenses/
 * 
 *
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 */
package net.neilcsmith.praxis.player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import net.neilcsmith.praxis.core.IllegalRootStateException;
import net.neilcsmith.praxis.hub.TaskServiceImpl;
import net.neilcsmith.praxis.hub.DefaultHub;
import net.neilcsmith.praxis.laf.PraxisLAFManager;
import net.neilcsmith.praxis.script.impl.ScriptServiceImpl;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;
import org.netbeans.spi.sendopts.OptionProcessor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
@ServiceProvider(service = OptionProcessor.class)
public class CLIProcessor extends OptionProcessor {

    private final static Logger LOG = Logger.getLogger(CLIProcessor.class.getName());
    private final static Option ALWAYS = Option.always();
    private final static Option PLAYER =
            Option.withoutArgument(Option.NO_SHORT_NAME, "player");
    private final static Option FILE = Option.defaultArguments();

    @Override
    protected Set<Option> getOptions() {
        Set<Option> opts = new HashSet<Option>(3);
        opts.add(ALWAYS);
        opts.add(PLAYER);
        opts.add(FILE);
        return opts;
    }

    @Override
    protected void process(Env env, Map<Option, String[]> optionValues) throws CommandException {

        // set up UI        
        PraxisLAFManager.getInstance().installUI();
        UIManager.put("ClassLoader", Lookup.getDefault().lookup(ClassLoader.class));

        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "Current Directory : {0}", env.getCurrentDirectory());
            LOG.log(Level.FINE, "netbeans.user.dir : {0}", System.getProperty("netbeans.user.dir"));
        }

        boolean player = optionValues.containsKey(PLAYER);
        String script = null;

        if (optionValues.containsKey(FILE)) {
            String[] files = optionValues.get(FILE);
            if (files.length != 1) {
                throw new CommandException(1, "Too many script files specified on command line.");
            }
            try {
                script = loadScript(env, files[0]);
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, "Error loading script file", ex);
                throw new CommandException(1, "Error loading script file.");
            }
        }

        for (ModuleInfo info : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
            if (info.owns(this.getClass())) {
                System.setProperty("net.neilcsmith.praxisplayer.version",
                        info.getImplementationVersion());
                break;
            }
        }

        try {
            if (!player) {
                if (script == null) {
                    throw new CommandException(1,
                            "When not using the --player option you must pass in a valid script.");
                }
                startNonGuiPlayer(script);

            } else {
                env.getOutputStream().println("WARNING: The Praxis GUI player is deprecated.");
                startPlayer(script);
            }
        } catch (IllegalRootStateException ex) {
            throw new CommandException(1, "Error starting hub.");
        }





//       
    }

    private String loadScript(Env env, String filename) throws IOException {

        LOG.log(Level.FINE, "File : {0}", filename);
        File f = new File(filename);
        if (!f.isAbsolute()) {
            f = new File(env.getCurrentDirectory(), filename);        
        }
        LOG.log(Level.FINE, "java.io.File : {0}", f);
//        LOG.log(Level.FINE, "java.net.URL : {0}", f.toURI().toURL());
        FileObject target = FileUtil.toFileObject(f);
        if (target == null) {
            LOG.log(Level.FINE, "Can't find script file");
            throw new IOException("Can't find script file");
        }
        if (target.isFolder()) {
            target = findProjectFile(target);
        }
        LOG.log(Level.FINE, "Loading script : {0}", target);
        String script = target.asText();
        script = "set _PWD " + FileUtil.toFile(target.getParent()).toURI() + "\n" + script;
        return script;
    }
    
    
    private FileObject findProjectFile(FileObject projectDir) throws IOException {
        LOG.log(Level.FINE, "Searching project directory : {0}", projectDir);
        ArrayList<FileObject> files = new ArrayList<FileObject>(1);
        for (FileObject file : projectDir.getChildren()) {
            LOG.log(Level.FINE, "Found Child : {0}", file);
            if (file.hasExt("pxp")) {
                files.add(file);
            }
        }
        if (files.size() == 1) {
            FileObject file = files.get(0);
            LOG.log(Level.FINE, "Found project file : {0}", file);
            return file;
        } else {
            for (FileObject file : files) {
                LOG.log(Level.FINE, "Checking file : {0}", file);
                if (file.getName().equals(projectDir.getName())) {
                    LOG.log(Level.FINE, "Found project file : {0}", file);
                    return file;
                }
            }
        }
        throw new IOException("No project file found");
    }

    private void startPlayer(String script) throws IllegalRootStateException {
        Player player;
        if (script != null) {
            player = new Player(script);
        } else {
            player = new Player();
        }
        DefaultHub hub = new DefaultHub(new ScriptServiceImpl(),
                new TaskServiceImpl(), player);
        hub.activate();
    }

    private void startNonGuiPlayer(String script) throws IllegalRootStateException {
        DefaultHub hub = new DefaultHub(new ScriptServiceImpl(),
                new TaskServiceImpl(), new NonGuiPlayer(script));

        hub.activate();

    }
}
