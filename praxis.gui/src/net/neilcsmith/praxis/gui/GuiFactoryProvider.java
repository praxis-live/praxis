/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 - Neil C Smith. All rights reserved.
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
package net.neilcsmith.praxis.gui;

import net.neilcsmith.praxis.core.ComponentFactory;
import net.neilcsmith.praxis.core.ComponentFactoryProvider;
import net.neilcsmith.praxis.gui.components.Button;
import net.neilcsmith.praxis.gui.components.FileField;
import net.neilcsmith.praxis.gui.components.HPanel;
import net.neilcsmith.praxis.gui.components.HRangeSlider;
import net.neilcsmith.praxis.gui.components.HSlider;
import net.neilcsmith.praxis.gui.components.Tabs;
import net.neilcsmith.praxis.gui.components.ToggleButton;
import net.neilcsmith.praxis.gui.components.VPanel;
import net.neilcsmith.praxis.gui.components.VRangeSlider;
import net.neilcsmith.praxis.gui.components.VSlider;
import net.neilcsmith.praxis.gui.components.XYController;
import net.neilcsmith.praxis.impl.AbstractComponentFactory;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class GuiFactoryProvider implements ComponentFactoryProvider {

    private final static ComponentFactory factory = new Factory();

    public ComponentFactory getFactory() {
        return factory;
    }

    private static class Factory extends AbstractComponentFactory {

        private Factory() {
            build();
        }

        private void build() {
            // ROOT
            addRoot("root:gui", DefaultGuiRoot.class);

            // COMPONENTS
            addComponent("gui:h-slider", HSlider.class);
            addComponent("gui:v-slider", VSlider.class);
            addComponent("gui:h-rangeslider", HRangeSlider.class);
            addComponent("gui:v-rangeslider", VRangeSlider.class);
            addComponent("gui:button", Button.class);
            addComponent("gui:togglebutton", ToggleButton.class);
            addComponent("gui:xy-pad", XYController.class);
            addComponent("gui:filefield", FileField.class);
            // GUI containers
            addComponent("gui:h-panel", HPanel.class);
            addComponent("gui:v-panel", VPanel.class);
            addComponent("gui:tabs", Tabs.class);

        }
    }
}
