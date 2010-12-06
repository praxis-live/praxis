/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Neil C Smith.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.UIManager;
import net.neilcsmith.praxis.core.IllegalRootStateException;
import net.neilcsmith.praxis.hub.TaskServiceImpl;
import net.neilcsmith.praxis.hub.DefaultHub;
import net.neilcsmith.praxis.laf.PraxisLAFManager;
import net.neilcsmith.praxis.script.ScriptServiceImpl;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;
import org.netbeans.spi.sendopts.OptionProcessor;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
@ServiceProvider(service = OptionProcessor.class)
public class CLIProcessor extends OptionProcessor {

    private final static Option ALWAYS = Option.always();
    private final static Option NO_PLAYER =
            Option.withoutArgument(Option.NO_SHORT_NAME, "noplayer");
    private final static Option FILE = Option.defaultArguments();

    @Override
    protected Set<Option> getOptions() {
        Set<Option> opts = new HashSet<Option>(3);
        opts.add(ALWAYS);
        opts.add(NO_PLAYER);
        opts.add(FILE);
        return opts;
    }

    @Override
    protected void process(Env env, Map<Option, String[]> optionValues) throws CommandException {

        PraxisLAFManager.getInstance().installUI();
        UIManager.put("ClassLoader", Lookup.getDefault().lookup(ClassLoader.class));

        env.getOutputStream().println("Current Directory : " + env.getCurrentDirectory());

        boolean noplayer = optionValues.containsKey(NO_PLAYER);
        String script = null;

        if (optionValues.containsKey(FILE)) {
            String[] files = optionValues.get(FILE);
            if (files.length != 1) {
                throw new CommandException(1, "Too many script files specified on command line.");
            }
            try {
                File f = new File(env.getCurrentDirectory(), files[0]);
                env.getOutputStream().println("Loading script : " + f);
                script = loadScript(f);
            } catch (Exception ex) {
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
            if (noplayer) {
                if (script == null) {
                    throw new CommandException(1,
                            "When using the --noplayer option you must pass in a valid script.");
                }
                startNonGuiPlayer(script);

            } else {
                startPlayer(script);
            }
        } catch (IllegalRootStateException ex) {
            throw new CommandException(1, "Error starting hub.");
        }





//       
    }

    private String loadScript(File script) throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader(script));
        StringBuilder str = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            str.append(line);
            str.append('\n');
        }
        return str.toString();

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
