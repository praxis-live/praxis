/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2012 Neil C Smith.
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
package net.neilcsmith.praxis.texteditor;

import javax.swing.JComponent;
import javax.swing.text.JTextComponent;
import net.neilcsmith.praxis.texteditor.rsta.RSTATextEditor;

/**
 *
 * @author Neil C Smith
 */
public abstract class TextEditor {
    
    /**
     * Get the main Swing component for this editor that can be added to a 
     * Swing container.
     * @return JComponent editor.
     */
    public abstract JComponent getEditorComponent();
    
    /**
     * Get the primary text component for this editor.  The text component should
     * be a child of the editor component or the editor component itself.  If a 
     * child, it should already be added to the editor component hierarchy.
     * @return JTextComponent primary text component.
     */
    public abstract JTextComponent getTextComponent();
    
    /**
     * Create a TextEditor for the given MIME type.  If the MIME type is null or
     * unknown, returns a plain text editor.
     * @param mimeType
     * @param initialText 
     * @return TextEditor implementation
     */
    public static TextEditor create(String mimeType, String initialText) {
        if (mimeType == null) {
            mimeType = "text/plain";
        }
        return RSTATextEditor.create(mimeType, initialText);
    }
    
}
