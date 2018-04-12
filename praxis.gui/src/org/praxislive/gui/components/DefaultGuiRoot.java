/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2016 Neil C Smith.
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
package org.praxislive.gui.components;

import com.bulenkov.darcula.DarculaLaf;
import java.awt.Dimension;
import java.awt.EventQueue;
import org.praxislive.core.Lookup;
import java.awt.LayoutManager;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
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
import javax.swing.UIManager;
import net.miginfocom.swing.MigLayout;
import org.praxislive.core.ControlAddress;
import org.praxislive.gui.Keys;
import org.praxislive.impl.swing.ControlBinding;
import org.praxislive.impl.swing.ControlBinding.Adaptor;
import org.praxislive.impl.swing.BindingContext;
import org.praxislive.gui.GuiContext;
import org.praxislive.impl.swing.DefaultBindingControl;
import org.praxislive.impl.swing.AbstractSwingRoot;
import org.praxislive.impl.InstanceLookup;

/**
 *
 * @author Neil C Smith
 */

// IMPORTANT: Fixes and changes to behaviour of this class should be propagated
//            to DockableGuiRoot in praxis.live.pxr.gui
public class DefaultGuiRoot extends AbstractSwingRoot {

    private static boolean lafConfigured = false;
    
    private JFrame frame;
    private JPanel container;
    private MigLayout layout;
    private LayoutChangeListener layoutListener;
    private Map<ControlAddress, DefaultBindingControl> bindingCache;
    private Bindings bindings;
    private Context context;
    private Lookup lookup;

    public DefaultGuiRoot() {
        bindingCache = new HashMap<ControlAddress, DefaultBindingControl>();
    }

    @Override
    protected void setup() {
        if (!lafConfigured) {
            setupLookAndFeel();
            lafConfigured = true;
        }
        frame = new JFrame();
        frame.setTitle("PRAXIS : " + getAddress());
//        frame.setSize(150, 50);
        frame.setMinimumSize(new Dimension(150,50));
        frame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                setIdle();
            }
        });
        frame.getContentPane().setLayout(new MigLayout("fill", "[fill, grow]"));
        layout = new MigLayout("fill", "[fill]");
        container = new JPanel(layout);
        container.addContainerListener(new ChildrenListener());
        layoutListener = new LayoutChangeListener();        
        frame.getContentPane().add(new JScrollPane(container), "grow, push");
    }
    
    private void setupLookAndFeel() {
        try {
            assert EventQueue.isDispatchThread();
            UIManager.installLookAndFeel(
                    new UIManager.LookAndFeelInfo(
                            DarculaLaf.NAME,
                            DarculaLaf.class.getName()));
            Lookup.SYSTEM.find(ClassLoader.class).ifPresent(cl -> UIManager.put("ClassLoader", cl));
            UIManager.setLookAndFeel(new DarculaLaf());
        } catch (Exception ex) {
            Logger.getLogger(DefaultGuiRoot.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
        
    @Override
    public Lookup getLookup() {
        if (lookup == null) {
            bindings = new Bindings();
            context = new Context();
            lookup = InstanceLookup.create(super.getLookup(), bindings, context);
        }
        return lookup;
    }

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
    protected void dispose() {
        super.dispose();
        frame.setVisible(false);
        frame.dispose();
    }


    private class Bindings extends BindingContext {

        public void bind(ControlAddress address, Adaptor adaptor) {
            DefaultBindingControl binding = bindingCache.get(address);
            if (binding == null) {
                binding = new DefaultBindingControl(address);
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
            DefaultBindingControl binding = bindingCache.get(cBinding.getAddress());
            if (binding != null) {
                binding.unbind(adaptor);
            }
        }
    }

    private class Context extends GuiContext {

        @Override
        public JComponent getContainer() {
            return container;
        }
    }



    private void setLayoutConstraint(JComponent child) {
        layout.setComponentConstraints(child, child.getClientProperty(Keys.LayoutConstraint));
        container.revalidate();
        container.repaint();
    }

    private class ChildrenListener implements ContainerListener {

        public void componentAdded(ContainerEvent e) {
            if (e.getChild() instanceof JComponent) {
                JComponent child = (JComponent) e.getChild();
                child.addPropertyChangeListener(
                        Keys.LayoutConstraint, layoutListener);
                setLayoutConstraint(child);
            }
        }

        public void componentRemoved(ContainerEvent e) {
            if (e.getChild() instanceof JComponent) {
                ((JComponent) e.getChild()).removePropertyChangeListener(
                        Keys.LayoutConstraint, layoutListener);
            }
        }

    }

    private class LayoutChangeListener implements PropertyChangeListener {

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

}
