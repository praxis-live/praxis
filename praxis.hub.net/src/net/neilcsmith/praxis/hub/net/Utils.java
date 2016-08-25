
package net.neilcsmith.praxis.hub.net;

import java.io.File;
import java.net.URI;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
class Utils {
    
    private Utils() {}
    
    final static String KEY_MASTER_USER_DIRECTORY = "master-user-directory";
    final static String KEY_FILE_SERVER_ADDRESS = "file-server-address";
    final static String KEY_REMOTE_SERVICES = "remote-services";
        
    private final static URI USER_DIRECTORY = new File("").toURI();
    
    static URI getUserDirectory() {
        return USER_DIRECTORY;
    }
    
}
