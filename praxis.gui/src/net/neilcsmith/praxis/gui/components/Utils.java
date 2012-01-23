/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Neil C Smith.
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
package net.neilcsmith.praxis.gui.components;

import java.awt.Color;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

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
