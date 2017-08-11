/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2017 Neil C Smith.
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
package net.neilcsmith.praxis.video.pgl;

import com.jogamp.common.util.IOUtil;
import com.jogamp.newt.NewtFactory;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.newt.event.WindowListener;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.util.FPSAnimator;
import processing.core.PGraphics;
import processing.opengl.PSurfaceJOGL;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
class PGLGraphicsPSurface extends PSurfaceJOGL {

    PGLGraphicsPSurface(PGraphics graphics) {
        super(graphics);
    }

    @Override
    protected void initAnimator() {
        animator = new FPSAnimator(window, 60);
//            animator.setUncaughtExceptionHandler(new GLAnimatorControl.UncaughtExceptionHandler() {
//
//                @Override
//                public void uncaughtException(GLAnimatorControl glac, GLAutoDrawable glad, Throwable thrwbl) {
//                    assert false;
//                }
//            });
    }

    @Override
    protected void initWindow() {
        super.initWindow();
        if (!sketch.sketchFullScreen()) {
            window.setResizable(true);
        }
    }

    @Override
    protected void initIcons() {
        String[] files = new String[] {
            "icons/praxislive16.png",
            "icons/praxislive32.png",
            "icons/praxislive48.png",
            "icons/praxislive128.png",
        };
        NewtFactory.setWindowIcons(new IOUtil.ClassResources(files, this.getClass().getClassLoader(), this.getClass()));
    }

    
    
    @Override
    public boolean stopThread() {
        boolean stopped = super.stopThread();
        if (stopped) {
            if (window != null) {
                final GLWindow win = window;
                display.getEDTUtil().invoke(false, new Runnable() {

                    @Override
                    public void run() {
                        for (MouseListener l : win.getMouseListeners()) {
                            win.removeMouseListener(l);
                        }
                        for (KeyListener l : win.getKeyListeners()) {
                            win.removeKeyListener(l);
                        }
                        for (WindowListener l : win.getWindowListeners()) {
                            win.removeWindowListener(l);
                        }
                        win.removeGLEventListener(win.getGLEventListener(0));
                        win.destroy();
                    }
                });

            }
        }
        return stopped;
    }
}
