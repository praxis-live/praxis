/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.neilcsmith.praxis.player;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.IllegalRootStateException;
import net.neilcsmith.praxis.hub.TaskServiceImpl;
import net.neilcsmith.praxis.hub.DefaultHub;
import net.neilcsmith.praxis.laf.PraxisLAFManager;
import net.neilcsmith.praxis.script.ScriptServiceImpl;

/**
 *
 * @author Neil C Smith
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {


        
        try {
            PraxisLAFManager.getInstance().installUI();
            DefaultHub hub = new DefaultHub(new ScriptServiceImpl(), new TaskServiceImpl(), new Player());
            hub.activate();
        } catch (IllegalRootStateException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
