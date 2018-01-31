
package org.praxislive.hub.net;

import java.io.File;
//import java.io.IOException;
//import java.net.ServerSocket;
//import java.util.logging.Level;
//import java.util.logging.Logger;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
class Utils {
    
    private Utils() {}
    
    final static String KEY_MASTER_USER_DIRECTORY = "master-user-directory";
    final static String KEY_FILE_SERVER_PORT = "file-server-port";
    final static String KEY_REMOTE_SERVICES = "remote-services";
        
    private final static File USER_DIRECTORY = new File(System.getProperty("user.home", "")).getAbsoluteFile();
    // @TODO make user configurable.
    private final static int FILE_SERVER_PORT = Integer.getInteger("praxis.file.server.port", 26356);
    
    static File getUserDirectory() {
        return USER_DIRECTORY;
    }
    
    static int getFileServerPort() {
//        try (ServerSocket socket = new ServerSocket(0)) {
//            socket.setReuseAddress(true);
//            return socket.getLocalPort();
//        } catch (IOException ex) {
//            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
            return FILE_SERVER_PORT;
//        }
    }
    
}
