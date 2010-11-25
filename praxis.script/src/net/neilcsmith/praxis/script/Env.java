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

package net.neilcsmith.praxis.script;

import net.neilcsmith.praxis.core.ControlAddress;
import net.neilcsmith.praxis.core.Lookup;
import net.neilcsmith.praxis.core.PacketRouter;
import net.neilcsmith.praxis.core.interfaces.ServiceManager;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public interface Env {

    public final static String CONTEXT = "_CTXT";
    public final static String PWD = "_PWD";

    public abstract Lookup getLookup();

//    @Deprecated
//    public abstract ServiceManager getServiceManager();

    public abstract long getTime();

    public abstract PacketRouter getPacketRouter();

    public abstract ControlAddress getAddress();

}
