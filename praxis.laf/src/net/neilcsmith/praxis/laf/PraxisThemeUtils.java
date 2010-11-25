/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Neil C Smith.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License,
 * or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this work; if not, see http://www.gnu.org/licenses/
 * 
 *
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 *
 */

package net.neilcsmith.praxis.laf;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;

/**
 *
 * @author Neil C Smith
 */
class PraxisThemeUtils {

    private PraxisThemeUtils() {}

//  static void paintFocus( Graphics g, int x, int y, int width, int height, int r1, int r2, Color color) {
//    paintFocus( g, x, y, width, height, r1, r2, 2.0f, color);
//  }

  static void paintFocus( Graphics g, int x, int y, int width, int height, int r1, int r2, float grosor, Color color) {
    Graphics2D g2d = (Graphics2D)g;
    g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    Stroke oldStroke = g2d.getStroke();

    g2d.setColor( color );
    g2d.setStroke( new BasicStroke( grosor));
    if ( r1 == 0 && r2 == 0 ) {
      g.drawRect( x, y, width, height);
    }
    else {
      g.drawRoundRect( x,y, width-1,height-1, r1,r2);
    }

    g2d.setStroke( oldStroke);

    g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
  }

}
