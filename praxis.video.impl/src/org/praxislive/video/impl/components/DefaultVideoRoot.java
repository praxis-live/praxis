/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2018 Neil C Smith.
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
package org.praxislive.video.impl.components;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;
import org.praxislive.core.Lookup;
import org.praxislive.impl.AbstractRoot;
import org.praxislive.impl.InstanceLookup;
import org.praxislive.impl.IntProperty;
import org.praxislive.impl.NumberProperty;
import org.praxislive.impl.RootState;
import org.praxislive.impl.StringProperty;
import org.praxislive.video.ClientConfiguration;
import org.praxislive.video.ClientRegistrationException;
import org.praxislive.video.Player;
import org.praxislive.video.PlayerConfiguration;
import org.praxislive.video.PlayerFactory;
import org.praxislive.video.QueueContext;
import org.praxislive.video.VideoContext;
import org.praxislive.video.pipes.FrameRateListener;
import org.praxislive.video.pipes.FrameRateSource;

/**
 *
 * @author Neil C Smith
 */
public class DefaultVideoRoot extends AbstractRoot implements FrameRateListener {

    private final static String SOFTWARE = "Software";
    private final static List<String> RENDERERS = new ArrayList<>();
    static {
        RENDERERS.add(SOFTWARE);
        Lookup.SYSTEM.findAll(PlayerFactory.Provider.class)
                .forEach(r -> RENDERERS.add(r.getLibraryName()));
    }

    private final static Logger LOG = Logger.getLogger(DefaultVideoRoot.class.getName());

    private final static int WIDTH_DEFAULT = 640;
    private final static int HEIGHT_DEFAULT = 480;
    private final static double FPS_DEFAULT = 30;
    private int skipcount;
    private int width = WIDTH_DEFAULT;
    private int height = HEIGHT_DEFAULT;
    private double fps = FPS_DEFAULT;
    private StringProperty renderer;
    private Player player;
    private VideoContext.OutputClient outputClient;
    private VideoContextImpl ctxt;
    private Lookup lookup;

    public DefaultVideoRoot() {
        renderer = StringProperty.builder().defaultValue(SOFTWARE).allowedValues(RENDERERS.toArray(new String[0])).build();

        registerControl("renderer", renderer);
        registerControl("width", IntProperty.create(new WidthBinding(), 1, 16384, width));
        registerControl("height", IntProperty.create(new HeightBinding(), 1, 16384, height));
        registerControl("fps", NumberProperty.create(new FpsBinding(), 1, 256, fps));
        
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
            }
            update(source.getTime(), true);
        } catch (Exception ex) {
            // @TODO remove source
            player.terminate();
        }
    }

    @Override
    protected void starting() {
        try {
            String lib = renderer.getValue();
            player = createPlayer(lib);
            player.addFrameRateListener(this);
            lookup = InstanceLookup.create(getLookup(),
                    player.getLookup().findAll(Object.class).toArray());
            if (outputClient != null && outputClient.getOutputCount() > 0) {
                player.getSink(0).addSource(outputClient.getOutputSource(0));
            }
            setDelegate(new Runnable() {
                public void run() {
                    player.run();
                    setIdle();
                }
            });
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Couldn't start video renderer", ex);
            setIdle();
        }
    }

    private Player createPlayer(String library) throws Exception {
        Lookup clientLookup = Lookup.EMPTY;
        if (outputClient != null) {
            clientLookup = outputClient.getLookup();
        }
        PlayerFactory factory = findPlayerFactory(library);
        Lookup plLkp = InstanceLookup.create(new QueueContextImpl());
        return factory.createPlayer(new PlayerConfiguration(getRootHub().getClock(), width, height, fps, plLkp),
                new ClientConfiguration[]{
                    new ClientConfiguration(0, 1, clientLookup)
                });
    }

    private PlayerFactory findPlayerFactory(String lib) throws Exception {
        if (lib == null || lib.isEmpty() || "Software".equals(lib)) {
            return SWPlayer.getFactory();
        }
        return Lookup.SYSTEM.findAll(PlayerFactory.Provider.class)
                .filter(p -> lib.equals(p.getLibraryName()))
                .map(PlayerFactory.Provider::getFactory)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No valid renderer found"));
    }

    @Override
    protected void stopping() {
        lookup = null;
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

}
