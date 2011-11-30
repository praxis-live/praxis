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
package net.neilcsmith.praxis.impl;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;
import net.neilcsmith.praxis.core.IllegalRootStateException;
import net.neilcsmith.praxis.core.Packet;
import net.neilcsmith.praxis.core.Root;
import net.neilcsmith.praxis.core.RootHub;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class AbstractSwingRoot extends AbstractRoot {

    private final static Logger LOG = Logger.getLogger(AbstractSwingRoot.class.getName());
    private final static int DEFAULT_PERIOD = 50;

    private final Object lock = new Object();
    private Timer timer;

    protected AbstractSwingRoot() {
    }

    protected AbstractSwingRoot(EnumSet<Caps> caps) {
        super(caps);
    }

    @Override
    public Root.Controller initialize(String ID, RootHub hub) throws IllegalRootStateException {
        Root.Controller ctrl = super.initialize(ID, hub);
        timer = new Timer(DEFAULT_PERIOD, new TimerProcessor());
        return new DelegateController(ctrl);
    }

    @Override
    protected final void activating() {
        super.activating();
        try {
            EventQueue.invokeAndWait(new Runnable() {

                public void run() {
                    setup();
                }
            });
        } catch (Exception ex) {
            // @TODO what to do about exception?
        }
    }

    @Override
    protected final void run() {

        timer.start();
        RootState st;
        while ((st = getState()) == RootState.ACTIVE_IDLE || st == RootState.ACTIVE_RUNNING) {
            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException ex) {
                    LOG.log(Level.WARNING, "Unexpected Thread interruption in AbstractSwingRoot", ex);
                }
            }
        }
    }

    @Override
    protected final void terminating() {
        super.terminating();
        try {
            EventQueue.invokeAndWait(new Runnable() {

                public void run() {
                    dispose();
                }
            });
        } catch (Exception ex) {
            // @TODO what to do about exception?
        }
    }

    protected void setup() {
    }

    protected void dispose() {
    }

    @Override
    final void disconnect() {
        try {
            EventQueue.invokeAndWait(new Runnable() {

                public void run() {
                    AbstractSwingRoot.super.disconnect();
                }
            });
        } catch (Exception ex) {
            // @TODO what to do about exception?
        }
    }

    private void nextControlFrame() {
        try {
            nextControlFrame(System.nanoTime());
        } catch (IllegalRootStateException ex) {
            timer.stop();
            synchronized (lock) {
                lock.notifyAll();
            }
        }
    }

    private class DelegateController implements Root.Controller {

        private Root.Controller ctrl;
        private Runnable runner;

        private DelegateController(Root.Controller ctrl) {
            this.ctrl = ctrl;
            runner = new Runnable() {

                public void run() {
                    nextControlFrame();
                }
            };
        }

        public boolean submitPacket(Packet packet) {
            boolean ret = ctrl.submitPacket(packet);
            RootState state = getState();
            if (state == RootState.ACTIVE_RUNNING
                    || state == RootState.ACTIVE_IDLE) {
                EventQueue.invokeLater(runner);
            }
            return ret;
        }

        public void shutdown() {
            ctrl.shutdown();
        }

        public void run() throws IllegalRootStateException {
            ctrl.run();
        }
    }

    private class TimerProcessor implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            nextControlFrame();
        }
    }
}
