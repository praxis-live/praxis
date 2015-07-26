/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Neil C Smith.
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
package net.neilcsmith.praxis.tracker;

import java.util.ArrayList;
import java.util.List;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ArgumentFormatException;
import net.neilcsmith.praxis.core.syntax.Token;
import net.neilcsmith.praxis.core.syntax.Tokenizer;
import net.neilcsmith.praxis.core.types.PNumber;
import net.neilcsmith.praxis.core.types.PString;
import org.praxislive.tracker.Pattern;
import org.praxislive.tracker.Patterns;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
class PatternParser {

    private PatternParser() {
    }

    static Patterns parse(String data) throws ArgumentFormatException {
        if (data.isEmpty()) {
            return Patterns.EMPTY;
        }
        return parseImpl(data);

    }
    
    private static Patterns parseImpl(String data) throws ArgumentFormatException {
        List<PatternImpl> patterns = new ArrayList<>();
        List<Argument> table = new ArrayList<>();
        int maxColumn = 0;
        int column = 0;
        Tokenizer tk = new Tokenizer(data);
        for (Token t : tk) {
            Token.Type type = t.getType();
            switch (type) {
                case PLAIN:
                case QUOTED:
                case BRACED:
                    if (maxColumn > 0 && column >= maxColumn) {
                        throw new ArgumentFormatException();
                    }
                    table.add(type == Token.Type.PLAIN
                            ? getPlainArgument(t.getText())
                            : getQuotedArgument(t.getText()));
                    column++;
                    break;
                case EOL:
                    if (column > 0) {
                        // inside pattern
                        if (maxColumn == 0) {
                            maxColumn = column;
                        } else {
                            // pad to end of row
                            while (column < maxColumn) {
                                table.add(null);
                                column++;
                            }
                        }
                        column = 0;
                    } else if (maxColumn > 0) {
                        // end of pattern
                        int columns = maxColumn;
                        int size = table.size();
                        int rows = size / columns;
                        patterns.add(new PatternImpl(table.toArray(new Argument[size]), rows, columns));
                        column = 0;
                        maxColumn = 0;
                        table.clear();
                    }
                    break;

                case COMMENT:
                    break;
                default:
                    throw new ArgumentFormatException();
            }
        }
        if (maxColumn > 0) {
            // still in pattern
            int columns = maxColumn;
            int size = table.size();
            int rows = size / columns;
            patterns.add(new PatternImpl(table.toArray(new Argument[size]), rows, columns));
        }

        if (patterns.isEmpty()) {
            return Patterns.EMPTY;
        }
        
        return new PatternsImpl(patterns.toArray(new Pattern[patterns.size()]));
    }

    private static Argument getPlainArgument(String token) {
        if (".".equals(token)) {
            return null;
        } else if (token.isEmpty()) {
            return PString.EMPTY;
        } else if ("0123456789-.".indexOf(token.charAt(0)) > -1) {
            try {
                return PNumber.valueOf(token);
            } catch (ArgumentFormatException ex) {
                // fall through
            }
        }
        return PString.valueOf(token);
    }

    private static Argument getQuotedArgument(String token) {
        if (token.isEmpty()) {
            return PString.EMPTY;
        } else {
            return PString.valueOf(token);
        }
    }

    

    static class PatternImpl extends Pattern {

        private final Argument[] values;
        private final int rows;
        private final int columns;

        private PatternImpl(Argument[] values, int rows, int columns) {
            assert values.length == rows * columns;
            this.values = values;
            this.rows = rows;
            this.columns = columns;
        }

        @Override
        public Argument getValueAt(int row, int column) {
            return values[(row * columns) + column];
        }

        @Override
        public int getRowCount() {
            return rows;
        }

        @Override
        public int getColumnCount() {
            return columns;
        }

    }

    static class PatternsImpl extends Patterns {

        private final Pattern[] patterns;

        private PatternsImpl(Pattern[] patterns) {
            this.patterns = patterns;
        }

        @Override
        public Pattern getPattern(int index) {
            return patterns[index];
        }

        @Override
        public int getPatternCount() {
            return patterns.length;
        }

    }

}
