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
 *
 */
package net.neilcsmith.praxis.audio.code;

import java.io.IOException;
import java.net.URI;
import net.neilcsmith.praxis.audio.code.userapi.Table;
import net.neilcsmith.praxis.audio.io.AudioData;
import net.neilcsmith.praxis.code.ResourceProperty;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
class TableLoader extends ResourceProperty.Loader<Table> {

    private final static TableLoader INSTANCE = new TableLoader();
    
    private TableLoader() {
        super(Table.class);
    }
    
    @Override
    public Table load(URI uri) throws IOException {
        try {
            AudioData ad = AudioData.fromURL(uri.toURL());
            return Table.wrap(ad.data, (int)(ad.sampleRate + 0.5f), ad.channels);
        } catch (Exception ex) {
            if (ex instanceof IOException) {
                throw (IOException) ex;
            } else {
                throw new IOException(ex);
            }
        }
    }
    
    static TableLoader getDefault() {
        return INSTANCE;
    }
    
}
