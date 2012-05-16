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

package net.neilcsmith.praxis.audio.components;

import java.net.URI;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.info.ArgumentInfo;
import net.neilcsmith.praxis.core.interfaces.TaskService;
import net.neilcsmith.praxis.core.interfaces.TaskService.Task;
import net.neilcsmith.praxis.core.types.PMap;
import net.neilcsmith.praxis.core.types.PReference;
import net.neilcsmith.praxis.core.types.PString;
import net.neilcsmith.praxis.core.types.PResource;
import net.neilcsmith.praxis.impl.AbstractAsyncProperty;
import net.neilcsmith.praxis.audio.io.SampleTable;

/**
 *
 * @author Neil C Smith
 */
public class SampleTableLoader extends AbstractAsyncProperty<SampleTable> {
    
    private Listener listener;
    
    public SampleTableLoader(Listener listener) {
        super(new ArgumentInfo[]{ArgumentInfo.create(PResource.class, PMap.EMPTY)},
                SampleTable.class,
                new Argument[]{PString.EMPTY},
                null);
        if (listener == null) {
            throw new NullPointerException();
        }
        this.listener = listener;
    }
    
    public SampleTable getTable() {
        return getValue();
    }

    @Override
    protected TaskService.Task createTask(CallArguments keys) throws Exception {
        return new LoaderTask(keys.get(0));
    }

    @Override
    protected void valueChanged(long time) {
        listener.tableLoaded(this, time);
    }

    @Override
    protected void taskError(long time) {
        listener.tableError(this, time);
    }


    
    
    
    private class LoaderTask implements Task {
        
        private Argument uri;
        
        private LoaderTask(Argument uri) {
            this.uri = uri;
        }

        public Argument execute() throws Exception {
            URI file = PResource.coerce(uri).value();
            SampleTable table = SampleTable.fromURL(file.toURL());
            return PReference.wrap(table);
        }
        
        
    }
    
    public static interface Listener {
        
        public void tableLoaded(SampleTableLoader loader, long time);
        
        public void tableError(SampleTableLoader loader, long time);
        
    }

}
