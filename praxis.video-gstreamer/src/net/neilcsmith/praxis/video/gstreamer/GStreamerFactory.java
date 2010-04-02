/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.neilcsmith.praxis.video.gstreamer;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.video.InvalidVideoResourceException;
import net.neilcsmith.praxis.video.VideoDelegateFactory;
import net.neilcsmith.ripl.delegates.VideoDelegate;
import net.neilcsmith.ripl.gstreamer.DV1394Delegate;
import net.neilcsmith.ripl.gstreamer.IPCamDelegate;
import net.neilcsmith.ripl.gstreamer.PlaybinDelegate;
import net.neilcsmith.ripl.gstreamer.V4LDelegate;

/**
 *
 * @author Neil C Smith
 */
public class GStreamerFactory implements VideoDelegateFactory {

    private static GStreamerFactory instance = new GStreamerFactory();

    private GStreamerFactory() {
    // singleton
    }

    public static GStreamerFactory getInstance() {
        return instance;
    }

    public VideoDelegate create(URI resource) throws InvalidVideoResourceException, InstantiationException {
        String scheme = resource.getScheme();
        if (scheme.equals("file") || scheme.equals("http")) {
            return createPlaybinDelegate(resource);
        } else if (scheme.equals("v4l") || scheme.equals("v4l2")) {
            return createV4LDelegate(resource);
        } else if (scheme.equals("ipcam")) {
            return createIPCamDelegate(resource);
        } else if (scheme.equals("dv1394")) {
            return createDV1394Delegate(resource);
        }
        throw new InvalidVideoResourceException();
    }

    private VideoDelegate createDV1394Delegate(URI resource) {
        return DV1394Delegate.create(resource);
    }

    private VideoDelegate createPlaybinDelegate(URI resource) {
        return PlaybinDelegate.create(resource);
    }

    private VideoDelegate createIPCamDelegate(URI resource) {
        if (resource.getScheme().equals("ipcam")) {
            try {
                resource = new URI("http", resource.getSchemeSpecificPart(), resource.getFragment());
            } catch (URISyntaxException ex) {
                Logger.getLogger(GStreamerFactory.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return IPCamDelegate.create(resource);
    }
    
    private VideoDelegate createV4LDelegate(URI resource) {
        return V4LDelegate.create(resource);
    }
}
