/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Neil C Smith.
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
 *
 */
package net.neilcsmith.praxis.gui.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.Painter;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;

/**
 *
 * @author Neil C Smith
 */
class JXYController extends JComponent {

    private int controlWidth = 32;
    private int controlXPos;
    private int controlYPos;
    protected transient ChangeEvent changeEvent = null;
    private BoundedRangeModel xRangeModel;
    private BoundedRangeModel yRangeModel;

    //mouse motion related fields
    private int mouseXOffset;
    private int mouseYOffset;
    private boolean draggingControl;

    private Painter<JComponent> thumbPainter;
    private Painter<JComponent> selectedThumbPainter;
    private Color foreground;
    private Color selectedForeground;
    private Color background;

    /**
     * Creates a new instance of XYController
     */
    public JXYController() {
        setPreferredSize(new Dimension(200, 200));

        MouseInput mouseAdapter = new MouseInput();
        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
        addComponentListener(new ResizeHandler());
        mouseXOffset = 0;
        mouseYOffset = 0;
        draggingControl = false;

        xRangeModel = new DefaultBoundedRangeModel(0, 0, 0, 100);
        yRangeModel = new DefaultBoundedRangeModel(0, 0, 0, 100);
        ModelListener modelListener = new ModelListener();
        xRangeModel.addChangeListener(modelListener);
        yRangeModel.addChangeListener(modelListener);
        updateUI();
    }

    @Override
    public void updateUI() {
        super.updateUI();
        UIDefaults defs = UIManager.getDefaults();
        Object val = defs.get("Slider:SliderThumb[Enabled].backgroundPainter");
        if (val instanceof Painter) {
            thumbPainter = (Painter<JComponent>) val;
        }
        val = defs.get("Slider:SliderThumb[Focused+Pressed].backgroundPainter");
        if (val instanceof Painter) {
            selectedThumbPainter = (Painter<JComponent>) val;
        }
        val = defs.get("controlShadow");
        if (val instanceof Color) {
            foreground = (Color) val;
        }
        val = defs.get("nimbusFocus");
        if (val instanceof Color) {
            selectedForeground = (Color) val;
        }
        val = defs.get("desktop");
        if (val instanceof Color) {
            background = (Color) val;
        }
    }

    public BoundedRangeModel getXRangeModel() {
        return xRangeModel;
    }

    public BoundedRangeModel getYRangeModel() {
        return yRangeModel;
    }

    public int getXValue() {
        return xRangeModel.getValue();
    }

    public int getYValue() {
        return yRangeModel.getValue();
    }

    public int getXMinimum() {
        return xRangeModel.getMinimum();
    }

    public int getYMinimum() {
        return yRangeModel.getMinimum();
    }

    public int getXMaximum() {
        return xRangeModel.getMaximum();
    }

    public int getYMaximum() {
        return yRangeModel.getMaximum();
    }

