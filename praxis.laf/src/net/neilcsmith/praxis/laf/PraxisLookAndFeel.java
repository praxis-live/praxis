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
import javax.swing.UIDefaults;

/**
 *
 * @author Neil C Smith
 */
public class PraxisLookAndFeel extends NimRODLookAndFeel {
    
    public PraxisLookAndFeel() {
        setCurrentTheme(new PraxisTheme());
    }

    @Override
    public String getID() {
        return "Praxis";
    }



    @Override
    protected void initComponentDefaults(UIDefaults table) {
        super.initComponentDefaults(table);
        table.put("ButtonUI", "net.neilcsmith.praxis.laf.PraxisButtonUI");
        table.put("ToggleButtonUI", "net.neilcsmith.praxis.laf.PraxisToggleButtonUI");
    }



}
