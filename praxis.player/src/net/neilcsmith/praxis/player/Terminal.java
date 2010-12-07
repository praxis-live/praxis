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
package net.neilcsmith.praxis.player;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import net.miginfocom.swing.MigLayout;
import net.neilcsmith.praxis.core.CallArguments;
import org.openide.util.Exceptions;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class Terminal extends JComponent {

    private final static String SCRIPT = "script";
    private final static String OK = "response";
    private final static String OK_PREFIX = " -- OK : ";
    private final static String ERROR = "error";
    private final static String ERROR_PREFIX = " -- ERROR : ";


    private Context context;
//    private JTextPane hTextPane;
    private StyledDocument history;
    private JTextArea input;
    private JButton evalButton;
    private Action evalAction;
    private JButton clearButton;
    private Action clearAction;

    public Terminal(Context context) {
        if (context == null) {
            throw new NullPointerException();
        }
        this.context = context;
        buildActions();
        buildUI();
        buildStyles();
    }

    private void buildActions() {
        evalAction = new EvalAction();
        clearAction = new ClearAction();
    }

    private void buildUI() {
        setLayout(new MigLayout("fill"));
        history = new DefaultStyledDocument();
        JTextPane hTextPane = new JTextPane(history);
        hTextPane.setEditable(false);
        input = new JTextArea();
        JScrollPane hPane = new JScrollPane(hTextPane,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        hPane.setMinimumSize(new Dimension(200, 100));
        hPane.setPreferredSize(new Dimension(450, 250));
        JScrollPane iPane = new JScrollPane(input,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        iPane.setMinimumSize(new Dimension(200, 100));
        hPane.setPreferredSize(new Dimension(450, 250));
        JSplitPane splPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                hPane, iPane);
        splPane.setContinuousLayout(true);
        splPane.setOneTouchExpandable(true);
        splPane.setResizeWeight(0.8);
//        splPane.setPreferredSize(new Dimension(600, 450));
        add(splPane, "grow, push, wrap");

        clearButton = new JButton(clearAction);
        add(clearButton, "split, growprio 0, align right");
        evalButton = new JButton(evalAction);
        add(evalButton, "growprio 0");

        input.getKeymap().addActionForKeyStroke(KeyStroke.getKeyStroke("control ENTER"), evalAction);

    }

    private void buildStyles() {
        Style def = StyleContext.getDefaultStyleContext().
                        getStyle(StyleContext.DEFAULT_STYLE);
        Style s = history.addStyle(SCRIPT, def);

        s = history.addStyle(OK, def);
        StyleConstants.setItalic(s, true);
        StyleConstants.setForeground(s, Color.GREEN);

        s = history.addStyle(ERROR, def);
        StyleConstants.setItalic(s, true);
        StyleConstants.setForeground(s, Color.RED);


    }

    @Override
    public void requestFocus() {
        input.requestFocus();
    }

    @Override
    public boolean requestFocusInWindow() {
        return input.requestFocusInWindow();
    }

    public void processResponse(CallArguments args) {
        try {
            history.insertString(history.getLength(),
                    argsToString(args, OK_PREFIX),
                    history.getStyle(OK));
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        evalAction.setEnabled(true);
    }

    public void processError(CallArguments args) {
        try {
            history.insertString(history.getLength(),
                    argsToString(args, ERROR_PREFIX),
                    history.getStyle(ERROR));
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        evalAction.setEnabled(true);
    }

    private String argsToString(CallArguments args, String prefix) {
        StringBuilder builder = new StringBuilder(prefix);
        for (int i=0; i < args.getCount(); i++) {
            if (i != 0) {
                builder.append(" ");
            }
            builder.append(args.getArg(i));
        }
        builder.append("\n");
        return builder.toString();
    }

    private class EvalAction extends AbstractAction {

        private EvalAction() {
            super("Evaluate (Ctrl+ENTER)");

        }

        public void actionPerformed(ActionEvent e) {
            input.selectAll();
            input.requestFocusInWindow();
            String script = input.getText();
            script = script.trim();
            if (script.isEmpty()) {
                input.setText(null);
                return;
            }
            try {
                history.insertString(history.getLength(), script + "\n", history.getStyle(SCRIPT));
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
            try {
                context.eval(script);
                setEnabled(false);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
                try {
                    history.insertString(history.getLength(),
                            ERROR_PREFIX + "No Script Context Available\n",
                            history.getStyle(ERROR));
                } catch (BadLocationException ex1) {
                    Exceptions.printStackTrace(ex1);
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
                Exceptions.printStackTrace(ex);
            }
            input.setText(null);
            input.requestFocusInWindow();
             try {
                context.clear();
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
                try {
                    history.insertString(history.getLength(),
                            ERROR_PREFIX + "No Script Context Available\n",
                            history.getStyle(ERROR));
                } catch (BadLocationException ex1) {
                    Exceptions.printStackTrace(ex1);
                }
            }
        }

    }

    public static interface Context {

        public void eval(String script) throws Exception;

        public void clear() throws Exception;
    }
}