    public void addChangeListener(ChangeListener listener) {
        listenerList.add(ChangeListener.class, listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        listenerList.remove(ChangeListener.class, listener);
    }

    public ChangeListener[] getChangeListeners() {
        return (ChangeListener[]) listenerList.getListeners(ChangeListener.class);
    }

    protected void paintComponent(Graphics graphics) {
        Graphics2D g2d = (Graphics2D) graphics;
        int width = getWidth();
        int height = getHeight();
        if (background != null) {
            g2d.setColor(background);
            g2d.fillRect(0, 0, width, height);
        }

        Painter<JComponent> p;
        if (draggingControl) {
            g2d.setColor(selectedForeground == null ? getForeground().brighter() : selectedForeground);
            p = selectedThumbPainter;
        } else {
            g2d.setColor(foreground == null ? getForeground() : foreground);
            p = thumbPainter;
        }

        int radius = controlWidth / 2;
        g2d.drawLine(controlXPos + radius, 0, controlXPos + radius, height);
        g2d.drawLine(0, controlYPos + radius, width, controlYPos + radius);

        if (p != null) {
            g2d.translate(controlXPos, controlYPos);
            p.paint(g2d, this, controlWidth, controlWidth);
            g2d.translate(-controlXPos, -controlYPos);
        } else {
            g2d.drawRect(controlXPos + 1, controlYPos + 1,
                    controlWidth - 1, controlWidth - 1);
        }

    }

    protected int xPositionForModelValue() {
        int normalizedValue = xRangeModel.getValue() - xRangeModel.getMinimum();
        int modelRange = xRangeModel.getMaximum() - xRangeModel.getMinimum();
        float ratio = (float) normalizedValue / (float) modelRange;
        return (int) (ratio * (getWidth() - controlWidth));
    }

    protected int yPositionForModelValue() {
//        int normalizedValue = yRangeModel.getValue() - yRangeModel.getMinimum();
//        int modelRange = yRangeModel.getMaximum() - yRangeModel.getMinimum();
//        float ratio = (float) normalizedValue / (float) modelRange;
//        return (int) (ratio * (getHeight() - controlWidth));
        int normalizedValue = yRangeModel.getValue() - yRangeModel.getMinimum();
        int modelRange = yRangeModel.getMaximum() - yRangeModel.getMinimum();
        float ratio = (float) normalizedValue / (float) modelRange;
        ratio = 1 - ratio;
        return (int) (ratio * (getHeight() - controlWidth));
    }

    protected int xModelValueForPosition() {
        float ratio = (float) controlXPos / (float) (getWidth() - controlWidth);
        int modelRange = xRangeModel.getMaximum() - xRangeModel.getMinimum();
        return (int) ((ratio * modelRange) + xRangeModel.getMinimum());
    }

    protected int yModelValueForPosition() {
//        float ratio = (float) controlYPos / (float) (getHeight() - controlWidth);
//        int modelRange = yRangeModel.getMaximum() - yRangeModel.getMinimum();
//        return (int) ((ratio * modelRange) + yRangeModel.getMinimum());
        float ratio = (float) controlYPos / (float) (getHeight() - controlWidth);
        ratio = 1 - ratio;
        int modelRange = yRangeModel.getMaximum() - yRangeModel.getMinimum();
        return (int) ((ratio * modelRange) + yRangeModel.getMinimum());
    }

    protected void fireStateChanged() {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ChangeListener.class) {
                if (changeEvent == null) {
                    changeEvent = new ChangeEvent(this);
                }
                ((ChangeListener) listeners[i + 1]).stateChanged(changeEvent);
            }
        }
    }

    class ModelListener implements ChangeListener {

        public void stateChanged(ChangeEvent changeEvent) {
            Object source = changeEvent.getSource();

            // don't update UI if currently dragging
            if (!draggingControl) {
                if (source == xRangeModel) {
                    controlXPos = xPositionForModelValue();
                    repaint();
                } else if (source == yRangeModel) {
                    controlYPos = yPositionForModelValue();
                    repaint();
                }
            }

            fireStateChanged();

        }
    }

    class MouseInput extends MouseInputAdapter {

        public void mouseDragged(MouseEvent mouseEvent) {
//            System.out.println("" + mouseEvent);
            if (draggingControl) {
                int x = mouseEvent.getX() - mouseXOffset;
                int y = mouseEvent.getY() - mouseYOffset;
                int xMax = (getWidth() - controlWidth);
                int yMax = (getHeight() - controlWidth);
                // check X
                if (x < 0) {
                    controlXPos = 0;
                } else if (x > xMax) {
                    controlXPos = xMax;
                } else {
                    controlXPos = x;
                }
                // check Y
                if (y < 0) {
                    controlYPos = 0;
                } else if (y > yMax) {
                    controlYPos = yMax;
                } else {
                    controlYPos = y;
                }
                //update model
                xRangeModel.setValue(xModelValueForPosition());
                yRangeModel.setValue(yModelValueForPosition());
                //
                repaint();
            }
        }

        public void mousePressed(MouseEvent mouseEvent) {
            int x = mouseEvent.getX();
            int y = mouseEvent.getY();
            if (x >= controlXPos && x < (controlXPos + controlWidth)
                    && y >= controlYPos && y < (controlYPos + controlWidth)) {
                mouseXOffset = x - controlXPos;
                mouseYOffset = y - controlYPos;

            } else {
                int rad = controlWidth / 2;
                int maxY = getHeight() - controlWidth;
                int maxX = getWidth() - controlWidth;
                controlXPos = x - rad;
                if (controlXPos < 0) {
                    controlXPos = 0;
                } else if (controlXPos > maxX) {
                    controlXPos = maxX;
                }
                controlYPos = y - rad;
                if (controlYPos < 0) {
                    controlYPos = 0;
                } else if (controlYPos > maxY) {
                    controlYPos = maxY;
                }
                mouseXOffset = x - controlXPos;
                mouseYOffset = y - controlYPos;
            }
            draggingControl = true;
            //update model
            xRangeModel.setValue(xModelValueForPosition());
            xRangeModel.setValueIsAdjusting(true);
            yRangeModel.setValue(yModelValueForPosition());
            yRangeModel.setValueIsAdjusting(true);
            repaint();
        }

        public void mouseReleased(MouseEvent mouseEvent) {
            // final update of model here if dragging has been set, in case value has been missed
            if (draggingControl) {
                xRangeModel.setValue(xModelValueForPosition());
                xRangeModel.setValueIsAdjusting(false);
                yRangeModel.setValue(yModelValueForPosition());
                yRangeModel.setValueIsAdjusting(false);
                draggingControl = false;
                repaint();
            }

        }
    }

    private class ResizeHandler extends ComponentAdapter {

        @Override
        public void componentResized(ComponentEvent e) {
            controlXPos = xPositionForModelValue();
            controlYPos = yPositionForModelValue();
            repaint();
        }
    }

}
