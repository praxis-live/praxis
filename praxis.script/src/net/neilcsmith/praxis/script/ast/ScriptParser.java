/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 - Neil C Smith. All rights reserved.
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
package net.neilcsmith.praxis.script.ast;

import java.util.ArrayList;
import java.util.List;
import net.neilcsmith.praxis.core.ArgumentFormatException;
import net.neilcsmith.praxis.core.ComponentAddress;
import net.neilcsmith.praxis.core.ControlAddress;
import net.neilcsmith.praxis.core.PortAddress;
import net.neilcsmith.praxis.core.syntax.InvalidSyntaxException;
import net.neilcsmith.praxis.core.syntax.Token;
import net.neilcsmith.praxis.core.syntax.Tokenizer;
import net.neilcsmith.praxis.core.types.PNumber;
import net.neilcsmith.praxis.core.types.PString;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class ScriptParser {

    private final static ScriptParser INSTANCE = new ScriptParser();

    private ScriptParser() {
    }

    public RootNode parse(CharSequence script) throws InvalidSyntaxException {
        return new RootNode(buildSyntaxTree(script));
    }

    private List<LineNode> buildSyntaxTree(CharSequence script) throws InvalidSyntaxException {
        Tokenizer tkz = new Tokenizer(script);
        List<LineNode> lines = new ArrayList<LineNode>();
        List<Node> current = new ArrayList<Node>();
        for (Token tok : tkz) {
            switch (tok.getType()) {
                case PLAIN:
                    current.add(getPlainNode(tok));
                    break;
                case QUOTED:
                    current.add(getQuotedNode(tok));
                    break;
                case BRACED:
                    current.add(getBracedNode(tok));
                    break;
                case SUBCOMMAND:
                    current.add(getSubcommandNode(tok));
                    break;
                case EOL:
                    if (current.size() > 0) {
                        lines.add(new LineNode(current));
                        current.clear();
                    }
                    break;
                default:
                    break;
            }
        }
        return lines;
    }

    private Node getPlainNode(Token tok) {
        String text = tok.getText();
        if (text.isEmpty()) {
            return new LiteralNode(PString.EMPTY);
        }
        char ch = text.charAt(0);
        if (isDigit(ch)) {
            return numberOrStringNode(text);
        }
        if (text.length() > 1) {
            if (ch == '-') {
                if (isDigit(text.charAt(1))) {
                    return numberOrStringNode(text);
                }
            } else if (ch == '$') {
                return variableNode(text);
            } else if (ch == '.') {
                return relativeAddressNode(text);
            } else if (ch == '/') {
                return absoluteAddressOrStringNode(text);
            }
        }
        return new LiteralNode(PString.valueOf(text));
    }

    private boolean isDigit(char ch) {
        switch (ch) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                return true;
            default:
                return false;
        }
    }

    private Node numberOrStringNode(String text) {
        try {
            return new LiteralNode(PNumber.valueOf(text));
        } catch (Exception ex) {
            return new LiteralNode(PString.valueOf(text));
        }
    }

    private Node variableNode(String text) {
        return new VariableNode(text.substring(1));
    }

    private Node relativeAddressNode(String text) {
        return new AddressNode(text);
    }

    private Node absoluteAddressOrStringNode(String text) {
        try {
            if (text.lastIndexOf('.') > -1) {
                return new LiteralNode(ControlAddress.valueOf(text));
            } else if (text.lastIndexOf('!') > -1) {
                return new LiteralNode(PortAddress.valueOf(text));
            } else {
                return new LiteralNode(ComponentAddress.valueOf(text));
            }
        } catch (ArgumentFormatException ex) {
            return new LiteralNode(PString.valueOf(text));
        }
    }

    private Node getQuotedNode(Token tok) {
        return new LiteralNode(PString.valueOf(tok.getText()));
    }

    private Node getBracedNode(Token tok) {
        return new LiteralNode(PString.valueOf(tok.getText()));
    }

    private Node getSubcommandNode(Token tok) throws InvalidSyntaxException {
        String script = tok.getText();
        List<LineNode> lines = buildSyntaxTree(script);
        return new SubcommandNode(lines);
    }

    public static ScriptParser getInstance() {
        return INSTANCE;
    }
}
