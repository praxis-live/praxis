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
package org.praxislive.gui.components;

import java.awt.Color;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import org.praxislive.core.Value;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
class Utils {
    
    private final static Border DEFAULT_BORDER = new DefaultBorder();
    
    private Utils() {}
    
    static Border getBorder() {
        return BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
    }
    
    static Color mix(Color bg, Color fg, double mix) {
        int amt = (int) Math.round(mix * 255);
        amt = amt < 0 ? 0 : amt > 255 ? 255 : amt;
        return new Color(
                mix(bg.getRed(), fg.getRed(), amt),
                mix(bg.getGreen(), fg.getGreen(), amt),
                mix(bg.getBlue(), fg.getBlue(), amt)
        );
    }
    
    static boolean equivalent(Value arg1, Value arg2) {
        return arg1.equivalent(arg2) || arg2.equivalent(arg1);
    }
    
    private static int mix(int a, int b, int amt) {
        return a + (((b - a) * amt) >> 8);
    }
    
    
    
    private static class DefaultBorder extends EtchedBorder {
        
        @Override
        public Color getHighlightColor(Component cmp) {
            if (highlight == null) {
                return cmp.getBackground().brighter().brighter();
            } else {
                return super.getHighlightColor(cmp);
            }
        }
        
        @Override
        public Color getShadowColor(Component cmp) {
            if (shadow == null) {
                return cmp.getBackground().brighter();
            } else {
                return super.getShadowColor(cmp);
            }     
        }
        
        
        
    }
    
}
