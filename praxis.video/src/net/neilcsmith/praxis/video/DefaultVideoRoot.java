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
package net.neilcsmith.praxis.video;

import net.neilcsmith.praxis.core.IllegalRootStateException;
import net.neilcsmith.praxis.core.Root;
import net.neilcsmith.praxis.impl.AbstractRoot;
import net.neilcsmith.praxis.impl.BooleanProperty;
import net.neilcsmith.praxis.impl.FloatProperty;
import net.neilcsmith.praxis.impl.IntProperty;
import net.neilcsmith.ripl.FrameRateListener;
import net.neilcsmith.ripl.FrameRateSource;
import net.neilcsmith.ripl.Player;
import net.neilcsmith.ripl.render.RiplPlayer;

/**
 *
 * @author Neil C Smith
 */
public class DefaultVideoRoot extends AbstractRoot implements VideoRoot, FrameRateListener {

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
    private VideoOutputClient outputClient;

    public DefaultVideoRoot() {
        super(State.ACTIVE_IDLE);
//        placeholder = new Placeholder();
        buildControls();
    }

    private void buildControls() {
        registerControl("width", IntProperty.create(this, new WidthBinding(), 1, 2048, width));
        registerControl("height", IntProperty.create(this, new HeightBinding(), 1, 2048, height));
        registerControl("fps", FloatProperty.create(this, new FpsBinding(), 1, 100, fps));
        registerControl("full-screen", BooleanProperty.create(this, new FullScreenBinding(), fullScreen));
    }
    
//    public VideoInputProxy registerImageInputClient(Component client) throws ClientRegistrationException {
//        throw new ClientRegistrationException();
//    }
//
//    public void unregisterImageInputClient(Component client) {
//        throw new UnsupportedOperationException();
//    }
//
//    public VideoOutputProxy registerImageOutputClient(Component client) throws ClientRegistrationException {
//        if (outputServer == null) {
//            outputServer = new OutputServer();
//            return outputServer;
//        }
//        throw new ClientRegistrationException();
//    }
//
//    public void unregisterImageOutputClient(Component proxy) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
    
    public int registerVideoInputClient(VideoInputClient client) throws ClientRegistrationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void unregisterVideoInputClient(VideoInputClient client) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int registerVideoOutputClient(VideoOutputClient client) throws ClientRegistrationException {
        if (outputClient == null) {
            outputClient = client;
            return 1;
        } else {
            throw new ClientRegistrationException();
        }
    }

    // @TODO should not allow while running!
    public void unregisterVideoOutputClient(VideoOutputClient client) {
        if (outputClient == client) {
            outputClient = null;
        }
    }

    public void nextFrame(FrameRateSource source) {
        try {
            if (!source.isRendering()) {
                skipcount++;
//                System.out.println("Frame skipped " + skipcount);
            }
            setTime(source.getTime());
            processControlFrame();
        } catch (IllegalRootStateException ex) {
            // @TODO remove source
            player.terminate();
        }
    }

    @Override
    protected void starting() {
        try {
            skipcount = 0;
            player = new RiplPlayer("PRAXIS : " + getAddress(), width, height, fps, fullScreen);
            player.addFrameRateListener(this);
//            player.getSink(0).addSource(placeholder);
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

    @Override
    protected void stopping() {
        setInterrupt(new Runnable() {

            public void run() {
//                player.getOutputSink().removeSource(placeholder);
                player.terminate();
            }
        });

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
            if (getState() == Root.State.ACTIVE_RUNNING) {
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
            if (getState() == Root.State.ACTIVE_RUNNING) {
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
            if (getState() == Root.State.ACTIVE_RUNNING) {
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
            if (getState() == Root.State.ACTIVE_RUNNING) {
                throw new UnsupportedOperationException("Can't set full screen state while running");
            }
            fullScreen = value;
        }

        public boolean getBoundValue() {
            return fullScreen;
        }
        
    }


}
