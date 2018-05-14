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
package org.praxislive.terminal;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import net.miginfocom.swing.MigLayout;
import org.praxislive.core.Value;
import org.praxislive.core.CallArguments;
import org.praxislive.core.types.PReference;
import org.praxislive.texteditor.TextEditor;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class Terminal extends JComponent {

    private final static Logger LOG = Logger.getLogger(Terminal.class.getName());

    private final static String SCRIPT = "script";
    private final static String OK = "response";
    private final static String OK_PREFIX = " -- ";
    private final static String ERROR = "error";
    private final static String ERROR_PREFIX = " -- ERROR -- ";


    private Context context;
    private StyledDocument history;
    private TextEditor input;
    private JButton evalButton;
    private Action evalAction;
    private JButton clearButton;
    private Action clearAction;

    public Terminal() {
        this(null);
    }

    public Terminal(Context context) {
        buildActions();
        buildUI();
        buildStyles();
        setContext(context);
    }

    public final void setContext(Context context) {
        if (context != this.context) {
            this.context = context;
            evalAction.setEnabled(true);
        }
    }

    public Context getContext() {
        return context;
    }

    private void buildActions() {
        evalAction = new RunAction();
        clearAction = new ClearAction();
    }

    private void buildUI() {
        setLayout(new MigLayout("fill"));
        history = new DefaultStyledDocument();
        JTextPane hTextPane = new JTextPane(history);
        hTextPane.setEditable(false);        
        JScrollPane hPane = new JScrollPane(hTextPane,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        hPane.setMinimumSize(new Dimension(300, 200));
        hPane.setPreferredSize(new Dimension(450, 250));
        input = TextEditor.create("text/x-praxis-script", "");
        JComponent iPane = input.getEditorComponent();
        iPane.setMinimumSize(new Dimension(300, 200));
        JSplitPane splPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                hPane, iPane);
        splPane.setContinuousLayout(true);
        splPane.setOneTouchExpandable(true);
        splPane.setResizeWeight(0.8);
//        splPane.setPreferredSize(new Dimension(600, 450));
        add(splPane, "grow, push, wrap");

        clearButton = new JButton(clearAction);
        add(clearButton, "split, align right");
        evalButton = new JButton(evalAction);
        add(evalButton, "");

        Keymap map = JTextComponent.addKeymap("PraxisTerminal", input.getTextComponent().getKeymap());
        map.addActionForKeyStroke(KeyStroke.getKeyStroke("control ENTER"), evalAction);
        input.getTextComponent().setKeymap(map);

    }

    private void buildStyles() {
        Style def = StyleContext.getDefaultStyleContext().
                        getStyle(StyleContext.DEFAULT_STYLE);
        Style s = history.addStyle(SCRIPT, def);

        s = history.addStyle(OK, def);
//        StyleConstants.setItalic(s, true);
        StyleConstants.setForeground(s, Color.GREEN);

        s = history.addStyle(ERROR, def);
//        StyleConstants.setItalic(s, true);
        StyleConstants.setForeground(s, Color.RED);


    }

    @Override
    public void requestFocus() {
        input.getTextComponent().requestFocus();
    }

    @Override
    public boolean requestFocusInWindow() {
        return input.getTextComponent().requestFocusInWindow();
    }

    public void processResponse(CallArguments args) {
        try {
            history.insertString(history.getLength(),
                    argsToString(args, OK_PREFIX),
                    history.getStyle(OK));
        } catch (BadLocationException ex) {
            LOG.log(Level.FINE, "Unexpected Exception in Terminal", ex);
        }
        evalAction.setEnabled(true);
    }

    public void processError(CallArguments args) {
        try {
            history.insertString(history.getLength(),
                    argsToString(args, ERROR_PREFIX),
                    history.getStyle(ERROR));
        } catch (BadLocationException ex) {
            LOG.log(Level.FINE, "Unexpected Exception in Terminal", ex);
        }
        if (args.getSize() > 0) {
                Value err = args.get(0);
                if (err instanceof PReference) {
                    Object o = ((PReference) err).getReference();
                    if (o instanceof Throwable) {
                        LOG.log(
                                Level.WARNING, "ERROR: ", (Throwable) o);
                    } else {
                        LOG.log(Level.WARNING, "ERROR: {0}", o.toString());
                    }
                } else {
                    LOG.log(Level.WARNING, "ERROR: {0}", err.toString());
                }
            }

        evalAction.setEnabled(true);
    }

    private String argsToString(CallArguments args, String prefix) {
        StringBuilder builder = new StringBuilder(prefix);
        for (int i=0; i < args.getSize(); i++) {
            if (i != 0) {
                builder.append(" ");
            }
            builder.append(args.get(i));
        }
        builder.append("\n");
        return builder.toString();
    }

    private class RunAction extends AbstractAction {

        private RunAction() {
            super("Run (Ctrl+ENTER)");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent text = input.getTextComponent();
            text.selectAll();
            text.requestFocusInWindow();
            String script = text.getText();
            script = script.trim();
            if (script.isEmpty()) {
                text.setText(null);
                return;
            }
            try {
                history.insertString(history.getLength(), script + "\n", history.getStyle(SCRIPT));
            } catch (BadLocationException ex) {
                LOG.log(Level.FINE, "Unexpected Exception in Terminal", ex);
            }
            try {
                context.eval(script);
                setEnabled(false);
            } catch (Exception ex) {
                LOG.log(Level.WARNING, "", ex);
                try {
                    history.insertString(history.getLength(),
                            ERROR_PREFIX + "No Script Context Available\n",
                            history.getStyle(ERROR));
                } catch (BadLocationException ex1) {
                    LOG.log(Level.FINE, "Unexpected Exception in Terminal", ex1);
                }
            }
        }
    }

    private class ClearAction extends AbstractAction {

        private ClearAction() {
            super("Clear");
        }

        public void actionPerformed(ActionEvent e) {
            try {
                history.remove(0, history.getLength());
            } catch (BadLocationException ex) {
                LOG.log(Level.FINE, "Unexpected Exception in Terminal", ex);
            }
            input.getTextComponent().setText("");
            requestFocusInWindow();
             try {
                context.clear();
            } catch (Exception ex) {
                LOG.log(Level.WARNING, "", ex);
                try {
                    history.insertString(history.getLength(),
                            ERROR_PREFIX + "No Script Context Available\n",
                            history.getStyle(ERROR));
                } catch (BadLocationException ex1) {
                    LOG.log(Level.FINE, "Unexpected Exception in Terminal", ex1);
                }
            }
        }

    }

    public static interface Context {

        public void eval(String script) throws Exception;

        public void clear() throws Exception;
    }
}