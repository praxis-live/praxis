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
package org.praxislive.code.userapi;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.praxislive.core.Value;
import org.praxislive.core.types.PNumber;

/**
 * Annotations for setting meta-data about fields.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Type {

    Class<? extends Value> value() default Value.class;
    java.lang.String[] properties() default {};
    java.lang.String def() default "";
    
    @Deprecated
    Class<? extends Value> cls() default Value.class;

    @Retention(RetentionPolicy.RUNTIME)
    public static @interface Number {
        double min() default PNumber.MIN_VALUE;
        double max() default PNumber.MAX_VALUE;
        double skew() default 1;
        double def() default 0;
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface Integer {
        int min() default PNumber.MIN_VALUE;
        int max() default PNumber.MAX_VALUE;
        int def() default 0;
        int[] suggested() default {};
    }

    @Retention(RetentionPolicy.RUNTIME)
    public static @interface String {
        java.lang.String[] allowed() default {};
        java.lang.String[] suggested() default {};
        boolean emptyIsDefault() default false;
        java.lang.String mime() default "";
        java.lang.String def() default "";
        java.lang.String template() default "";
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface Boolean {
        boolean def() default false;
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface Resource {
        
    }
    

}
