/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2013 Neil C Smith.
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
package net.neilcsmith.praxis.video.components;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.IllegalRootStateException;
import net.neilcsmith.praxis.core.Lookup;
import net.neilcsmith.praxis.core.info.ArgumentInfo;
import net.neilcsmith.praxis.core.types.PArray;
import net.neilcsmith.praxis.core.types.PMap;
import net.neilcsmith.praxis.core.types.PString;
import net.neilcsmith.praxis.impl.AbstractRoot;
import net.neilcsmith.praxis.impl.ArgumentProperty;
import net.neilcsmith.praxis.impl.BooleanProperty;
import net.neilcsmith.praxis.impl.InstanceLookup;
import net.neilcsmith.praxis.impl.IntProperty;
import net.neilcsmith.praxis.impl.NumberProperty;
import net.neilcsmith.praxis.impl.RootState;
import net.neilcsmith.praxis.video.ClientConfiguration;
import net.neilcsmith.praxis.video.ClientRegistrationException;
import net.neilcsmith.praxis.video.Player;
import net.neilcsmith.praxis.video.PlayerConfiguration;
import net.neilcsmith.praxis.video.PlayerFactory;
import net.neilcsmith.praxis.video.QueueContext;
import net.neilcsmith.praxis.video.VideoContext;
import net.neilcsmith.praxis.video.VideoSettings;
import net.neilcsmith.praxis.video.WindowHints;
import net.neilcsmith.praxis.video.pipes.FrameRateListener;
import net.neilcsmith.praxis.video.pipes.FrameRateSource;

/**
 *
 * @author Neil C Smith
 */
public class DefaultVideoRoot extends AbstractRoot implements FrameRateListener {
    
    private final static Logger LOG = Logger.getLogger(DefaultVideoRoot.class.getName());

    private final static int WIDTH_DEFAULT = 640;
    private final static int HEIGHT_DEFAULT = 480;
    private final static double FPS_DEFAULT = 24;
    private final static boolean FULL_SCREEN_DEFAULT = false;
    private int skipcount;
    private int width = WIDTH_DEFAULT;
    private int height = HEIGHT_DEFAULT;
    private double fps = FPS_DEFAULT;
    private boolean fullScreen = FULL_SCREEN_DEFAULT;
    private ArgumentProperty renderer;
//    private String title;
    private Player player;
//    private Placeholder placeholder;
//    private OutputServer outputServer;
    private VideoContext.OutputClient outputClient;
    private VideoContextImpl ctxt;
    private Lookup lookup;

    public DefaultVideoRoot() {
//        placeholder = new Placeholder();
        buildControls();
    }

    private void buildControls() {
        renderer = ArgumentProperty.create(
                ArgumentInfo.create(PString.class,
                PMap.create(ArgumentInfo.KEY_SUGGESTED_VALUES, PArray.valueOf(PString.valueOf("Software"), PString.valueOf("OpenGL")))));
        registerControl("renderer", renderer);
        registerControl("width", IntProperty.create(new WidthBinding(), 1, 2048, width));
        registerControl("height", IntProperty.create(new HeightBinding(), 1, 2048, height));
        registerControl("fps", NumberProperty.create(new FpsBinding(), 1, 100, fps));
        registerControl("full-screen", BooleanProperty.create(this, new FullScreenBinding(), fullScreen));
        ctxt = new VideoContextImpl();
    }

    @Override
    public Lookup getLookup() {
        if (lookup == null) {
            lookup = InstanceLookup.create(super.getLookup(), ctxt);
        }
        return lookup;
    }

    public void nextFrame(FrameRateSource source) {
        try {
            if (!source.isRendering()) {
                skipcount++;
//                System.out.println("Frame skipped " + skipcount);
            }
            nextControlFrame(source.getTime());
        } catch (IllegalRootStateException ex) {
            // @TODO remove source
            player.terminate();
        }
    }

    @Override
    protected void starting() {
        try {
            String lib = renderer.getValue().toString();
            if (lib.isEmpty()) {
                lib = VideoSettings.getRenderer();
            }
            player = createPlayer(lib);
            player.addFrameRateListener(this);
            if (outputClient != null && outputClient.getOutputCount() > 0) {
                player.getSink(0).addSource(outputClient.getOutputSource(0));
            }
            setInterrupt(new Runnable() {
                public void run() {
                    player.run();
                    try {
                        setIdle();
                    } catch (IllegalRootStateException ex) {
                        // ignore - state already changed?
                    }
                }
            });
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Couldn't start video renderer", ex);
            try {
                setIdle();
            } catch (IllegalRootStateException ex1) {
                // ignore - state already changed?
            }
        }
    }

