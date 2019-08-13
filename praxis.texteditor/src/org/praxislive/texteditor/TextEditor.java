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
package org.praxislive.texteditor;

import java.awt.EventQueue;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.text.JTextComponent;

/**
 *
 * @author Neil C Smith
 */
@Deprecated
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
        return new DefaultTextEditor(mimeType, initialText);
    }
    
 
    private static class DefaultTextEditor extends TextEditor {
        
        private final String mimeType;
        private final String initialText;
        
        private JScrollPane scrollPane;
        private JEditorPane editorPane;
        
        private DefaultTextEditor(String mimeType, String initialText) {
            this.mimeType = mimeType;
            this.initialText = initialText;
        }

        @Override
        public JComponent getEditorComponent() {
            assert EventQueue.isDispatchThread();
            if (scrollPane == null) {
                init();
            }
            return scrollPane;
        }

        @Override
        public JTextComponent getTextComponent() {
            assert EventQueue.isDispatchThread();
            if (editorPane == null) {
                init();
            }
            return editorPane;
        }

        private void init() {
            assert scrollPane == null && editorPane == null;
            editorPane = new JEditorPane(mimeType, initialText);
            scrollPane = new JScrollPane(editorPane);
        }
        
        
        
        
    }
    
}
