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
package net.neilcsmith.praxis.video.components;

import java.util.Collections;
import net.neilcsmith.praxis.core.IllegalRootStateException;
import net.neilcsmith.praxis.core.Lookup;
import net.neilcsmith.praxis.impl.AbstractRoot;
import net.neilcsmith.praxis.impl.BooleanProperty;
import net.neilcsmith.praxis.impl.FloatProperty;
import net.neilcsmith.praxis.impl.InstanceLookup;
import net.neilcsmith.praxis.impl.IntProperty;
import net.neilcsmith.praxis.impl.RootState;
import net.neilcsmith.praxis.settings.Settings;
import net.neilcsmith.praxis.video.ClientConfiguration;
import net.neilcsmith.praxis.video.ClientRegistrationException;
import net.neilcsmith.praxis.video.Player;
import net.neilcsmith.praxis.video.PlayerConfiguration;
import net.neilcsmith.praxis.video.PlayerFactory;
import net.neilcsmith.praxis.video.VideoContext;
import net.neilcsmith.praxis.video.VideoSettings;
import net.neilcsmith.praxis.video.pipes.FrameRateListener;
import net.neilcsmith.praxis.video.pipes.FrameRateSource;

/**
 *
 * @author Neil C Smith
 */
public class DefaultVideoRoot extends AbstractRoot implements FrameRateListener {

    private final static int WIDTH_DEFAULT = 640;
    private final static int HEIGHT_DEFAULT = 480;
    private final static double FPS_DEFAULT = 24;
    private final static boolean FULL_SCREEN_DEFAULT = false;
    private int skipcount;
    private int width = WIDTH_DEFAULT;
    private int height = HEIGHT_DEFAULT;
    private double fps = FPS_DEFAULT;
    private boolean fullScreen = FULL_SCREEN_DEFAULT;
//    private String title;
    private Player player;
//    private Placeholder placeholder;
//    private OutputServer outputServer;
    private VideoContext.OutputClient outputClient;
    private Context ctxt;
    private Lookup lookup;

    public DefaultVideoRoot() {
//        placeholder = new Placeholder();
        buildControls();
    }

    private void buildControls() {
        registerControl("width", IntProperty.create(new WidthBinding(), 1, 2048, width));
        registerControl("height", IntProperty.create(new HeightBinding(), 1, 2048, height));
        registerControl("fps", FloatProperty.create(new FpsBinding(), 1, 100, fps));
        registerControl("full-screen", BooleanProperty.create(this, new FullScreenBinding(), fullScreen));
        ctxt = new Context();
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

            player = createPlayer(VideoSettings.getRenderer());
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
        }
    }

    private Player createPlayer(String library) {
        String title = "PRAXIS : " + getAddress();
        int outWidth = width;
        int outHeight = height;
        int rotation = 0;
        if (outputClient != null) {
            Object w = outputClient.getClientHint(ClientConfiguration.CLIENT_KEY_WIDTH);
            Object h = outputClient.getClientHint(ClientConfiguration.CLIENT_KEY_HEIGHT);
            Object r = outputClient.getClientHint(ClientConfiguration.CLIENT_KEY_ROTATION);
            if (w instanceof Integer) {
                outWidth = ((Integer) w).intValue();
            }
            if (h instanceof Integer) {
                outHeight = ((Integer) h).intValue();
            }
            if (r instanceof Integer) {
                rotation = ((Integer) r).intValue();
            }
        }        
//        if ("Reference".equals(library)) {
//            return new ReferencePlayer(title, width, height, fps, fullScreen);
//        } else {           
            PlayerFactory factory = findPlayerFactory(library);
            if (factory != null) {
                try {
                    return factory.createPlayer(new PlayerConfiguration(width, height, fps),
                            new ClientConfiguration[] {
                                new ClientConfiguration(0, 1, 
                                        fullScreen ? Collections.<String, Object>singletonMap(
                            ClientConfiguration.CLIENT_KEY_FULLSCREEN, Boolean.TRUE)
                                    : null)
                            });
                } catch (Exception ex) {
                    // fall through
                }
            }
            if (outWidth == width && outHeight == height && rotation == 0) {
                return SWPlayer.create(title, width, height, fps, fullScreen);
            } else {
                return SWPlayer.create(title, width, height, fps, fullScreen, outWidth, outHeight, rotation);
            }
//        }
    }
    
    private PlayerFactory findPlayerFactory(String lib) {
        if (lib == null || lib.isEmpty()) {
            return null;
        }
        try {
            for (PlayerFactory.Provider provider : getLookup().getAll(PlayerFactory.Provider.class)) {
                if (provider.getLibraryName().equals(lib)) {
                    return provider.getFactory();
                }
            }
        } catch (Exception e) {
        }
        return null;
    }
    

    @Override
    protected void stopping() {
        setInterrupt(new Runnable() {

            public void run() {
//                player.getOutputSink().removeSource(placeholder);
                player.terminate();
            }
        });

    }

    private class Context extends VideoContext {

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

//    private class OutputServer implements VideoOutputProxy {
//
//        public Sink getOutputSink(int index) {
//            if (index == 0) {
//                return placeholder;
//            }
//            throw new IndexOutOfBoundsException();
//        }
//
//        public int getOutputCount() {
//            return 1;
//        }
//    }
//    private class TitleBinding implements StringProperty.Binding {
//
//        public void setBoundValue(String value) {
//            if (getState() == Root.State.ACTIVE_RUNNING) {
//                throw new UnsupportedOperationException("Can't set title while running");
//            }
//            title = value;
//        }
//
//        public String getBoundValue() {
//            return title;
//        }
//        
//    }
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

    private class FpsBinding implements FloatProperty.Binding {

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
