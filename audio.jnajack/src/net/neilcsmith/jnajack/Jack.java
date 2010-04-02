/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Neil C Smith. All rights reserved.
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
 * along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 *
 */
package net.neilcsmith.jnajack;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import net.neilcsmith.jnajack.lowlevel.JackLibrary;
import net.neilcsmith.jnajack.lowlevel.JackLibrary._jack_client;

/**
 * Main java wrapper to the Jack API. Loads the native library and provides methods
 * for creating clients and querying the server.
 *
 * Most functions from the native Jack API that manipulate clients or ports can
 * be found in JackClient and JackPort.
 *
 * This class is a singleton. Use Jack.getInstance()
 *
 * @author Neil C Smith
 */
public class Jack {

    private static Logger logger = Logger.getLogger(Jack.class.getName());
    private final static String CALL_ERROR_MSG = "Error calling native lib";
    private static Jack instance;
    private JackLibrary jackLib;
    private _jack_client clientPtr;

    private Jack(JackLibrary jackLib, _jack_client clientPtr) {
        this.jackLib = jackLib;
        this.clientPtr = clientPtr;
    }

    /**
     * Open an external client session with a JACK server.
     * @param name of at most <code>getMaximumClientNameSize()</code> characters.
     * The name scope is local to each server.  Unless forbidden by the
     * JackUseExactName option, the server will modify this name to
     * create a unique variant, if needed.
     * @param options EnumSet containing required JackOptions.
     * @param status EnumSet will be filled with JackStatus values from native call
     * @return JackClient
     * @throws JackException if client could not be opened. Check status set for reasons.
     */
    public JackClient openClient(String name, EnumSet<JackOptions> options, EnumSet<JackStatus> status)
            throws JackException {
        int opt = 0;
        // turn options into int
        if (options != null) {
            for (JackOptions option : options) {
                opt |= option.getIntValue();
            }
        }
        IntByReference statRef = new IntByReference(0);
        JackLibrary._jack_client cl = null;
        try {
            cl = jackLib.jack_client_open(name, opt, statRef);
        } catch (Throwable e) {
            logger.log(Level.SEVERE, CALL_ERROR_MSG, e);
            throw new JackException("Could not create Jack client", e);
        }
        // get status set from int
        int statVal = statRef.getValue();
        if (status != null) {
            status.clear();
            for (JackStatus stat : JackStatus.values()) {
                if ((stat.getIntValue() & statVal) != 0) {
                    status.add(stat);
                }
            }
        }

        if (cl == null) {
            throw new JackException("Could not create Jack client, check status set.");
        }

        if ((JackStatus.JackNameNotUnique.getIntValue() & statVal) != 0) {
            try {
                name = jackLib.jack_get_client_name(cl);
            } catch (Throwable e) {
                logger.log(Level.SEVERE, CALL_ERROR_MSG, e);
                try {
                    jackLib.jack_client_close(cl);
                } catch (Throwable e2) {
                }
                throw new JackException("Could not create Jack client, check status set.", e);
            }
        }

        return new JackClient(name, jackLib, cl);


    }

    /**
     * Currently defers to clientOpen(String name, EnumSet<JackOptions> options, EnumSet<JackStatus> status)
     *
     * Here for API completeness, but not yet supported.
     *
     * @param name
     * @param options
     * @param status
     * @param args
     * @return
     * @throws JackException
     */
    public JackClient openClient(String name, EnumSet<JackOptions> options, EnumSet<JackStatus> status, Object... args)
            throws JackException {
        return openClient(name, options, status);
    }

    /**
     * Get an array of port names that match the requested criteria.
     * @param regex A regular expression to match against the port names. If null or
     * of zero length then no filtering will be done.
     * @param type A JackPortType to filter results by. If null, the results will not
     * be filtered by type.
     * @param flags A set of JackPortFlags to filter results by. If the set is empty
     * or null then the results will not be filtered.
     * @return String[] of full port names.
     * @throws net.neilcsmith.jnajack.JackException
     */
    // @TODO need to free memory allocated by Jack
    public String[] getPorts(String regex, JackPortType type, EnumSet<JackPortFlags> flags)
            throws JackException {
        // don't pass regex String to native method. Invalid Strings can crash the VM
        int fl = 0;
        if (flags != null) {
            for (JackPortFlags flag : flags) {
                fl |= flag.getIntValue();
            }
        }
        String typeString = type == null ? null : type.getTypeString();
        try {
            Pointer ptr = jackLib.jack_get_ports(clientPtr, null,
                    typeString, new NativeLong(fl));
            if (ptr == null) {
                return new String[0];
            } else {
                String[] names = ptr.getStringArray(0);
                jackLib.free(ptr);
                if (regex != null && !regex.isEmpty()) {
                    names = filterRegex(names, regex);
                }
                return names;
            }
        } catch (Throwable e) {
            logger.log(Level.SEVERE, CALL_ERROR_MSG, e);
            throw new JackException(e);
        }


    }