    private Player createPlayer(String library) throws Exception {
        String title = "PRAXIS : " + getAddress();
//        int outWidth = width;
//        int outHeight = height;
//        int rotation = 0;
        Lookup clientLookup = Lookup.EMPTY;
        if (outputClient != null) {
            clientLookup = outputClient.getLookup();
//            ClientConfiguration.Dimension dim = clientLookup.get(ClientConfiguration.Dimension.class);
//            if (dim != null) {
//                outWidth = dim.getWidth();
//                outHeight = dim.getHeight();
//            }
//            ClientConfiguration.Rotation rot = clientLookup.get(ClientConfiguration.Rotation.class);
//            if (rot != null) {
//                rotation = rot.getAngle();
//            }
        }
        PlayerFactory factory = findPlayerFactory(library);
        WindowHints wHints = new WindowHints();
        wHints.setTitle(title);
        wHints.setFullScreen(fullScreen);
        Lookup clLkp = InstanceLookup.create(clientLookup, wHints);
        Lookup plLkp = InstanceLookup.create(new QueueContextImpl());
        return factory.createPlayer(new PlayerConfiguration(width, height, fps, plLkp),
                new ClientConfiguration[]{
                    new ClientConfiguration(0, 1, clLkp)
                });
    }

    private PlayerFactory findPlayerFactory(String lib) throws Exception {
        if (lib == null || lib.isEmpty() || "Software".equals(lib)) {
            return SWPlayer.getFactory();
        }
        for (PlayerFactory.Provider provider : Lookup.SYSTEM.getAll(PlayerFactory.Provider.class)) {
            if (provider.getLibraryName().equals(lib)) {
                return provider.getFactory();
            }
        }
        throw new IllegalArgumentException("No valid renderer found");
    }

    @Override
    protected void stopping() {
//        setInterrupt(new Runnable() {
//            public void run() {
////                player.getOutputSink().removeSource(placeholder);
//                player.terminate();
//            }
//        });
        player.terminate();
        interrupt();


    }

    private class VideoContextImpl extends VideoContext {

        public int registerVideoInputClient(InputClient client) throws ClientRegistrationException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void unregisterVideoInputClient(InputClient client) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int registerVideoOutputClient(OutputClient client) throws ClientRegistrationException {
            if (outputClient == null) {
                outputClient = client;
                return 1;
            } else {
                throw new ClientRegistrationException();
            }
        }

        // @TODO should not allow while running!
        public void unregisterVideoOutputClient(OutputClient client) {
            if (outputClient == client) {
                outputClient = null;
            }
        }
    }

    private class QueueContextImpl implements QueueContext {

        public void process(long time, TimeUnit unit) throws InterruptedException {
            poll(time, unit);
        }
    }

    private class WidthBinding implements IntProperty.Binding {

        public void setBoundValue(long time, int value) {
            if (getState() == RootState.ACTIVE_RUNNING) {
                throw new UnsupportedOperationException("Can't set width while running");
            }
            width = value;
        }

        public int getBoundValue() {
            return width;
        }
    }

    private class HeightBinding implements IntProperty.Binding {

        public void setBoundValue(long time, int value) {
            if (getState() == RootState.ACTIVE_RUNNING) {
                throw new UnsupportedOperationException("Can't set height while running");
            }
            height = value;
        }

        public int getBoundValue() {
            return height;
        }
    }

    private class FpsBinding implements NumberProperty.Binding {

        public void setBoundValue(long time, double value) {
            if (getState() == RootState.ACTIVE_RUNNING) {
                throw new UnsupportedOperationException("Can't set fps while running");
            }
            fps = value;
        }

        public double getBoundValue() {
            return fps;
        }
    }

    private class FullScreenBinding implements BooleanProperty.Binding {

        public void setBoundValue(long time, boolean value) {
            if (getState() == RootState.ACTIVE_RUNNING) {
                throw new UnsupportedOperationException("Can't set full screen state while running");
            }
            fullScreen = value;
        }

        public boolean getBoundValue() {
            return fullScreen;
        }
    }
}
