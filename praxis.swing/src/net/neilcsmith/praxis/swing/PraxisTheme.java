/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008/09 - Neil C Smith. All rights reserved.
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
package net.neilcsmith.praxis.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;
import javax.swing.UIDefaults;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.DimensionUIResource;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;

/**
 *
 * @author Neil C Smith
 */
public class PraxisTheme extends DefaultMetalTheme {
//nimrodlf.p1=#5454C0
//nimrodlf.p2=#5E5ECA
//nimrodlf.p3=#6868D4
//nimrodlf.s1=#191E27
//nimrodlf.s2=#232831
//nimrodlf.s3=#2D323B
//nimrodlf.w=#494854
//nimrodlf.b=#FFFFFF

//    private Color fg = new Color(153,204,255);
//    private Color bg = new Color(0,0,10);
//    private ColorUIResource b = new ColorUIResource(Color.decode("#ffffff"));
//    private ColorUIResource w = new ColorUIResource(Color.decode("#717171"));
//    private ColorUIResource primary1 = new ColorUIResource(Color.decode("#a1a0a0"));
//    private ColorUIResource primary2 = new ColorUIResource(Color.decode("#abaaaa"));
//    private ColorUIResource primary3 = new ColorUIResource(Color.decode("#b5b4b4"));
//    private ColorUIResource secondary1 = new ColorUIResource(Color.decode("#464746"));
//    private ColorUIResource secondary2 = new ColorUIResource(Color.decode("#666666"));
//    private ColorUIResource secondary3 = new ColorUIResource(Color.decode("#5a5b5a"));

    // current theme
    private ColorUIResource b = new ColorUIResource(Color.decode("#ffffff"));
    private ColorUIResource w = new ColorUIResource(Color.decode("#181818"));
    private ColorUIResource primary1 = new ColorUIResource(Color.decode("#a0a0a5"));
    private ColorUIResource primary2 = new ColorUIResource(Color.decode("#77777a"));
    private ColorUIResource primary3 = new ColorUIResource(Color.decode("#666667"));
    private ColorUIResource secondary1 = new ColorUIResource(Color.decode("#666667"));
    private ColorUIResource secondary2 = new ColorUIResource(Color.decode("#555556"));
    private ColorUIResource secondary3 = new ColorUIResource(Color.decode("#39393a"));



//    private ColorUIResource primary1 = new ColorUIResource(33,66,66);
//    private ColorUIResource primary2 = new ColorUIResource(66,99,99);
//    private ColorUIResource primary3 = new ColorUIResource(99,99,99);
//    private ColorUIResource secondary1 = new ColorUIResource(0,0,0);
//    private ColorUIResource secondary2 = new ColorUIResource(51,51,51);
//    private ColorUIResource secondary3 = new ColorUIResource(102,102,102);
//    private ColorUIResource b = new ColorUIResource(255,255,255);
//    private ColorUIResource w = new ColorUIResource(0,0,0);
    @Override
    protected ColorUIResource getPrimary1() {
        return primary1;
    }

    @Override
    protected ColorUIResource getPrimary2() {
        return primary2;
    }

    @Override
    protected ColorUIResource getPrimary3() {
        return primary3;
    }

    @Override
    protected ColorUIResource getSecondary1() {
        return secondary1;
    }

    @Override
    protected ColorUIResource getSecondary2() {
        return secondary2;
    }

    @Override
    protected ColorUIResource getSecondary3() {
        return secondary3;
    }

    @Override
    protected ColorUIResource getWhite() {
        return w;
    }

    @Override
    protected ColorUIResource getBlack() {
        return b;
    }

    @Override
    public String getName() {
        return "Praxis Theme";
    }

    @Override
    public void addCustomEntriesToTable(UIDefaults table) {
        super.addCustomEntriesToTable(table);
          table.put("Slider.horizontalThumbIcon", new HorizontalSliderThumbIcon());
          table.put("Slider.verticalThumbIcon", new VerticalSliderThumbIcon());
          table.put("Slider.horizontalSize", new DimensionUIResource(200, 36));
          table.put("Slider.verticalSize", new DimensionUIResource(36, 200));
          table.put("Slider.minimumHorizontalSize", new DimensionUIResource(42, 36));
          table.put("Slider.minimumVerticalSize", new DimensionUIResource(36, 42));
          table.put("Button.select", w);
          table.put("ToggleButton.select", w);
    }

    private static class HorizontalSliderThumbIcon implements Icon, UIResource {

        private int w = 24;
        private int h = 23;

        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.translate(x, y);
            g.setColor(c.isFocusOwner() ? c.getForeground().brighter()
                    : c.getForeground());
            g.fillRect(0, 1, w, h - 1);

            g.setColor(MetalLookAndFeel.getControlHighlight());
            g.drawLine(0, 1, w - 1, 1);
            g.drawLine(0, 1, 0, h);
            g.drawLine(w / 2, 2, w / 2, h - 1);

            g.setColor(MetalLookAndFeel.getControlDarkShadow());
            g.drawLine(0, h, w - 1, h);
            g.drawLine(w - 1, 1, w - 1, h);
            g.drawLine(w / 2 - 1, 2, w / 2 - 1, h);

            g.translate(-x, -y);
        }

        public int getIconWidth() {
            return w;
        }

        public int getIconHeight() {
            return h;
        }
    }

    private static class VerticalSliderThumbIcon implements Icon, UIResource {

        private int w = 23;
        private int h = 24;

        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.translate(x, y);
            g.setColor(c.isFocusOwner() ? c.getForeground().brighter()
                    : c.getForeground());

            g.fillRect(1, 0, w - 1, h);

            // highlight
            g.setColor(MetalLookAndFeel.getControlHighlight());
            g.drawLine(1, 0, w, 0);
            g.drawLine(1, 1, 1, h-1);
            g.drawLine(2, h/2, w-1, h/2);

            // shadow
            g.setColor(MetalLookAndFeel.getControlDarkShadow());
            g.drawLine(2, h-1, w, h-1);
            g.drawLine(w, h-1, w, 0);
            g.drawLine(2, h/2-1, w-1, h/2-1);

            g.translate(-x, -y);
        }

        public int getIconWidth() {
            return w;
        }

        public int getIconHeight() {
            return h;
        }
    }
}