    private String[] filterRegex(String[] names, String regex) {
        Pattern pattern = Pattern.compile(regex);
        ArrayList<String> list = new ArrayList<String>();
        for (String name : names) {
            if (pattern.matcher(name).find()) {
                list.add(name);
            }
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * Establish a connection between two ports.
     * When a connection exists, data written to the source port will
     * be available to be read at the destination port.
     * The port types must be identical.
     * The JackPortFlags of the source port must include
     * JackPortIsOutput.
     * The JackPortFlags of the destination port must include
     * JackPortIsInput.
     * @param source
     * @param destination
     * @throws JackException
     * @TODO this method fails if the jack server has been restarted as the client
     * no longer exists.
     */
    public void connect(String source, String destination)
            throws JackException {
        int ret = -1;
        try {
            ret = jackLib.jack_connect(clientPtr, source, destination);
        } catch (Throwable e) {
            logger.log(Level.SEVERE, CALL_ERROR_MSG, e);
            throw new JackException(e);
        }
        if (ret != 0) {
            throw new JackException();
        }
    }

    /**
     * Remove a connection between two ports.
     * @param source
     * @param destination
     * @throws JackException
     */
    public void disconnect(String source, String destination)
            throws JackException {
        int ret = -1;
        try {
            ret = jackLib.jack_disconnect(clientPtr, source, destination);
        } catch (Throwable e) {
            logger.log(Level.SEVERE, CALL_ERROR_MSG, e);
            throw new JackException(e);
        }
        if (ret != 0) {
            throw new JackException();
        }
    }

    /**
     * Get the maximum number of characters allowed in a JACK client name
     * @return maximum number of characters.
     * @throws JackException
     */
    public int getMaximumClientNameSize() throws JackException {
        try {
            return jackLib.jack_client_name_size() - 1;
        } catch (Throwable e) {
            logger.log(Level.SEVERE, CALL_ERROR_MSG, e);
            throw new JackException(e);
        }
    }

    /**
     * Get the maximum number of characters allowed in a JACK port name. This is
     * the full port name, prefixed by "client_name:".
     * @return maximum number of characters.
     * @throws net.neilcsmith.jnajack.JackException
     */
    public int getMaximumPortNameSize() throws JackException {
        try {
            return jackLib.jack_port_name_size() - 1;
        } catch (Throwable e) {
            logger.log(Level.SEVERE, CALL_ERROR_MSG, e);
            throw new JackException(e);
        }
    }

    /**
     * return JACK's current system time in microseconds using JACK clock source.
     *
     * The value returned is guaranteed to be monotonic, but not linear.
     * @return time
     * @throws net.neilcsmith.jnajack.JackException
     */
    public long getTime() throws JackException {
        try {
            return jackLib.jack_get_time().longValue();
        } catch (Throwable e) {
            logger.log(Level.SEVERE, CALL_ERROR_MSG, e);
            throw new JackException(e);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        try {
            jackLib.jack_client_close(clientPtr);
        } catch (Throwable e) {
            logger.log(Level.SEVERE, CALL_ERROR_MSG, e);
        }
    }

    // @TODO this is not in Jack 1 API - implement usable workaround.
//    public int[] getVersion() throws JackException {
//        try {
//            IntByReference major = new IntByReference();
//            IntByReference minor = new IntByReference();
//            IntByReference micro = new IntByReference();
//            IntByReference protocol = new IntByReference();
//            jackLib.jack_get_version(major, minor, micro, protocol);
//            int[] ret = new int[3];
//            ret[0] = major.getValue();
//            ret[1] = minor.getValue();
//            ret[2] = micro.getValue();
//            ret[3] = protocol.getValue();
//            return ret;
//        } catch (Throwable e) {
//            throw new JackException(e);
//        }
//    }
    /**
     * Get access to the single JNAJack Jack instance.
     *
     * @return Jack
     * @throws net.neilcsmith.jnajack.JackException if native library cannot be loaded.
     */
    public synchronized static Jack getInstance() throws JackException {
        if (instance != null) {
            return instance;
        }
        JackLibrary jackLib;
        JackLibrary._jack_client clientPtr;
        try {
            jackLib = (JackLibrary) Native.loadLibrary("jack", JackLibrary.class);
            clientPtr = jackLib.jack_client_open("__jnajack__",
                    JackLibrary.JackOptions.JackUseExactName | JackLibrary.JackOptions.JackNoStartServer,
                    null);

        } catch (Throwable e) {
            throw new JackException("Can't find native library", e);
        }
        if (clientPtr == null) {
            throw new JackException("Could not initialize JNAJack client");
        }
        instance = new Jack(jackLib, clientPtr);
        return instance;


    }
}
