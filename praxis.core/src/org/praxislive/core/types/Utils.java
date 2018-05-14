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
package org.praxislive.core.types;

import org.praxislive.core.Value;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
class Utils {

    private Utils() {
    }

    static String escape(String input) {
        String res = doPlain(input);
        if (res == null) {
            res = doQuoted(input);
        }
        return res;
    }

    static String escapeQuoted(String input) {
        String res = doQuoted(input);
        return res;
    }

    static boolean equivalent(Value arg1, Value arg2) {
        return arg1.equivalent(arg2) || arg2.equivalent(arg1);
    }
    
    private static String doPlain(String input) {
        int len = input.length();
        if (len == 0 || len > 128) {
            return null;
        }
        if (input.startsWith(".")) {
            // script executor would change this into address
            return null;
        }
        for (int i = 0; i < len; i++) {
            char c = input.charAt(i);
            if (Character.isLetterOrDigit(c) || c == '.' || c == '-' || c == '_') {
                continue;
            } else {
                return null;
            }
        }
        return input;
    }

    private static String doQuoted(String input) {
        int len = input.length();
        if (len == 0) {
            return "\"\"";
        }
        StringBuilder sb = new StringBuilder(len * 2);
        sb.append("\"");
        for (int i = 0; i < len; i++) {
            char c = input.charAt(i);
            switch (c) {
                case '{':
                case '}':
                case '[':
                case ']':
                    sb.append('\\');
                    sb.append(c);
                    break;
                case '\"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                default:
                    sb.append(c);
            }
        }
        sb.append("\"");
        return sb.toString();
    }


}
