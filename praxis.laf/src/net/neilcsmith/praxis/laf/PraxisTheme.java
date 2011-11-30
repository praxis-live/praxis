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

import com.nilo.plaf.nimrod.NimRODTheme;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.UIDefaults;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.DimensionUIResource;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.metal.MetalLookAndFeel;

/**
 *
 * @author Neil C Smith
 */
public class PraxisTheme extends NimRODTheme {

    private static Logger logger = Logger.getLogger(PraxisTheme.class.getName());

    // current theme
//    private ColorUIResource b = new ColorUIResource(Color.decode("#ffffff"));
//    private ColorUIResource w = new ColorUIResource(Color.decode("#181818"));
//    private ColorUIResource primary1 = new ColorUIResource(Color.decode("#a0a0a5"));
//    private ColorUIResource primary2 = new ColorUIResource(Color.decode("#77777a"));
//    private ColorUIResource primary3 = new ColorUIResource(Color.decode("#666667"));
//    private ColorUIResource secondary1 = new ColorUIResource(Color.decode("#666667"));
//    private ColorUIResource secondary2 = new ColorUIResource(Color.decode("#555556"));
//    private ColorUIResource secondary3 = new ColorUIResource(Color.decode("#39393a"));

//
//    @Override
//    protected ColorUIResource getPrimary1() {
//        return primary1;
//    }
//
//    @Override
//    protected ColorUIResource getPrimary2() {
//        return primary2;
//    }
//
//    @Override
//    protected ColorUIResource getPrimary3() {
//        return primary3;
//    }
//
//    @Override
//    protected ColorUIResource getSecondary1() {
//        return secondary1;
//    }
//
//    @Override
//    protected ColorUIResource getSecondary2() {
//        return secondary2;
//    }
//
//    @Override
//    protected ColorUIResource getSecondary3() {
//        return secondary3;
//    }
//
//    @Override
//    protected ColorUIResource getWhite() {
//        return w;
//    }
//
//    @Override
//    protected ColorUIResource getBlack() {
//        return b;
//    }
    public PraxisTheme() {
        URL theme = getClass().getResource("default.theme");
        if (theme != null) {
            logger.fine("Praxis LAF - theme file found");
            Properties props = new Properties();
            InputStream res = null;
            try {
                res = theme.openStream();
                props.load(res);
                initFromProps(props);
                res.close();
            } catch (Exception ex) {
            }

        }
    }

    private void initFromProps(Properties props) {
        logger.fine("Praxis LAF - loading colours from theme file");
        setPrimary1(Color.decode(props.getProperty("praxis.p1")));
        setPrimary2(Color.decode(props.getProperty("praxis.p2")));
        setPrimary3(Color.decode(props.getProperty("praxis.p3")));

        setSecondary1(Color.decode(props.getProperty("praxis.s1")));
        setSecondary2(Color.decode(props.getProperty("praxis.s2")));
        setSecondary3(Color.decode(props.getProperty("praxis.s3")));

        setWhite(Color.decode(props.getProperty("praxis.w")));
        setBlack(Color.decode(props.getProperty("praxis.b")));

        setMenuOpacity(Integer.parseInt(props.getProperty("praxis.menuOpacity")));
        setFrameOpacity(Integer.parseInt(props.getProperty("praxis.frameOpacity")));

    }

    @Override
    public String getName() {
        return "Praxis Theme";
    }

    @Override
    public void addCustomEntriesToTable(UIDefaults table) {
        super.addCustomEntriesToTable(table);
        
//        table.put("Slider.horizontalThumbIcon", new HorizontalSliderThumbIcon());
//        table.put("Slider.verticalThumbIcon", new VerticalSliderThumbIcon());
//        table.put("Slider.horizontalSize", new DimensionUIResource(200, 36));
//        table.put("Slider.verticalSize", new DimensionUIResource(36, 200));
//        table.put("Slider.minimumHorizontalSize", new DimensionUIResource(42, 36));
//        table.put("Slider.minimumVerticalSize", new DimensionUIResource(36, 42));
//          table.put("Button.select", w);
//          table.put("ToggleButton.select", w);
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
            g.drawLine(1, 1, 1, h - 1);
            g.drawLine(2, h / 2, w - 1, h / 2);

            // shadow
            g.setColor(MetalLookAndFeel.getControlDarkShadow());
            g.drawLine(2, h - 1, w, h - 1);
            g.drawLine(w, h - 1, w, 0);
            g.drawLine(2, h / 2 - 1, w - 1, h / 2 - 1);

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
