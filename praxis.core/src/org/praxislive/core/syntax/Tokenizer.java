/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2018 Neil C Smith.
 * 
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * version 3 for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License version 3
 * along with this work; if not, see http://www.gnu.org/licenses/
 * 
 * 
 * Please visit https://www.praxislive.org if you need additional information or
 * have any questions.
 */
package org.praxislive.core.syntax;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Class to split a CharSequence into Tokens.
 *
 *
 * @author Neil C Smith
 */
public class Tokenizer implements Iterable<Token> {

    private final CharSequence text;

    /**
     * Create a Tokenizer for the specified text.
     * @param text
     */
    public Tokenizer(CharSequence text) {
        this.text = text;
    }

//    public Tokenizer(String text) {
//        this.text = text;
//    }
    public Iterator<Token> iterator() {
        return new TokenIterator();
    }

    private class TokenIterator implements Iterator<Token> {

        private int index = 0;
        private Token next = null;
        private Token previous = null;
        private final int length = text.length();
        private StringBuilder buf = new StringBuilder(); // a reusable char buffer

        public Token next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            previous = next;
            next = null;
            return previous;
        }

        public void remove() {
            throw new UnsupportedOperationException("Tokens cannot be removed");
        }

        public boolean hasNext() {
            if (next != null) {
                return true;
            }
            try {
                while (next == null && index < length) {
                    char ch = text.charAt(index);

                    switch (ch) {
                        case '\r':
                        case '\n':
                        case ';' :
                            next = parseEOL();
                            break;
                        case '"':
                            next = parseQuotedWord();
                            break;
                        case '[':
                            next = parseSubCommand();
                            break;
                        case '{':
                            next = parseBracedWord();
                            break;
                        case '}':
                        case ']':
                            throw new IllegalArgumentException();
                        case '#':
                            if (previous == null || previous.getType() == Token.Type.EOL) {
                                next = parseComment();
                                break;
                            }
                        case '\\':
                            ch = text.charAt(index + 1);
                            if (ch == '\r' || ch == '\n' || ch == ';') {
                                consumeEscapedEOL();
                                break;
                            }
                        default:
                            // ignore whitespace
                            if (Character.isWhitespace(ch)) {
                                index++;
                            } else {
                                next = parseWord();
                            }
                    }


                }

            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }


            if (next != null) {
                return true;
            } else if (previous == null || previous.getType() != Token.Type.EOL) {
                next = new Token(Token.Type.EOL, "", text.length(), text.length());
                return true;
            } else {
                return false;
            }


        }

        private void consumeEscapedEOL() {
            index++; //consume \
            char ch = text.charAt(index);
            if (ch == '\r') {
                if (index + 1 < length && text.charAt(index + 1) == '\n') {
                    index++; // consume extra character
                }
            }
            index++;
        }

        private char parseEscape() {
            index++; // consume \
            if (index < length) {
                char ch = text.charAt(index);
                switch (ch) {
                    case '\r':
                        if (index < length - 1 && text.charAt(index + 1) == '\n') {
                            index++; // consume \r
                        }
                    case '\n':
                        return ' ';
                    case 'n':
                        return '\n';
                    case 't':
                        return '\t';
                    default:
                        if (!Character.isLetterOrDigit(ch)) {
                            return ch;
                        }
                }
            }
            throw new RuntimeException();

        }

        private Token parseWord() {
            int startIndex = index;
            buf.setLength(0);
            while (index < length) {
                char ch = text.charAt(index);
                if (ch == '\\') {
                    char esc = text.charAt(index + 1);
                    if (esc == '\r' || esc == '\n') {
                        break;
                    }
                    ch = parseEscape();
                } else if (ch == ';' || Character.isWhitespace(ch)) {
                    break;
                }
                buf.append(ch);
                index++;
            }
            return new Token(Token.Type.PLAIN, buf.toString(), startIndex, index);
        }

        private Token parseQuotedWord() {
            int startIndex = index;
            index++; // consume "
            boolean inToken = true;
            buf.setLength(0);
            while (index < length && inToken) {
                char ch = text.charAt(index);
                if (ch == '\\') {
                    ch = parseEscape();
                } else if (ch == '"') {
                    index++; // consume ending "
                    inToken = false;
                    break;
                }
                buf.append(ch);
                index++;
            }
            if (inToken) {
                throw new RuntimeException();
            }
            return new Token(Token.Type.QUOTED, buf.toString(), startIndex, index);
        }

        private Token parseBracedWord() {
            int startIndex = index;
            index++; // consume {
            int level = 1;
            buf.setLength(0);
            while (index < length && level > 0) {
                char ch = text.charAt(index);
                if (ch == '}') {
                    if (text.charAt(index - 1) != '\\') {
                        level--;
                    } else {
//                        buf.delete(buf.length() - 1, buf.length());
                    }
                } else if (ch == '{') {
                    if (text.charAt(index - 1) != '\\') {
                        level++;
                    } else {
//                        buf.delete(buf.length() - 1, buf.length());
                    }
                }
                if (level > 0) {
                    buf.append(ch);
                }
                index++;
            }
            if (level > 0) {
                throw new RuntimeException();
            }
            return new Token(Token.Type.BRACED, buf.toString(), startIndex, index);
        }

        private Token parseSubCommand() {
            int startIndex = index;
            index++; // consume [
            int level = 1;
            buf.setLength(0);
            while (index < length && level > 0) {
                char ch = text.charAt(index);
                if (ch == ']') {
                    if (text.charAt(index - 1) != '\\') {
                        level--;
                    }
                } else if (ch == '[') {
                    if (text.charAt(index - 1) != '\\') {
                        level++;
                    }
                }
                if (level > 0) {
                    buf.append(ch);
                }
                index++;
            }
            if (level > 0) {
                throw new RuntimeException();
            }
            return new Token(Token.Type.SUBCOMMAND, buf.toString(), startIndex, index);
        }

        private Token parseComment() {
            int startIndex = index;
            index++; // consume #
            buf.setLength(0);
            while (index < length) {
                char ch = text.charAt(index);
                if (ch == '\\') {
                    char esc = text.charAt(index + 1);
                    if (esc == '\r' || esc == '\n') {
                        consumeEscapedEOL();
                        ch = ' ';
                    }
                } else if (ch == '\r' || ch == '\n') {
                    break;
                }
                if (buf.length() == 0 && Character.isWhitespace(ch)) {
                    index++;
                } else {
                    buf.append(ch);
                    index++;
                }
            }
            return new Token(Token.Type.COMMENT, buf.toString(), startIndex, index);

        }

        private Token parseEOL() {
            int startIndex = index;
            char ch = text.charAt(index);
            if (ch == '\r') {
                if (index + 1 < length && text.charAt(index + 1) == '\n') {
                    index++; // consume extra character
                }
            }
            index++;
            return new Token(Token.Type.EOL, "", startIndex, index);
        }
    }
}
