/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2018 Neil C Smith.
 * 
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * version 3 for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License version 3
 * along with this work; if not, see http://www.gnu.org/licenses/
 * 
 * 
 * Please visit https://www.praxislive.org if you need additional information or
 * have any questions.
 */
package org.praxislive.gui.components;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import org.praxislive.gui.impl.SingleBindingGuiComponent;
import org.praxislive.gui.impl.BoundedValueAdaptor;
import java.util.logging.Logger;
import org.praxislive.core.Value;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.border.Border;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.plaf.basic.BasicSliderUI;
import org.praxislive.core.ArgumentInfo;
import org.praxislive.core.types.PMap;
import org.praxislive.core.types.PNumber;
import org.praxislive.core.types.PString;
import org.praxislive.impl.swing.ControlBinding.Adaptor;
import org.praxislive.impl.ArgumentProperty;

/**
 *
 * @author Neil C Smith
 */
class Slider extends SingleBindingGuiComponent {

    private static final Logger LOG = Logger.getLogger(Slider.class.getName());
    private String labelText;
    private Box box;
    private JSlider slider;
    private BoundedValueAdaptor adaptor;
    private boolean vertical;
    private PNumber prefMin;
    private PNumber prefMax;
    private PString prefScale;

    public Slider(boolean vertical) {
        labelText = "";
        this.vertical = vertical;
//        registerControl("label", StringProperty.create( new LabelBinding(), labelText));
//        registerControl("minimum", ArgumentProperty.create( new MinBinding(), PString.EMPTY));
//        registerControl("maximum", ArgumentProperty.create( new MaxBinding(), PString.EMPTY));
//        registerControl("scale", ArgumentProperty.create( new ScaleBinding(), PString.EMPTY));
    }

    @Override
    protected void initControls() {
        super.initControls();
        ArgumentInfo info = ArgumentInfo.create(Value.class,
                PMap.create(ArgumentInfo.KEY_ALLOW_EMPTY, true, ArgumentInfo.KEY_EMPTY_IS_DEFAULT, true));
        registerControl("minimum", ArgumentProperty.create(info, new MinBinding(), PString.EMPTY));
        registerControl("maximum", ArgumentProperty.create(info, new MaxBinding(), PString.EMPTY));
        info = ArgumentInfo.create(PString.class, PMap.create(ArgumentInfo.KEY_EMPTY_IS_DEFAULT, true));
        registerControl("scale", ArgumentProperty.create(info, new ScaleBinding(), PString.EMPTY));
    }

    @Override
    protected JComponent createSwingComponent() {
        if (box == null) {
            createComponentAndAdaptor();
        }
        return box;
    }

    @Override
    protected Adaptor getBindingAdaptor() {
        if (adaptor == null) {
            createComponentAndAdaptor();
        }
        return adaptor;
    }

    private void createComponentAndAdaptor() {
        slider = new JSlider(vertical ? JSlider.VERTICAL : JSlider.HORIZONTAL, 0, 500, 0);
        slider.setUI(new UI(slider));
        adaptor = new BoundedValueAdaptor(slider.getModel());
        if (prefMin != null) {
            adaptor.setPreferredMinimum(prefMin);
        }
        if (prefMax != null) {
            adaptor.setPreferredMaximum(prefMax);
        }
        if (prefScale != null) {
            adaptor.setPreferredScale(prefScale);
        }
        slider.addAncestorListener(new AncestorListener() {

            public void ancestorAdded(AncestorEvent event) {
                adaptor.setActive(true);
            }

            public void ancestorRemoved(AncestorEvent event) {
                adaptor.setActive(false);
            }

            public void ancestorMoved(AncestorEvent event) {
                // no op
            }
        });
        box = vertical ? Box.createVerticalBox() : Box.createHorizontalBox();
        box.add(slider);
        updateBorders();

    }

    private void updateBorders() {
        if (box != null) {
            Border etched = Utils.getBorder();
//                    BorderFactory.createEtchedBorder(slider.getBackground().brighter().brighter(),
//                    slider.getBackground().brighter());
            if (labelText.isEmpty()) {
                box.setBorder(etched);
            } else {
                box.setBorder(BorderFactory.createTitledBorder(
                        etched, labelText));
            }
            box.revalidate();
        }

    }

    @Override
    protected void updateLabel() {
        super.updateLabel();
        if (isLabelOnParent()) {
            labelText = "";
        } else {
            labelText = getLabel();
        }
        updateBorders();
    }

//    private class LabelBinding implements StringProperty.Binding {
//
//        public void setBoundValue(long time, String value) {
//            labelText = value;
//            if (box != null) {
//                updateBorders();
//            }
//        }
//
//        public String getBoundValue() {
//            return labelText;
//        }
//    }
    private class MinBinding implements ArgumentProperty.Binding {

        public void setBoundValue(long time, Value value) {
            if (value.isEmpty()) {
                prefMin = null;
            } else {
                try {
                    prefMin = PNumber.coerce(value);
                } catch (Exception ex) {
                    prefMin = null;
                }
            }
            if (adaptor != null) {
                adaptor.setPreferredMinimum(prefMin);
            }
        }

