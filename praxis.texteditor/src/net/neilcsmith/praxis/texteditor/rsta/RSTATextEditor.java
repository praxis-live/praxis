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
package net.neilcsmith.praxis.texteditor.rsta;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.InputStream;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;
import net.neilcsmith.praxis.texteditor.TextEditor;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.modes.PlainTextTokenMaker;
import org.fife.ui.rtextarea.RTADefaultInputMap;
import org.fife.ui.rtextarea.RTextScrollPane;

/**
 *
 * @author Neil C Smith
 */
//@TODO switch to TokenMakerFactory
public class RSTATextEditor extends TextEditor {

    private static Theme theme;
    private RTextScrollPane scrollPane;
    private RSyntaxTextArea rsta;

    private RSTATextEditor(String mime, String initialText) {
        rsta = new RSyntaxTextArea();
        scrollPane = new RTextScrollPane(rsta);
        initTextArea(mime, initialText);
        initScrollPane();
    }

    private void initTextArea(String mime, String text) {
        String syntax = getSyntaxStyle(mime);
        if (syntax != null) {
            rsta.setSyntaxEditingStyle(syntax);
        } else if ("text/x-glsl-frag".equals(mime) ||
                "text/x-glsl-vert".equals(mime)) {
            ((RSyntaxDocument) rsta.getDocument()).setSyntaxStyle(new GLSLTokenMaker());
        } else {
            ((RSyntaxDocument) rsta.getDocument()).setSyntaxStyle(new ExtendedPlainTokenMaker());
        }
        
        if (theme == null) {
            initTheme();
        }
        theme.apply(rsta);

        rsta.setTabsEmulated(true);
        rsta.setTabSize(2);
//        rsta.setMarkOccurrences(true); @TODO fix highlight colour
        rsta.setCodeFoldingEnabled(true);
        rsta.setClearWhitespaceLinesEnabled(true);

        rsta.setText(text);
        rsta.discardAllEdits();
        rsta.setCaretPosition(0);

        removeCtrlEnterBinding(rsta);

    }

    private void initScrollPane() {
//        scrollPane.setLineNumbersEnabled(true);
    }

    private void removeCtrlEnterBinding(RSyntaxTextArea rsta) {
        InputMap im = rsta.getInputMap();
        while (im != null) {
            if (im instanceof RTADefaultInputMap) {
                im.remove(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            }
            im = im.getParent();
        }
    }

    @Override
    public JComponent getEditorComponent() {
        return scrollPane;
    }

    @Override
    public JTextComponent getTextComponent() {
        return rsta;
    }

    private static String getSyntaxStyle(String mime) {
        if ("text/x-praxis-java".equals(mime)) {
            return SyntaxConstants.SYNTAX_STYLE_JAVA;
        } else if ("text/plain".equals(mime)) {
            return SyntaxConstants.SYNTAX_STYLE_NONE;
        } else {
            return null;
        }
    }

    private static void initTheme() {
        try {
            InputStream in = RSTATextEditor.class.getResourceAsStream("dark.xml");
            theme = Theme.load(in);
        } catch (Exception ex) {
        }
    }

    public static RSTATextEditor create(String mimeType, String initialText) {
        return new RSTATextEditor(mimeType, initialText);
    }

    private static class ExtendedPlainTokenMaker extends PlainTextTokenMaker {

        @Override
        public boolean getCurlyBracesDenoteCodeBlocks() {
            return true;
        }

        @Override
        public boolean getShouldIndentNextLineAfter(Token t) {
            if (t != null && t.textCount == 1) {
                char ch = t.text[t.textOffset];
                return ch == '{' || ch == '(';
            }
            return false;
        }
    }
}
