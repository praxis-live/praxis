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
package org.praxislive.impl.swing;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;
import org.praxislive.core.Packet;
import org.praxislive.core.Root;
import org.praxislive.core.RootHub;
import org.praxislive.impl.AbstractRoot;
import org.praxislive.impl.RootState;

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
    public Root.Controller initialize(String ID, RootHub hub) {
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
    final protected void disconnect() {
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

    private void delegateUpdate() {
        try {
            update(System.nanoTime(), true);
        } catch (Exception ex) {
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
                    delegateUpdate();
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

        public void run() {
            ctrl.run();
        }
    }

    private class TimerProcessor implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            update();
        }
    }
}
