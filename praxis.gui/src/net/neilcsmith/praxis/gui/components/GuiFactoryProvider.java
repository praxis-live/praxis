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
package net.neilcsmith.praxis.gui.components;

import net.neilcsmith.praxis.core.ComponentFactory;
import net.neilcsmith.praxis.core.ComponentFactoryProvider;
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
//            addComponent("gui:h-panel", HPanel.class);
//            addComponent("gui:v-panel", VPanel.class);
            addComponent("gui:panel", Panel.class);
            addComponent("gui:tabs", Tabs.class);

        }
    }
}
