/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.neilcsmith.praxis.video.gstreamer;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import net.neilcsmith.praxis.video.VideoDelegateFactory;
import net.neilcsmith.praxis.video.VideoDelegateFactoryProvider;

/**
 *
 * @author Neil C Smith
 */
public class GStreamerFactoryProvider implements VideoDelegateFactoryProvider {
    
    private static String[] schemes = { "file", "http", "v4l", "v4l2", "ipcam", "dv1394"};

    public Set<String> getSupportedSchemes() {
        return new LinkedHashSet<String>(Arrays.asList(schemes)); 
    }

    public String getLibraryName() {
        return "gstreamer";
    }

    public VideoDelegateFactory getFactory() {
        return GStreamerFactory.getInstance();
    }

}