        public Value getBoundValue() {
            if (prefMin == null) {
                return PString.EMPTY;
            } else {
                return prefMin;
            }
        }

    }

    private class MaxBinding implements ArgumentProperty.Binding {

        public void setBoundValue(long time, Value value) {
            if (value.isEmpty()) {
                prefMax = null;
            } else {
                try {
                    prefMax = PNumber.coerce(value);
                } catch (Exception ex) {
                    prefMax = null;
                }
            }
            if (adaptor != null) {
                adaptor.setPreferredMaximum(prefMax);
            }
        }

        public Value getBoundValue() {
            if (prefMax == null) {
                return PString.EMPTY;
            } else {
                return prefMax;
            }
        }

    }

    private class ScaleBinding implements ArgumentProperty.Binding {

        public void setBoundValue(long time, Value value) {
            if (value.isEmpty()) {
                prefScale = null;
            } else if (value instanceof PString) {
                prefScale = (PString) value;
            } else {
                prefScale = PString.valueOf(value);
            }
            if (adaptor != null) {
                adaptor.setPreferredScale(prefScale);
            }
        }

        public Value getBoundValue() {
            if (prefScale == null) {
                return PString.EMPTY;
            } else {
                return prefScale;
            }
        }

    }

    private static class UI extends BasicSliderUI {

        public UI(JSlider slider) {
            super(slider);
        }

        @Override
        protected Dimension getThumbSize() {
            if (slider.getOrientation() == JSlider.VERTICAL) {
                return new Dimension(36, 12);
            } else {
                return new Dimension(12, 36);
            }
        }

        @Override
        public void paintFocus(Graphics g) {
            // do nothing
        }

        @Override
        public void paintTrack(Graphics g) {
            Rectangle r = trackRect;

            g.setColor(slider.hasFocus()
                    ? Utils.mix(slider.getBackground(), slider.getForeground(), 0.8)
                    : Utils.mix(slider.getBackground(), slider.getForeground(), 0.6));

            g.drawRect(r.x, r.y, r.width - 1, r.height - 1);
        }

        @Override
        public void paintThumb(Graphics g) {
            Rectangle r = thumbRect;
            if (isDragging()) {
                g.setColor(slider.getForeground());
            } else {
                g.setColor(Utils.mix(slider.getBackground(), slider.getForeground(), 0.8));
            }
            g.fillRect(r.x, r.y, r.width, r.height);
        }

        @Override
        protected TrackListener createTrackListener(JSlider slider) {
            return new TrackListener() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (!slider.isEnabled()) {
                        return;
                    }
                    calculateGeometry();

                    currentMouseX = e.getX();
                    currentMouseY = e.getY();

                    if (slider.isRequestFocusEnabled()) {
                        slider.requestFocus();
                    }

                    switch (slider.getOrientation()) {
                        case JSlider.VERTICAL:
                            offset = currentMouseY - thumbRect.y;
                            break;
                        case JSlider.HORIZONTAL:
                            offset = currentMouseX - thumbRect.x;
                            break;
                    }
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    int thumbMiddle;

                    if (!slider.isEnabled()) {
                        return;
                    }

                    currentMouseX = e.getX();
                    currentMouseY = e.getY();

                    slider.setValueIsAdjusting(true);

                    if (slider.getOrientation() == JSlider.VERTICAL) {
                        int halfThumbHeight = thumbRect.height / 2;
                        int thumbTop = e.getY() - offset;
                        int trackTop = trackRect.y;
                        int trackBottom = trackRect.y + (trackRect.height - 1);
                        int vMax = yPositionForValue(slider.getMaximum()
                                - slider.getExtent());

                        if (drawInverted()) {
                            trackBottom = vMax;
                        } else {
                            trackTop = vMax;
                        }
                        thumbTop = Math.max(thumbTop, trackTop - halfThumbHeight);
                        thumbTop = Math.min(thumbTop, trackBottom - halfThumbHeight);

                        setThumbLocation(thumbRect.x, thumbTop);

                        thumbMiddle = thumbTop + halfThumbHeight;
                        slider.setValue(valueForYPosition(thumbMiddle));
                    } else {
                        int halfThumbWidth = thumbRect.width / 2;
                        int thumbLeft = e.getX() - offset;
                        int trackLeft = trackRect.x;
                        int trackRight = trackRect.x + (trackRect.width - 1);
                        int hMax = xPositionForValue(slider.getMaximum()
                                - slider.getExtent());

                        if (drawInverted()) {
                            trackLeft = hMax;
                        } else {
                            trackRight = hMax;
                        }
                        thumbLeft = Math.max(thumbLeft, trackLeft - halfThumbWidth);
                        thumbLeft = Math.min(thumbLeft, trackRight - halfThumbWidth);

                        setThumbLocation(thumbLeft, thumbRect.y);

                        thumbMiddle = thumbLeft + halfThumbWidth;
                        slider.setValue(valueForXPosition(thumbMiddle));

                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (!slider.isEnabled()) {
                        return;
                    }
                    offset = 0;
                    slider.setValueIsAdjusting(false);
                    slider.repaint();
                }

            };
        }

    }

}
