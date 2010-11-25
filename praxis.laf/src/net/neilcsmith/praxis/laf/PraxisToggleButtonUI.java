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

import com.nilo.plaf.nimrod.NimRODLookAndFeel;
import com.nilo.plaf.nimrod.NimRODToggleButtonUI;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.plaf.ComponentUI;

/**
 *
 * @author Neil C Smith
 */
public class PraxisToggleButtonUI extends NimRODToggleButtonUI {

    public static ComponentUI createUI(JComponent c) {
        return new PraxisToggleButtonUI();

    }

    @Override
    protected void paintFocus(Graphics g, AbstractButton b,
            Rectangle viewRect, Rectangle textRect, Rectangle iconRect) {
        if (!b.isFocusPainted() || !oldOpaque) {
            return;
        }
        if (b.getParent() instanceof JToolBar) {
            return;  // No se pintael foco cuando estamos en una barra
        }

        PraxisThemeUtils.paintFocus(g, 3, 3, b.getWidth() - 6, b.getHeight() - 6, 2, 2, 1, NimRODLookAndFeel.getFocusColor());
    }
}
