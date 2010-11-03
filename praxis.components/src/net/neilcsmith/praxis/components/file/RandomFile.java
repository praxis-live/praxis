/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 - Neil C Smith. All rights reserved.
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

package net.neilcsmith.praxis.components.file;

import java.util.Random;
import net.neilcsmith.praxis.core.ControlPort;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.core.types.PArray;
import net.neilcsmith.praxis.core.types.PString;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.impl.AbstractRoot;
import net.neilcsmith.praxis.impl.DefaultControlOutputPort;
import net.neilcsmith.praxis.impl.ResourceListLoader;
import net.neilcsmith.praxis.impl.TriggerControl;

/**
 *
 * @author Neil C Smith
 * @TODO reimplement file listener when timecode added to resource loader
 */
public class RandomFile extends AbstractComponent {
    
    private ControlPort.Output output;
    private PArray files;
    private Random random;
    private ControlPort.Output rdyPort;
    private ControlPort.Output errPort;
    
    public RandomFile() {
        files = PArray.EMPTY;
        random = new Random();
        ResourceListLoader fileControl = ResourceListLoader.create(this, new FilesListener());
        registerControl("directory", fileControl);
        registerPort("directory", fileControl.getInputPort());
        TriggerControl trigger = TriggerControl.create( new TriggerBinding());
        registerControl("trigger", trigger);
        registerPort("trigger", trigger.createPort());
        output = new DefaultControlOutputPort(this);
        registerPort(Port.OUT, output);
        rdyPort = new DefaultControlOutputPort(this);
        registerPort("ready", rdyPort);
        errPort = new DefaultControlOutputPort(this);
        registerPort("error", errPort);
//        registerPort("ready", fileControl.getCompletePort());
//        registerPort("error", fileControl.getErrorPort());
    }
    
    private class FilesListener implements ResourceListLoader.Listener {

        public void listLoaded(ResourceListLoader source) {
            setResourceList(source.getList());
            rdyPort.send( ((AbstractRoot) getRoot()).getTime());
        }

        public void listError(ResourceListLoader source) {
            errPort.send(((AbstractRoot) getRoot()).getTime()); // @replace with timecode sent to message
        }
        
        

        public void setResourceList(PArray list) {
            if (list == null) {
                list = PArray.EMPTY;
            }
            System.out.println("Resource set to " + list);
            files = list;
        }
        
    }
    
    private class TriggerBinding implements TriggerControl.Binding {

        public void trigger(long time) {
            int count = files.getSize();
            if (count == 0) {
                output.send(time, PString.EMPTY);
            } else if (count == 1) {
                output.send(time, files.get(0));
            } else {
                int index = random.nextInt(count);
                output.send(time, files.get(index));
            }
        }
        
    }

}
