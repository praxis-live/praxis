package org.praxislive.gui.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * <p>
 * Implements a Swing-based Range slider, which allows the user to enter a range
 * (minimum and maximum) value.</p>
 *
 * @author Ben Bederson
 * @author Jesse Grosjean
 * @author Jon Meyer
 * @author Lance Good
 * @author jeffrey heer
 * @author Colin Combe
 * @author Neil C Smith <http://neilcsmith.net>
 */
class JRangeSlider extends JComponent
        implements MouseListener, MouseMotionListener, KeyListener {

    /*
     * NOTE: This is a modified version of the original class distributed by
     * Ben Bederson, Jesse Grosjean, and Jon Meyer as part of an HCIL Tech
     * Report.  It is modified to allow both vertical and horitonal modes.
     * It also fixes a bug with offset on the buttons. Also fixed a bug with
     * rendering using (x,y) instead of (0,0) as origin.  Also modified to
     * render arrows as a series of lines rather than as a GeneralPath.
     * Also modified to fix rounding errors on toLocal and toScreen.
     * 
     * With inclusion in prefuse, this class has been further modified to use a
     * bounded range model, support keyboard commands and more extensize
     * parameterization of rendering/appearance options. Furthermore, a stub
     * method has been introduced to allow subclasses to perform custom
     * rendering within the slider through.
     */
    final public static int VERTICAL = 0;
    final public static int HORIZONTAL = 1;
    final public static int LEFTRIGHT_TOPBOTTOM = 0;
    final public static int RIGHTLEFT_BOTTOMTOP = 1;

    final public static int PREFERRED_BREADTH = 36;
    final public static int PREFERRED_LENGTH = 200;
    final protected static int ARROW_SZ = 12;

    protected BoundedRangeModel model;
    protected int orientation;
    protected int direction;
    protected boolean empty;
    protected int increment = 1;
    protected int minExtent = 0; // min extent, in pixels

    protected ArrayList listeners = new ArrayList();
    protected ChangeEvent changeEvent = null;
    protected ChangeListener lstnr;

    // ------------------------------------------------------------------------
    /**
     * Create a new range slider.
     *
     * @param minimum - the minimum value of the range.
     * @param maximum - the maximum value of the range.
     * @param lowValue - the current low value shown by the range slider's bar.
     * @param highValue - the current high value shown by the range slider's
     * bar.
     * @param orientation - construct a horizontal or vertical slider?
     */
    public JRangeSlider(int minimum, int maximum, int lowValue, int highValue, int orientation) {
        this(new DefaultBoundedRangeModel(lowValue, highValue - lowValue, minimum, maximum),
                orientation, LEFTRIGHT_TOPBOTTOM);
    }

    /**
     * Create a new range slider.
     *
     * @param minimum - the minimum value of the range.
     * @param maximum - the maximum value of the range.
     * @param lowValue - the current low value shown by the range slider's bar.
     * @param highValue - the current high value shown by the range slider's
     * bar.
     * @param orientation - construct a horizontal or vertical slider?
     * @param direction - Is the slider left-to-right/top-to-bottom or
     * right-to-left/bottom-to-top
     */
    public JRangeSlider(int minimum, int maximum, int lowValue, int highValue, int orientation, int direction) {
        this(new DefaultBoundedRangeModel(lowValue, highValue - lowValue, minimum, maximum),
                orientation, direction);
    }

    /**
     * Create a new range slider.
     *
     * @param model - a BoundedRangeModel specifying the slider's range
     * @param orientation - construct a horizontal or vertical slider?
     * @param direction - Is the slider left-to-right/top-to-bottom or
     * right-to-left/bottom-to-top
     */
    public JRangeSlider(BoundedRangeModel model, int orientation, int direction) {
        super.setFocusable(true);
        this.model = model;
        this.orientation = orientation;
        this.direction = direction;

        this.lstnr = createListener();
        model.addChangeListener(lstnr);

        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);

    }

    /**
     * Create a listener to relay change events from the bounded range model.
     *
     * @return a ChangeListener to relay events from the range model
     */
    protected ChangeListener createListener() {
        return new RangeSliderChangeListener();
    }

    /**
     * Listener that fires a change event when it receives change event from the
     * slider list model.
     */
    protected class RangeSliderChangeListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            fireChangeEvent();
        }
    }

    /**
     * Returns the current "low" value shown by the range slider's bar. The low
     * value meets the constraint minimum <= lowValue <= highValue <= maximum.
     */
    public int getLowValue() {
        return model.getValue();
    }

    /**
     * Sets the low value shown by this range slider. This causes the range
     * slider to be repainted and a ChangeEvent to be fired.
     *
     * @param lowValue the low value to use
     */
    public void setLowValue(int lowValue) {
        int e = (model.getValue() - lowValue) + model.getExtent();
        model.setRangeProperties(lowValue, e,
                model.getMinimum(), model.getMaximum(), false);
        model.setValue(lowValue);
    }

    /**
     * Returns the current "high" value shown by the range slider's bar. The
     * high value meets the constraint minimum <= lowValue <= highValue <=
     * maximum.
     */
    public int getHighValue() {
        return model.getValue() + model.getExtent();
    }

    /**
     * Sets the high value shown by this range slider. This causes the range
     * slider to be repainted and a ChangeEvent to be fired.
     *
     * @param highValue the high value to use
     */
    public void setHighValue(int highValue) {
        model.setExtent(highValue - model.getValue());
    }

    /**
     * Set the slider range span.
     *
     * @param lowValue the low value of the slider range
     * @param highValue the high value of the slider range
     */
    public void setRange(int lowValue, int highValue) {
        model.setRangeProperties(lowValue, highValue - lowValue,
                model.getMinimum(), model.getMaximum(), false);
    }

    /**
     * Gets the minimum possible value for either the low value or the high
     * value.
     *
     * @return the minimum possible range value
     */
    public int getMinimum() {
        return model.getMinimum();
    }

    /**
     * Sets the minimum possible value for either the low value or the high
     * value.
     *
     * @param minimum the minimum possible range value
     */
    public void setMinimum(int minimum) {
        model.setMinimum(minimum);
    }

    /**
     * Gets the maximum possible value for either the low value or the high
     * value.
     *
     * @return the maximum possible range value
     */
    public int getMaximum() {
        return model.getMaximum();
    }

    /**
     * Sets the maximum possible value for either the low value or the high
     * value.
     *
     * @param maximum the maximum possible range value
     */
    public void setMaximum(int maximum) {
        model.setMaximum(maximum);
    }

    /**
     * Sets the minimum extent (difference between low and high values). This
     * method <strong>does not</strong> change the current state of the model,
     * but can affect all subsequent interaction.
     *
     * @param minExtent the minimum extent allowed in subsequent interaction
     */
    public void setMinExtent(int minExtent) {
        this.minExtent = minExtent;
    }

    /**
     * Sets whether this slider is empty.
     *
     * @param empty true if set to empty, false otherwise
     */
    public void setEmpty(boolean empty) {
        this.empty = empty;
        repaint();
    }

    /**
     * Get the BoundedRangeModel backing this slider.
     *
     * @return the slider's range model
     */
    public BoundedRangeModel getModel() {
        return model;
    }

    /**
     * Set the BoundedRangeModel backing this slider.
     *
     * @param brm the slider range model to use
     */
    public void setModel(BoundedRangeModel brm) {
        model.removeChangeListener(lstnr);
        model = brm;
        model.addChangeListener(lstnr);
        repaint();
    }

    /**
     * Registers a listener for ChangeEvents.
     *
     * @param cl the ChangeListener to add
     */
    public void addChangeListener(ChangeListener cl) {
        if (!listeners.contains(cl)) {
            listeners.add(cl);
        }
    }

    /**
     * Removes a listener for ChangeEvents.
     *
     * @param cl the ChangeListener to remove
     */
    public void removeChangeListener(ChangeListener cl) {
        listeners.remove(cl);
    }

    /**
     * Fire a change event to all listeners.
     */
    protected void fireChangeEvent() {
        repaint();
        if (changeEvent == null) {
            changeEvent = new ChangeEvent(this);
        }
        Iterator iter = listeners.iterator();
        while (iter.hasNext()) {
            ((ChangeListener) iter.next()).stateChanged(changeEvent);
        }
    }

    /**
     * @see java.awt.Component#getPreferredSize()
     */
    public Dimension getPreferredSize() {
        if (orientation == VERTICAL) {
            return new Dimension(PREFERRED_BREADTH, PREFERRED_LENGTH);
        } else {
            return new Dimension(PREFERRED_LENGTH, PREFERRED_BREADTH);
        }
    }

    // ------------------------------------------------------------------------
    // Rendering
    /**
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    public void paintComponent(Graphics g) {
        Rectangle bounds = getBounds();
        int width = (int) bounds.getWidth();
        int height = (int) bounds.getHeight();

        int min = toScreen(getLowValue());
        int max = toScreen(getHighValue());

        // Paint the full slider if the slider is marked as empty
        if (empty) {
            if (direction == LEFTRIGHT_TOPBOTTOM) {
                min = ARROW_SZ;
                max = (orientation == VERTICAL) ? height - ARROW_SZ : width - ARROW_SZ;
            } else {
                min = (orientation == VERTICAL) ? height - ARROW_SZ : width - ARROW_SZ;
                max = ARROW_SZ;
            }
        }

        Graphics2D g2 = (Graphics2D) g;

        paintTrack(g2, 0, 0, width, height);

        if (orientation == VERTICAL) {
            if (direction == LEFTRIGHT_TOPBOTTOM) {
                paintHandle(g2, 0, min - ARROW_SZ, width, ARROW_SZ, pick == PICK_LEFT_OR_TOP);
                paintThumb(g2, 0, min, width, max - min, pick == PICK_THUMB);
                paintHandle(g2, 0, max, width, ARROW_SZ, pick == PICK_RIGHT_OR_BOTTOM);
            } else {
                paintHandle(g2, 0, min, width, ARROW_SZ, pick == PICK_LEFT_OR_TOP);
                paintThumb(g2, 0, max, width, min - max, pick == PICK_THUMB);
                paintHandle(g2, 0, max - ARROW_SZ, width, ARROW_SZ, pick == PICK_RIGHT_OR_BOTTOM);
            }
        } else if (direction == LEFTRIGHT_TOPBOTTOM) {
            paintHandle(g2, min - ARROW_SZ, 0, ARROW_SZ, height, pick == PICK_LEFT_OR_TOP);
            paintThumb(g2, min, 0, max - min, height, pick == PICK_THUMB);
            paintHandle(g2, max, 0, ARROW_SZ, height, pick == PICK_RIGHT_OR_BOTTOM);
        } else {
            paintHandle(g2, min, 0, ARROW_SZ, height, pick == PICK_LEFT_OR_TOP);
            paintThumb(g2, max, 0, min - max, height, pick == PICK_THUMB);
            paintHandle(g2, max - ARROW_SZ, 0, ARROW_SZ, height, pick == PICK_RIGHT_OR_BOTTOM);
        }
    }

    private void paintTrack(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(hasFocus()?
                Utils.mix(getBackground(), getForeground(), 0.8) :
                Utils.mix(getBackground(), getForeground(), 0.6));
        g2.drawRect(x, y, w-1, h-1);

    }

    private void paintThumb(Graphics2D g2, int x, int y, int w, int h, boolean selected) {
        if (selected) {
            g2.setColor(Utils.mix(getBackground(), getForeground(), 0.4));
        } else {
            g2.setColor(Utils.mix(getBackground(), getForeground(), 0.3));
        }
        g2.fillRect(x, y, w, h);
    }

    private void paintHandle(Graphics2D g2, int x, int y, int w, int h, boolean selected) {
        if (selected) {
            g2.setColor(getForeground());
        } else {
            g2.setColor(Utils.mix(getBackground(), getForeground(), 0.8));
        }
        g2.fillRect(x, y, w, h);
    }

    /**
     * Converts from screen coordinates to a range value.
     */
    protected int toLocal(int xOrY) {
        Dimension sz = getSize();
        int min = getMinimum();
        double scale;
        if (orientation == VERTICAL) {
            scale = (sz.height - (2 * ARROW_SZ)) / (double) (getMaximum() - min);
        } else {
            scale = (sz.width - (2 * ARROW_SZ)) / (double) (getMaximum() - min);
        }

        if (direction == LEFTRIGHT_TOPBOTTOM) {
            return (int) (((xOrY - ARROW_SZ) / scale) + min + 0.5);
        } else if (orientation == VERTICAL) {
            return (int) ((sz.height - xOrY - ARROW_SZ) / scale + min + 0.5);
        } else {
            return (int) ((sz.width - xOrY - ARROW_SZ) / scale + min + 0.5);
        }
    }

    /**
     * Converts from a range value to screen coordinates.
     */
    protected int toScreen(int xOrY) {
        Dimension sz = getSize();
        int min = getMinimum();
        double scale;
        if (orientation == VERTICAL) {
            scale = (sz.height - (2 * ARROW_SZ)) / (double) (getMaximum() - min);
        } else {
            scale = (sz.width - (2 * ARROW_SZ)) / (double) (getMaximum() - min);
        }

        // If the direction is left/right_top/bottom then we subtract the min and multiply times scale
        // Otherwise, we have to invert the number by subtracting the value from the height
        if (direction == LEFTRIGHT_TOPBOTTOM) {
            return (int) (ARROW_SZ + ((xOrY - min) * scale) + 0.5);
        } else if (orientation == VERTICAL) {
            return (int) (sz.height - (xOrY - min) * scale - ARROW_SZ + 0.5);
        } else {
            return (int) (sz.width - (xOrY - min) * scale - ARROW_SZ + 0.5);
        }
    }

    /**
     * Converts from a range value to screen coordinates.
     */
    protected double toScreenDouble(int xOrY) {
        Dimension sz = getSize();
        int min = getMinimum();
        double scale;
        if (orientation == VERTICAL) {
            scale = (sz.height - (2 * ARROW_SZ)) / (double) (getMaximum() + 1 - min);
        } else {
            scale = (sz.width - (2 * ARROW_SZ)) / (double) (getMaximum() + 1 - min);
        }

        // If the direction is left/right_top/bottom then we subtract the min and multiply times scale
        // Otherwise, we have to invert the number by subtracting the value from the height
        if (direction == LEFTRIGHT_TOPBOTTOM) {
            return ARROW_SZ + ((xOrY - min) * scale);
        } else if (orientation == VERTICAL) {
            return sz.height - (xOrY - min) * scale - ARROW_SZ;
        } else {
            return sz.width - (xOrY - min) * scale - ARROW_SZ;
        }
    }

    // ------------------------------------------------------------------------
    // Event Handling
    static final int PICK_NONE = 0;
    static final int PICK_LEFT_OR_TOP = 1;
    static final int PICK_THUMB = 2;
    static final int PICK_RIGHT_OR_BOTTOM = 3;
    int pick;
    int pickOffsetLow;
    int pickOffsetHigh;
    int mouse;

    private int pickHandle(int xOrY) {
        int min = toScreen(getLowValue());
        int max = toScreen(getHighValue());
        int pick = PICK_NONE;

        if (direction == LEFTRIGHT_TOPBOTTOM) {
            if (xOrY < min) {
                pick = PICK_LEFT_OR_TOP;
            } else if ((xOrY >= min) && (xOrY <= max)) {
                pick = PICK_THUMB;
            } else if ((xOrY > max)) {
                pick = PICK_RIGHT_OR_BOTTOM;
            }
        } else if ((xOrY > min) && (xOrY < (min + ARROW_SZ))) {
            pick = PICK_LEFT_OR_TOP;
        } else if ((xOrY <= min) && (xOrY >= max)) {
            pick = PICK_THUMB;
        } else if ((xOrY > (max - ARROW_SZ) && (xOrY < max))) {
            pick = PICK_RIGHT_OR_BOTTOM;
        }

        return pick;
    }

    private void offset(int dxOrDy) {
        model.setValue(model.getValue() + dxOrDy);
    }

    /**
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    public void mousePressed(MouseEvent e) {
        if (orientation == VERTICAL) {
            pick = pickHandle(e.getY());
            pickOffsetLow = e.getY() - toScreen(getLowValue());
            pickOffsetHigh = e.getY() - toScreen(getHighValue());
            mouse = e.getY();
        } else {
            pick = pickHandle(e.getX());
            pickOffsetLow = e.getX() - toScreen(getLowValue());
            pickOffsetHigh = e.getX() - toScreen(getHighValue());
            mouse = e.getX();
        }
        repaint();
    }

    /**
     * @see
     * java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
     */
    public void mouseDragged(MouseEvent e) {
        requestFocus();
        int value = (orientation == VERTICAL) ? e.getY() : e.getX();

        int minimum = getMinimum();
        int maximum = getMaximum();
        int lowValue = getLowValue();
        int highValue = getHighValue();

        switch (pick) {
            case PICK_LEFT_OR_TOP:
                int low = toLocal(value - pickOffsetLow);

                if (low < minimum) {
                    low = minimum;
                }
                if (low > maximum - minExtent) {
                    low = maximum - minExtent;
                }
                if (low > highValue - minExtent) {
                    setRange(low, low + minExtent);
                } else {
                    setLowValue(low);
                }
                break;

            case PICK_RIGHT_OR_BOTTOM:
                int high = toLocal(value - pickOffsetHigh);

                if (high < minimum + minExtent) {
                    high = minimum + minExtent;
                }
                if (high > maximum) {
                    high = maximum;
                }
                if (high < lowValue + minExtent) {
                    setRange(high - minExtent, high);
                } else {
                    setHighValue(high);
                }
                break;

            case PICK_THUMB:
                int dxOrDy = toLocal(value - pickOffsetLow) - lowValue;
                if ((dxOrDy < 0) && ((lowValue + dxOrDy) < minimum)) {
                    dxOrDy = minimum - lowValue;
                }
                if ((dxOrDy > 0) && ((highValue + dxOrDy) > maximum)) {
                    dxOrDy = maximum - highValue;
                }
                if (dxOrDy != 0) {
                    offset(dxOrDy);
                }
                break;
        }
    }

    /**
     * @see
     * java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    public void mouseReleased(MouseEvent e) {
        pick = PICK_NONE;
        repaint();
    }

    /**
     * @see
     * java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
     */
    public void mouseMoved(MouseEvent e) {
        if (orientation == VERTICAL) {
            switch (pickHandle(e.getY())) {
                case PICK_LEFT_OR_TOP:
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    break;
                case PICK_RIGHT_OR_BOTTOM:
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    break;
                case PICK_THUMB:
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    break;
                case PICK_NONE:
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    break;
            }
        } else {
            switch (pickHandle(e.getX())) {
                case PICK_LEFT_OR_TOP:
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    break;
                case PICK_RIGHT_OR_BOTTOM:
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    break;
                case PICK_THUMB:
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    break;
                case PICK_NONE:
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    break;
            }
        }
    }

    /**
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    public void mouseClicked(MouseEvent e) {
    }

    /**
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    public void mouseEntered(MouseEvent e) {
    }

    /**
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    public void mouseExited(MouseEvent e) {
    }

    private void grow(int increment) {
        model.setRangeProperties(model.getValue() - increment,
                model.getExtent() + 2 * increment,
                model.getMinimum(), model.getMaximum(), false);
    }

    /**
     * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
     */
    public void keyPressed(KeyEvent e) {
        int kc = e.getKeyCode();
        boolean v = (orientation == VERTICAL);
        boolean d = (kc == KeyEvent.VK_DOWN);
        boolean u = (kc == KeyEvent.VK_UP);
        boolean l = (kc == KeyEvent.VK_LEFT);
        boolean r = (kc == KeyEvent.VK_RIGHT);

        int minimum = getMinimum();
        int maximum = getMaximum();
        int lowValue = getLowValue();
        int highValue = getHighValue();

        if (v && r || !v && u) {
            if (lowValue - increment >= minimum
                    && highValue + increment <= maximum) {
                grow(increment);
            }
        } else if (v && l || !v && d) {
            if (highValue - lowValue >= 2 * increment) {
                grow(-1 * increment);
            }
        } else if (v && d || !v && l) {
            if (lowValue - increment >= minimum) {
                offset(-increment);
            }
        } else if (v && u || !v && r) {
            if (highValue + increment <= maximum) {
                offset(increment);
            }
        }
    }

    /**
     * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
     */
    public void keyReleased(KeyEvent e) {
    }

    /**
     * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
     */
    public void keyTyped(KeyEvent e) {
    }

} // end of class JRangeSlider
