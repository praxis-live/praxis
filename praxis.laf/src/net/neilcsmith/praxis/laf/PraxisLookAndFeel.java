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
package net.neilcsmith.praxis.laf;

import java.awt.Color;
import javax.swing.UIDefaults;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

/**
 * Dark-themed Nimbus l&f
 *
 * @author Neil C Smith
 */
public class PraxisLookAndFeel extends NimbusLookAndFeel {
//

    private final static Color DARK_GREY = new ColorUIResource(Color.decode("#121212"));
    private final static Color MEDIUM_GREY = new ColorUIResource(Color.decode("#262626"));
    private final static Color LIGHT_GREY = new ColorUIResource(Color.decode("#AAAAAA"));
    private final static Color OFF_WHITE = new ColorUIResource(Color.decode("#DCDCDC"));
    private final static Color PRIMARY = new ColorUIResource(Color.decode("#4545A1"));
    private final static Color SECONDARY = new ColorUIResource(Color.decode("#5959B5"));
    
    private final static Color BASE = new ColorUIResource(Color.decode("#010105"));

    @Override
    public String getName() {
        return "Praxis";
    }
    
    @Override
    public UIDefaults getDefaults() {
        UIDefaults res = super.getDefaults();
        extendDefaults(res);
        return res;
    }

    private static void swap(UIDefaults def, String key1, String key2) {
        Object tmp = def.get(key1);
        def.put(key1, def.get(key2));
        def.put(key2, tmp);
    }
    
    private void extendDefaults(UIDefaults res) {

        // BEGIN :: Keep this section in sync with PraxisLookAndFeel
        res.put("control", DARK_GREY);
        res.put("info", Color.BLACK);// new Color(128,128,128) );
        res.put("nimbusBase", BASE);
        res.put("nimbusBlueGrey", MEDIUM_GREY);
////        res.put("nimbusBlueGrey", Color.decode("#4545a1"));
//        res.put("nimbusAlertYellow", new Color(248, 187, 0));
//        res.put("nimbusDisabledText", new Color(196, 196, 196));
        res.put("nimbusDisabledText", LIGHT_GREY);
        res.put("nimbusFocus", SECONDARY);
//        res.put("nimbusGreen", new Color(176, 179, 50));
        res.put("nimbusInfoBlue", MEDIUM_GREY);
        res.put("nimbusLightBackground", Color.BLACK);
//        res.put("nimbusOrange", new Color(191, 98, 4));
        res.put("nimbusOrange", PRIMARY);
//        res.put("nimbusRed", new Color(169, 46, 34));
        res.put("nimbusSelectedText", Color.WHITE);
        res.put("nimbusSelectionBackground", PRIMARY);
        res.put("text", OFF_WHITE);
        res.put("textForeground", OFF_WHITE);
        res.put("textText", OFF_WHITE);

        //  Menus
        SolidColorPainter primaryBG = new SolidColorPainter(PRIMARY);
        SolidColorPainter greyBG = new SolidColorPainter(MEDIUM_GREY);
        res.put("PopupMenu[Disabled].backgroundPainter", greyBG);
        res.put("PopupMenu[Enabled].backgroundPainter", greyBG);
        res.put("MenuBar:Menu[Enabled].textForeground", OFF_WHITE);
        res.put("Menu[Enabled].textForeground", OFF_WHITE);
        res.put("Menu[Disabled].textForeground", LIGHT_GREY);
        res.put("MenuItem[Enabled].textForeground", OFF_WHITE);
        res.put("MenuItem[Disabled].textForeground", LIGHT_GREY);
        res.put("CheckBoxMenuItem[Enabled].textForeground", OFF_WHITE);
        res.put("RadioButtonMenuItem[Enabled].textForeground", OFF_WHITE);
        res.put("Menu[Enabled].arrowIconPainter", res.get("Menu[Enabled+Selected].arrowIconPainter"));
        res.put("MenuBar:Menu[Selected].backgroundPainter", greyBG);
        res.put("Menu[Enabled+Selected].backgroundPainter", primaryBG);
        res.put("MenuItem[MouseOver].backgroundPainter", primaryBG);
        res.put("RadioButtonMenuItem[MouseOver].backgroundPainter", primaryBG);
        res.put("RadioButtonMenuItem[MouseOver+Selected].backgroundPainter", primaryBG);
        res.put("CheckBoxMenuItem[MouseOver].backgroundPainter", primaryBG);
        res.put("CheckBoxMenuItem[MouseOver+Selected].backgroundPainter", primaryBG);

        res.put("Slider:SliderThumb[Pressed].backgroundPainter",
                res.get("Slider:SliderThumb[MouseOver].backgroundPainter"));
        res.put("Slider:SliderThumb[Focused+Pressed].backgroundPainter",
                res.get("Slider:SliderThumb[Focused+MouseOver].backgroundPainter"));
        res.put("Slider:SliderThumb[MouseOver].backgroundPainter",
                res.get("Slider:SliderThumb[Focused].backgroundPainter"));
        
        res.put("Tree[Enabled].collapsedIconPainter", res.get("Tree[Enabled+Selected].collapsedIconPainter"));
        res.put("Tree[Enabled].expandedIconPainter", res.get("Tree[Enabled+Selected].expandedIconPainter"));

        res.put("Table[Enabled].textForeground", OFF_WHITE);
        res.put("Table[Enabled+Selected].textForeground", OFF_WHITE);

        res.put("TabbedPane:TabbedPaneTab[Enabled].backgroundPainter", null);
        res.put("TabbedPane:TabbedPaneTab[Disabled].backgroundPainter", null);
        
        res.put("ToggleButton[Selected].backgroundPainter", res.get("Button[Default].backgroundPainter"));
        res.put("ToggleButton[MouseOver+Selected].backgroundPainter", res.get("Button[Default+MouseOver].backgroundPainter"));
        res.put("ToggleButton[Focused+Selected].backgroundPainter", res.get("Button[Default+Focused].backgroundPainter"));
        res.put("ToggleButton[Focused+MouseOver+Selected].backgroundPainter", res.get("Button[Default+Focused+MouseOver].backgroundPainter"));
        res.put("ToolBar:ToggleButton[Selected].backgroundPainter", res.get("Button[Default].backgroundPainter"));
        res.put("ToolBar:ToggleButton[MouseOver+Selected].backgroundPainter", res.get("Button[Default+MouseOver].backgroundPainter"));
        res.put("ToolBar:ToggleButton[Focused+Selected].backgroundPainter", res.get("Button[Default+Focused].backgroundPainter"));
        res.put("ToolBar:ToggleButton[Focused+MouseOver+Selected].backgroundPainter", res.get("Button[Default+Focused+MouseOver].backgroundPainter"));

        res.put("ComboBox.selectionBackground", new Color(PRIMARY.getRGB()));
        
        // END :: Keep this section in sync with PraxisLookAndFeel


    }

}
