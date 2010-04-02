/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 - Neil C Smith. All rights reserved.
 * 
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details.
 * 
 * You should have received a copy of the GNU General Public License version 2
 * along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 */
package net.neilcsmith.praxis.script;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.Call;
import net.neilcsmith.praxis.core.ControlAddress;
import net.neilcsmith.praxis.core.ArgumentFormatException;
import net.neilcsmith.praxis.core.syntax.InvalidSyntaxException;
import net.neilcsmith.praxis.core.syntax.Token;
import net.neilcsmith.praxis.core.syntax.Tokenizer;
import net.neilcsmith.praxis.core.types.PString;
import net.neilcsmith.praxis.core.types.PUri;

/**
 * This class is only a temporary basic script parser.
 * @author Neil C Smith
 */
class TempScriptParser {

//    private CharSequence script;
    private Token[][] ast;
    private int lineNo;
    private Map<String, ControlAddress> commands;
    private Map<String, String> variables;

    public TempScriptParser(CharSequence script) throws InvalidSyntaxException {
        if (script == null) {
            throw new NullPointerException();
        }
        buildSyntaxTree(script);
        buildCommandMap();
//        this.script = script;
        variables = new HashMap<String, String>();
    }

    private void buildCommandMap() {
        try {
            commands = new HashMap<String, ControlAddress>();
            commands.put("create", ControlAddress.valueOf("/praxis.create"));
            commands.put("connect", ControlAddress.valueOf("/praxis.connect"));
        } catch (ArgumentFormatException ex) {
            Logger.getLogger(TempScriptParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void buildSyntaxTree(CharSequence script) throws InvalidSyntaxException {
        Tokenizer tkz = new Tokenizer(script);
        Token[] tokenArray = new Token[0];
        List<Token[]> lines = new ArrayList<Token[]>();
        List<Token> current = new ArrayList<Token>();
        for (Token tok : tkz) {
            switch (tok.getType()) {
                case PLAIN:
                case QUOTED:
                case BRACED:
                case SUBCOMMAND:
                    current.add(tok);
                    break;
                case EOL:
                    if (current.size() > 0) {
                        lines.add(current.toArray(tokenArray));
                        current.clear();
                    }
                default:
                    break;

            }
        }
        ast = lines.toArray(new Token[0][0]);
    }

    public Token[] getNextLine() {
        if (lineNo < ast.length) {
            return ast[lineNo++];

        } else {
            return null;
        }
    }

    public Call getNextCall(ControlAddress responseAddress, long timeCode)
            throws InvalidSyntaxException {
        Token[] line = getNextLine();
        if (line == null) {
            return null;
        }
        try {
            String com = line[0].getText();
            if (com.equals("set")) { // quick hack for variables support
                if (line.length != 3) {
                    throw new InvalidSyntaxException();
                }
                variables.put(line[1].getText(), line[2].getText());
                line = getNextLine();
                if (line == null) {
                    return null;
                }
                com = line[0].getText();
            }
            ControlAddress to = commands.get(com);
            if (to == null) {
                to = ControlAddress.valueOf(com);
            }
            CallArguments callArgs;
            if (line.length > 1) {
                Argument[] args = new Argument[line.length - 1];
                for (int i = 1; i < line.length; i++) {
                    Token tok = line[i];
                    if (tok.getType() == Token.Type.SUBCOMMAND) {
                        args[i - 1] = processSubcommand(tok);
                    } else {
                        args[i - 1] = PString.valueOf(line[i].getText());
                    }
                    
                }
                callArgs = CallArguments.create(args);
//            } else if (line.length == 2) {
//                Argument arg = PString.valueOf(line[1].getText());
//                callArgs = CallArguments.create(arg);
//
            } else {
                callArgs = CallArguments.EMPTY;
            }
            return Call.createCall(to, responseAddress, timeCode, callArgs);
        } catch (ArgumentFormatException ex) {
            throw new InvalidSyntaxException(); // link exception!
        }

    }
    
    private Argument processSubcommand(Token tok) throws InvalidSyntaxException {
        // hack for file support
        Iterator<Token> tknstream = new Tokenizer(tok.getText()).iterator();
        try {
            if (tknstream.next().getText().equals("file")) {
                String path = tknstream.next().getText();
                String base = variables.get("PWD");
                URI baseuri;
                if (base == null) {
                    baseuri = new File("").toURI();
//                    System.out.println("Base set to home directory");
                } else {
                    baseuri = new URI(base);
//                    System.out.println("Base set to " + baseuri);
                }
                URI pathuri = baseuri.resolve(new URI(null, null, path, null));
//                System.out.println("Path resolved to " + pathuri);
                return PUri.valueOf(pathuri);
            }
        } catch (Exception ex) {   
            System.out.println(ex);
        }
        throw new InvalidSyntaxException();
        
        
    }
}
