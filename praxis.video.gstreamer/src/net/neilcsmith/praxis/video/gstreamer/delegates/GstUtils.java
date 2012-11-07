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
package net.neilcsmith.praxis.video.gstreamer.delegates;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Neil C Smith
 * http://blogs.sun.com/prasanna/entry/java_regular_expressions_validating_http
 */
class GstUtils {

    private GstUtils() {
    }

    static Map<String, String> parseQueryString(String query) {
        if (query.isEmpty() || query.indexOf('=') == -1) {
            return Collections.emptyMap();
        }
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
//        Pattern ValidURI = Pattern.compile("(?:([a-zA-Z0-9]+)=([^=&]*)&)*([a-zA-Z0-9]+)=([^=&]*)");
        Pattern queryPattern = Pattern.compile("([a-zA-Z0-9]+)=([^=&]*)&*");
        Matcher params = queryPattern.matcher(query);
        while (params.find()) {
            map.put(params.group(1), params.group(2));
        }
        return map;
    }

//    public static void main(String[] args) {
//        String q = "param1=true&param2=fdsjkjh&param3=false";
//        Map<String, String> map = parseQueryString(q);
//        System.out.println(q);
//        System.out.println(map);
//        System.out.println(map == Collections.EMPTY_MAP);
//
//        q = "param1=true&param2=fdsjkjh&param3=fa=lse&param4=am I here?";
//        map = parseQueryString(q);
//        System.out.println(q);
//        System.out.println(map);
//        System.out.println(map == Collections.EMPTY_MAP);
//
//        q = "blahblah";
//        map = parseQueryString(q);
//        System.out.println(q);
//        System.out.println(map);
//        System.out.println(map == Collections.EMPTY_MAP);
//
//        q = "";
//        map = parseQueryString(q);
//        System.out.println(q);
//        System.out.println(map);
//        System.out.println(map == Collections.EMPTY_MAP);
//
//    }
}
