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
package org.praxislive.tracker.impl;

import java.util.ArrayList;
import java.util.List;
import org.praxislive.core.Value;
import org.praxislive.core.ValueFormatException;
import org.praxislive.core.syntax.Token;
import org.praxislive.core.syntax.Tokenizer;
import org.praxislive.core.types.PNumber;
import org.praxislive.core.types.PString;
import org.praxislive.tracker.Pattern;
import org.praxislive.tracker.Patterns;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
class PatternParser {

    private PatternParser() {
    }

    static Patterns parse(String data) throws ValueFormatException {
        if (data.isEmpty()) {
            return Patterns.EMPTY;
        }
        return parseImpl(data);

    }
    
    private static Patterns parseImpl(String data) throws ValueFormatException {
        List<PatternImpl> patterns = new ArrayList<>();
        List<Value> table = new ArrayList<>();
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
                        throw new ValueFormatException();
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
                        patterns.add(new PatternImpl(table.toArray(new Value[size]), rows, columns));
                        column = 0;
                        maxColumn = 0;
                        table.clear();
                    }
                    break;

                case COMMENT:
                    break;
                default:
                    throw new ValueFormatException();
            }
        }
        if (maxColumn > 0) {
            // still in pattern
            int columns = maxColumn;
            int size = table.size();
            int rows = size / columns;
            patterns.add(new PatternImpl(table.toArray(new Value[size]), rows, columns));
        }

        if (patterns.isEmpty()) {
            return Patterns.EMPTY;
        }
        
        return new PatternsImpl(patterns.toArray(new Pattern[patterns.size()]));
    }

    private static Value getPlainArgument(String token) {
        if (".".equals(token)) {
            return null;
        } else if (token.isEmpty()) {
            return PString.EMPTY;
        } else if ("0123456789-.".indexOf(token.charAt(0)) > -1) {
            try {
                return PNumber.parse(token);
            } catch (ValueFormatException ex) {
                // fall through
            }
        }
        return PString.of(token);
    }

    private static Value getQuotedArgument(String token) {
        if (token.isEmpty()) {
            return PString.EMPTY;
        } else {
            return PString.of(token);
        }
    }

    

    static class PatternImpl extends Pattern {

        private final Value[] values;
        private final int rows;
        private final int columns;

        private PatternImpl(Value[] values, int rows, int columns) {
            assert values.length == rows * columns;
            this.values = values;
            this.rows = rows;
            this.columns = columns;
        }

        @Override
        public Value getValueAt(int row, int column) {
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
