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
package net.neilcsmith.praxis.gui;

import java.awt.EventQueue;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Timer;
import net.miginfocom.swing.MigLayout;
import net.neilcsmith.praxis.core.Component;
import net.neilcsmith.praxis.core.ComponentAddress;
import net.neilcsmith.praxis.core.ControlAddress;
import net.neilcsmith.praxis.core.IllegalRootStateException;
import net.neilcsmith.praxis.core.Packet;
import net.neilcsmith.praxis.core.Root;
import net.neilcsmith.praxis.core.RootHub;
import net.neilcsmith.praxis.core.VetoException;
import net.neilcsmith.praxis.gui.ControlBinding.Adaptor;
import net.neilcsmith.praxis.impl.AbstractRoot;
import net.neilcsmith.praxis.impl.RootState;

/**
 *
 * @author Neil C Smith
 */
public class DefaultGuiRoot extends AbstractRoot implements GuiRoot {

    private final Object lock = new Object();
    private JFrame frame;
    private JPanel container;
    private MigLayout layout;
    private LayoutChangeListener layoutListener;
    private Timer timer;
    private Map<ControlAddress, DefaultBinding> bindingCache;

    public DefaultGuiRoot() {
        bindingCache = new HashMap<ControlAddress, DefaultBinding>();
    }

    @Override
    public Root.Controller initialize(String ID, RootHub hub) throws IllegalRootStateException {
        Root.Controller ctrl = super.initialize(ID, hub);        
        return new DelegateController(ctrl);
    }

    // @TODO should be activating?
    @Override
    protected void initializing() {
        super.initializing();
        try {
            EventQueue.invokeAndWait(new Runnable() {

                public void run() {
                    initialize();
                }
            });
        } catch (Exception ex) {
            // @TODO what to do about exception?
        }
    }
    
    

    @Override
    protected final void run() {
        
        timer = new Timer(50, new TimerProcessor());
        timer.start();
        RootState st;
        while ((st = getState()) == RootState.ACTIVE_IDLE || st == RootState.ACTIVE_RUNNING) {
//            System.out.println("Called Once");
            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException ex) {
                    System.out.println("Shouldn't be called");
                }
            }
        }
    }

    protected void initialize() {
        frame = new JFrame();
        frame.setTitle("PRAXIS : " + getAddress());
        frame.setSize(150, 50);
        frame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    setIdle();
                } catch (IllegalRootStateException ex) {
                    Logger.getLogger(DefaultGuiRoot.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
//        container = Box.createVerticalBox();
        layout = new MigLayout("fill", "[fill,grow]", "[fill,grow]");
        container = new JPanel(layout);
        layoutListener = new LayoutChangeListener(container);
        frame.add(new JScrollPane(container));
    }

    @Override
    public void addChild(String id, Component child) throws VetoException {
        super.addChild(id, child);
        if (child instanceof GuiComponent) {
            try {
                JComponent comp = ((GuiComponent) child).getSwingComponent();
//                comp.setAlignmentX(JComponent.LEFT_ALIGNMENT);
                comp.setAlignmentY(JComponent.TOP_ALIGNMENT);
                Object constraints = comp.getClientProperty(ClientKeys.LayoutConstraint);
                container.add(comp, constraints);
                container.revalidate();
                container.repaint();
                comp.addPropertyChangeListener(ClientKeys.LayoutConstraint, layoutListener);
            } catch (Exception e) {
                super.removeChild(id);
                throw new VetoException();
            }
        }
    }

    @Override
    public Component removeChild(String id) {
        Component child = super.removeChild(id);
        if (child instanceof GuiComponent) {
            JComponent comp = ((GuiComponent) child).getSwingComponent();
            container.remove(comp);
            container.revalidate();
            container.repaint();
            comp.removePropertyChangeListener(ClientKeys.LayoutConstraint, layoutListener);
        }
        return child;
    }

//    @Override
//    protected void addComponent(ComponentAddress address, Component component) throws Exception {
//        super.addComponent(address, component);
//        frame.pack();
//    }
//
//    @Override
//    protected void removeComponent(ComponentAddress address) throws Exception {
//        super.removeComponent(address);
//        frame.pack();
//    }

    @Override
    protected void starting() {
        super.starting();
        frame.pack();
        frame.setVisible(true);
    }

    @Override
    protected void stopping() {
        super.stopping();
        frame.setVisible(false);
    }

    @Override
    protected void terminating() {
        super.terminating();
        frame.setVisible(false);
        frame.dispose();
    }

    private class TimerProcessor implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            nextControlFrame();
        }
    }

    private void nextControlFrame() {
        try {
            setTime(System.nanoTime());
            processControlFrame();
        } catch (IllegalRootStateException ex) {
            timer.stop();
            synchronized (lock) {
                lock.notifyAll();
            }
        }
    }

    public void bind(ControlAddress address, Adaptor adaptor) {
        DefaultBinding binding = bindingCache.get(address);
        if (binding == null) {
            binding = new DefaultBinding(this, address);
            registerControl("_binding_" + Integer.toHexString(binding.hashCode()),
                    binding);
            bindingCache.put(address, binding);
        }
        binding.bind(adaptor);
    }

    public void unbind(Adaptor adaptor) {
        ControlBinding cBinding = adaptor.getBinding();
        if (cBinding == null) {
            return;
        }
        DefaultBinding binding = bindingCache.get(cBinding.getAddress());
        if (binding != null) {
            binding.unbind(adaptor);
        }
    }

    private class LayoutChangeListener implements PropertyChangeListener {

        private JComponent container;

        public LayoutChangeListener(JComponent container) {
            this.container = container;
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getSource() instanceof JComponent) {
                JComponent comp = (JComponent) evt.getSource();
                LayoutManager lm = container.getLayout();
                if (lm instanceof MigLayout) {
                    ((MigLayout) lm).setComponentConstraints(comp, evt.getNewValue());
                    container.revalidate();
                }
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
            EventQueue.invokeLater(runner);
            return ret;
        }

        public void shutdown() {
            ctrl.shutdown();
        }

        public void run() throws IllegalRootStateException {
            ctrl.run();
        }
    }
}
