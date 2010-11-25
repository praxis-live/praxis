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
 *
 */
package net.neilcsmith.praxis.midi.components;

import java.util.ArrayList;
import java.util.List;
import javax.sound.midi.ShortMessage;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ControlAddress;
import net.neilcsmith.praxis.core.ArgumentFormatException;
import net.neilcsmith.praxis.core.Call;
import net.neilcsmith.praxis.core.PacketRouter;
import net.neilcsmith.praxis.core.syntax.Token;
import net.neilcsmith.praxis.core.syntax.Tokenizer;
import net.neilcsmith.praxis.core.types.PNumber;

/**
 *
 * @author Neil C Smith
 */
class ControlMapping {

    private ControlAddress returnAddress;
    private Entry[] entries;

    private ControlMapping(Entry[] entries, ControlAddress returnAddress)
            throws ArgumentFormatException {
        this.entries = entries;
        this.returnAddress = returnAddress;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("Control Mapping\n");
        for (int i = 0; i < entries.length; i++) {
            Entry entry = entries[i];
            while (entry != null) {
                builder.append("ENTRY -- Channel : " + (i / 128) + " Controller : " +
                        (i % 128) + " Address : " + entry.address + " Min : " +
                        entry.minimum + " Max : " + entry.maximum + "\n");
                entry = entry.next;
            }
        }
        return builder.toString();
    }

    void sendCalls(PacketRouter router, ShortMessage message, long nanoTime) {
        int channel = message.getChannel();
        int controller = message.getData1();
        int index = (channel * 128) + controller;
        Entry entry = entries[index];
        while (entry != null) {
            Argument arg = parseArgument(entry, message);
            Call call = Call.createQuietCall(entry.address, returnAddress, nanoTime, arg);
            router.route(call);
            entry = entry.next;
        }
    }

    private Argument parseArgument(Entry entry, ShortMessage message) {
        int iVal = message.getData2();
        double min = entry.minimum;
        double max = entry.maximum;
        if (min == 0) {
            if (max == 127) {
                return PNumber.valueOf(iVal);
            } else {
                double val = (iVal / 127.0) * max;
                return PNumber.valueOf(val);
            }
        } else {
            double val = ((iVal / 127.0) * (max - min)) + min;
            return PNumber.valueOf(val);
        }
    }

    private static class Entry {

        ControlAddress address;
        double minimum;
        double maximum;
        Entry next;
    }

    static ControlMapping create(String mappings, ControlAddress returnAddress) throws ArgumentFormatException {

        String[][] ast = parseSyntaxTree(mappings);
        Entry[] entries = createEntries(ast);
        return new ControlMapping(entries, returnAddress);
    }

    private static String[][] parseSyntaxTree(String mappings) throws ArgumentFormatException {
        Tokenizer tkz = new Tokenizer(mappings);
        String[] strArray = new String[0];
        List<String[]> lines = new ArrayList<String[]>();
        List<String> current = new ArrayList<String>();
        for (Token tok : tkz) {
            switch (tok.getType()) {
                case PLAIN:
                case QUOTED:
                case BRACED:
                    current.add(tok.getText());
                    break;
                case EOL:
                    if (current.size() > 0) {
                        lines.add(current.toArray(strArray));
                        current.clear();
                    }
                    break;
                case SUBCOMMAND:
                    throw new ArgumentFormatException();
                default:
                    break;

            }
        }
        return lines.toArray(new String[][]{strArray});

    }

    private static Entry[] createEntries(String[][] ast) throws ArgumentFormatException {
        Entry[] entries = new Entry[16 * 128];
        for (String[] line : ast) {
            if (line.length < 3) {
                throw new ArgumentFormatException();
            }
            int channel = parseChannel(line[0]);
            int controller = parseControl(line[1]);

            ControlAddress address = parseAddress(line[2]);
            double minimum = 0;
            if (line.length > 3) {
                minimum = parseDouble(line[3]);
            }
            double maximum = 127;
            if (line.length > 4) {
                maximum = parseDouble(line[4]);
            }
            Entry entry = new Entry();
            entry.address = address;
            entry.minimum = minimum;
            entry.maximum = maximum;

            int index = (channel * 128) + controller;
            Entry existing = entries[index];
            if (existing == null) {
                entries[index] = entry;
            } else {
                while (existing.next != null) {
                    existing = existing.next;
                }
                existing.next = entry;
            }
        }
        return entries;
    }

    private static int parseChannel(String str) throws ArgumentFormatException {
        if (str.startsWith("ch:")) {
            try {
                int ch = Integer.parseInt(str.substring(3)) - 1;
                if (ch > 15 || ch < 0) {
                    throw new ArgumentFormatException();
                }
                return ch;
            } catch (NumberFormatException ex) {
            }
        }
        throw new ArgumentFormatException();
    }

    private static int parseControl(String str) throws ArgumentFormatException {
        if (str.startsWith("cc:")) {
            try {
                int cc = Integer.parseInt(str.substring(3));
                if (cc < 0 || cc > 127) {
                    throw new ArgumentFormatException();
                }
                return cc;
            } catch (NumberFormatException ex) {
            }
        }
        throw new ArgumentFormatException();
    }

    private static ControlAddress parseAddress(String str) throws ArgumentFormatException {
        try {
            return ControlAddress.valueOf(str);
        } catch (ArgumentFormatException ex) {
            throw new ArgumentFormatException(ex);
        }
    }

    private static double parseDouble(String str) throws ArgumentFormatException {
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException ex) {
            throw new ArgumentFormatException(ex);
        }
    }
}
