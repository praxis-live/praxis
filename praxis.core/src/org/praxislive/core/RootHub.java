/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2019 Neil C Smith.
 * 
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * version 3 for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License version 3
 * along with this work; if not, see http://www.gnu.org/licenses/
 * 
 * 
 * Please visit https://www.praxislive.org if you need additional information or
 * have any questions.
 */
package org.praxislive.core;

import java.util.List;
import org.praxislive.core.services.Service;

/**
 * RootHub implementations act as a container for Roots, providing a way for
 * Roots to communicate with each other and access other hub-wide services.
 *
 * The Lookup provided by the RootHub will usually contain an instance of
 * {@link org.praxislive.core.services.Services}.
 *
 * A RootHub implementation is local to a JVM. In a distributed hub there will
 * be a RootHub on each running process. Dispatching of messages between processes
 * will be handled transparently using the dispatch() method. Other information,
 * such as the Clock time, are specific to the RootHub instance (comparisons
 * across RootHubs are not useful). Packet timecodes will be automatically
 * translated.
 *
 * @author Neil C Smith
 */
public interface RootHub extends Lookup.Provider {

    /**
     * Dispatch a message to another Root.
     *
     * See {@link Root.Controller#submitPacket(org.praxislive.core.Packet)
     *
     * @param packet message to dispatch
     * @return true if message could be handled
     */
    public boolean dispatch(Packet packet);

    /**
     * The Clock instance for this RootHub. This method will return a high-precision
     * nanosecond timer used as the primary timing source within this RootHub
     * instance.  This method will always return the same Clock instance.
     *
     * @return the primary timing source of this RootHub
     */
    public Clock getClock();

    /**
     * An interface that can be registered as a service provider for modules
     * wanting to provide Root instances to be installed as RootHub extensions -
     * usually for Roots providing additional Services.
     */
    public static interface ExtensionProvider {

        /**
         * List of extensions to be installed.
         * @return list of extensions
         */
        public List<Root> getExtensions();

    }

    /**
     * An interface for RootHub extensions (see {@link ExtensionProvider}) to
     * advertise the services they provide. Use of {@link Component#getInfo()}
     * for this purpose should be considered deprecated.
     * 
     * The Root itself must provide the advertised services. Support for root
     * containers to provide services via child components is not yet supported.
     *
     * @see Service
     */
    public static interface ServiceProvider extends Root {

        // ideally this would be part of the ExtensionProvider itself, but breaks
        // too many assumptions in the API
        /**
         * A list of the services this extension provides. This method will be
         * called as the extension Root is installed in the RootHub.
         *
         * @return list of services
         */
        public List<Class<? extends Service>> services();

//        public default ComponentAddress resolve(ComponentAddress rootAddress,
//                Class<? extends Service> service) {
//            return rootAddress;
//        }
    }
}
