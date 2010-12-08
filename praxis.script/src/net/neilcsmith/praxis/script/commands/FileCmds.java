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
package net.neilcsmith.praxis.script.commands;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.ArgumentFormatException;
import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.types.PArray;
import net.neilcsmith.praxis.core.types.PString;
import net.neilcsmith.praxis.core.types.PResource;
import net.neilcsmith.praxis.script.Command;
import net.neilcsmith.praxis.script.CommandInstaller;
import net.neilcsmith.praxis.script.Env;
import net.neilcsmith.praxis.script.ExecutionException;
import net.neilcsmith.praxis.script.Namespace;
import net.neilcsmith.praxis.script.Variable;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class FileCmds implements CommandInstaller {

    private final static FileCmds instance = new FileCmds();
    
    private final static Command FILE = new FileCmd();
    private final static Command FILE_LIST = new FileListCmd();
    private final static Command FILE_NAMES = new FileNamesCmd();

    private FileCmds() {
    }

    public void install(Map<String, Command> commands) {
        commands.put("file", FILE);
        commands.put("file-list", FILE_LIST);
        commands.put("file-names", FILE_NAMES);
    }

    public static FileCmds getInstance() {
        return instance;
    }


    private static class FileCmd extends AbstractInlineCommand {

        public CallArguments process(Env env, Namespace namespace, CallArguments args) throws ExecutionException {
            if (args.getSize() != 1) {
                throw new ExecutionException();
            }
            try {
                Variable pwd = namespace.getVariable(Env.PWD);
                URI base;
                if (pwd == null) {
                    base = new File("").toURI();
                } else {
                    base = PResource.coerce(pwd.getValue()).value();
                }
                URI path = base.resolve(new URI(null, null, args.get(0).toString(), null));
                return CallArguments.create(PResource.valueOf(path));
            } catch (Exception ex) {
                throw new ExecutionException(ex);
            }
        }
    }

    private static class FileListCmd extends AbstractInlineCommand {

        public CallArguments process(Env env, Namespace namespace, CallArguments args) throws ExecutionException {
            if (args.getSize() != 1) {
                throw new ExecutionException();
            }
            try {
                File dir = new File(PResource.coerce(args.get(0)).value());
                if (dir.isDirectory()) {
                    return CallArguments.create(buildFileList(dir));
                }
            } catch (ArgumentFormatException ex) {
                throw new ExecutionException(ex);
            }
            throw new ExecutionException();

        }

        private PArray buildFileList(File dir) {
            File[] files = dir.listFiles();
            List<PResource> uris = new ArrayList<PResource>();
            for (File f : files) {
                uris.add(PResource.valueOf(f.toURI()));
            }
            Collections.sort(uris);
            return PArray.valueOf(uris);
        }

    }

    private static class FileNamesCmd extends AbstractInlineCommand {

        public CallArguments process(Env env, Namespace namespace, CallArguments args) throws ExecutionException {
            if (args.getSize() != 1) {
                throw new ExecutionException();
            }
            try {
                File dir = new File(PResource.coerce(args.get(0)).value());
                if (dir.isDirectory()) {
                    return CallArguments.create(buildFileList(dir));
                }
            } catch (ArgumentFormatException ex) {
                throw new ExecutionException(ex);
            }
            throw new ExecutionException();

        }

        private PArray buildFileList(File dir) {
            File[] files = dir.listFiles();
            List<PString> names = new ArrayList<PString>();
            for (File f : files) {
                names.add(PString.valueOf(f.getName()));
            }
            Collections.sort(names);
            return PArray.valueOf(names);
        }

    }
}
