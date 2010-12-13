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
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.EnumSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.Call;
import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.ControlAddress;
import net.neilcsmith.praxis.core.IllegalRootStateException;
import net.neilcsmith.praxis.core.ArgumentFormatException;
import net.neilcsmith.praxis.core.ComponentAddress;
import net.neilcsmith.praxis.core.PacketRouter;
import net.neilcsmith.praxis.core.interfaces.ServiceUnavailableException;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.core.interfaces.ScriptService;
import net.neilcsmith.praxis.core.types.PReference;
import net.neilcsmith.praxis.core.types.PString;
import net.neilcsmith.praxis.impl.AbstractControl;
import net.neilcsmith.praxis.impl.AbstractRoot;
import net.neilcsmith.praxis.impl.BasicControl;
import net.neilcsmith.praxis.impl.RootState;

/**
 *
 * @author Neil C Smith
 */
public class Player extends AbstractRoot {

    private final Object lock = new Object();
    private JFrame mainFrame;
    private JFrame scriptFrame;
    private JFrame terminalFrame;
    private Terminal terminal;
//    private JComponent container;
    private Timer timer;
    private Action newAction;
    private Action openAction;
    private Action clearAction;
    private Action quitAction;
    private Action terminalAction;
    private Action aboutAction;
    private ScriptControl scriptControl;
    private File currentFile;
    private JLabel status;
    private String script;

    public Player() {
        super(EnumSet.noneOf(Caps.class));
    }

    public Player(String script) {
        this.script = script;
    }

    @Override
    protected final void run() {
        try {
            EventQueue.invokeAndWait(new Runnable() {

                public void run() {
                    initialize();
                }
            });
        } catch (Exception ex) {
            return;
        }
        timer = new Timer(50, new TimerProcessor());
        timer.start();
        RootState st;
        while ((st = getState()) == RootState.ACTIVE_RUNNING || st == RootState.ACTIVE_IDLE) {
            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException ex) {
                    System.out.println("Shouldn't be called");
                }
            }
        }
    }

    protected void initialize() {

        scriptControl = new ScriptControl();
        registerControl("_script-control", scriptControl);
        buildActions();
        mainFrame = buildMainFrame();
        mainFrame.setVisible(true);
        if (script != null) {
            scriptControl.runScript(script, false);
        }

    }

    private void buildActions() {
        newAction = new NewAction();
        openAction = new OpenAction();
        clearAction = new ClearAction();
        quitAction = new QuitAction();
        terminalAction = new TerminalAction();
        aboutAction = new AboutAction();
    }

    private JFrame buildMainFrame() {
        JFrame frame = new JFrame("Praxis Player");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setJMenuBar(buildMenuBar());
        frame.add(buildMainPanel());
        frame.setMinimumSize(new Dimension(250, 125));
        frame.pack();
        frame.setResizable(false);
        return frame;
    }

    private JMenuBar buildMenuBar() {
        JMenuBar menu = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
//        fileMenu.add(newAction);
        fileMenu.add(openAction);
        fileMenu.add(clearAction);
        fileMenu.add(quitAction);
        menu.add(fileMenu);
        JMenu toolsMenu = new JMenu("Tools");
        toolsMenu.setMnemonic(KeyEvent.VK_T);
        toolsMenu.add(terminalAction);
        menu.add(toolsMenu);
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);
        helpMenu.add(aboutAction);
        menu.add(helpMenu);
        return menu;
    }

    private JComponent buildMainPanel() {
        Box logo = Box.createHorizontalBox();
        logo.add(Box.createHorizontalGlue());
        URL imageLoc = getClass().getClassLoader().getResource("net/neilcsmith/praxis/player/logo.png");
        logo.add(new JLabel(new ImageIcon(imageLoc)));
        logo.add(Box.createHorizontalGlue());
        logo.setOpaque(true);
//        logo.setBackground(new Color(0.96f, 0.96f, 0.96f));
        logo.setBackground(Color.BLACK);
        Box bar = Box.createHorizontalBox();
        status = new JLabel("Status : idle");
        bar.add(status);
        bar.add(Box.createHorizontalGlue());
        bar.add(new JButton(clearAction));
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.BLACK),
                BorderFactory.createEmptyBorder(4, 2, 2, 2)));
        Box bx = Box.createVerticalBox();
        bx.add(logo);
        bx.add(bar);
        return bx;
    }

    private void showTerminal() {
        if (terminalFrame == null) {
            terminalFrame = new JFrame("Praxis - Terminal");
            TerminalContext ctxt = new TerminalContext();
            registerControl("_terminal", ctxt);
            terminal = new Terminal(ctxt);
            terminalFrame.add(terminal);
            terminalFrame.pack();

        }
        terminalFrame.setVisible(true);
        terminalFrame.toFront();
        terminal.requestFocusInWindow();


    }

    private void showScriptDialog(boolean openFile) {
        final JTextArea script = new JTextArea(12, 40);
        if (openFile) {
            JFileChooser fileDialog = new JFileChooser(currentFile);
            int ret = fileDialog.showOpenDialog(mainFrame);
            if (ret == JFileChooser.APPROVE_OPTION) {
                try {
                    currentFile = fileDialog.getSelectedFile();
                    script.read(new FileReader(currentFile), null);
                    script.insert("set _PWD " + currentFile.toURI() + "\n", 0);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(mainFrame, "Error loading file", "ERROR", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else {
                return;
            }
        }
        script.addHierarchyListener(new HierarchyListener() {

            public void hierarchyChanged(HierarchyEvent e) {
                Window window = SwingUtilities.getWindowAncestor(script);
                if (window instanceof Dialog) {
                    Dialog dialog = (Dialog) window;
                    if (!dialog.isResizable()) {
                        dialog.setResizable(true);
                    }
                }
            }
        });
        JScrollPane scrollPane = new JScrollPane(script);
        int ret = JOptionPane.showOptionDialog(mainFrame, scrollPane, "Script",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, new String[]{"Run", "Clear and Run", "Cancel"}, "Run");
        if (ret == 0) {
            scriptControl.runScript(script.getText(), false);
        } else if (ret == 1) {
            scriptControl.runScript(script.getText(), true);
        }
    }

    private class TimerProcessor implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            try {
                nextControlFrame(System.nanoTime());
            } catch (IllegalRootStateException ex) {
                timer.stop();
                synchronized (lock) {
                    lock.notifyAll();
                }
            }

        }
    }

    private class ScriptControl extends BasicControl {

        ControlAddress evalControl;
        ControlAddress clearControl;
        boolean pendingClear;
        String pendingScript;
        Call activeCall;

        ScriptControl() {
            super(Player.this);
            try {
                ComponentAddress ss = getServiceManager().findService(
                        ScriptService.INSTANCE);
                evalControl = ControlAddress.create(ss, ScriptService.EVAL);
                clearControl = ControlAddress.valueOf("/praxis.clear");
            } catch (ServiceUnavailableException ex) {
                Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ArgumentFormatException ex) {
                Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        protected void processError(Call call) throws Exception {
            if (activeCall != null && call.getMatchID() == activeCall.getMatchID()) {
                pendingClear = false;
                pendingScript = null;
                activeCall = null;
//                activeCall = Call.createCall(clearControl, getAddress(), System.nanoTime(), CallArguments.EMPTY);
//                getCallRouter().routeCall(activeCall);
                newAction.setEnabled(true);
                openAction.setEnabled(true);
                status.setText("Status : error");
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        JOptionPane.showMessageDialog(mainFrame, "An error was encountered in running the script",
                                "ERROR", JOptionPane.ERROR_MESSAGE);
                    }
                });
            }
            CallArguments args = call.getArgs();
            if (args.getSize() > 0) {
                Argument err = args.get(0);
                if (err instanceof PReference) {
                    Object o = ((PReference) err).getReference();
                    if (o instanceof Throwable) {
                        Logger.getLogger(Player.class.getName()).log(
                                Level.SEVERE, "ERROR: ", (Throwable) o);
                    } else {
                        Logger.getLogger(Player.class.getName()).severe("ERROR: " + o.toString());
                    }
                } else {
                    Logger.getLogger(Player.class.getName()).severe("ERROR: " + err.toString());
                }
            }
        }

        @Override
        protected void processReturn(Call call) throws Exception {
            if (activeCall != null && call.getMatchID() == activeCall.getMatchID()) {
                if (pendingClear) {
                    status.setText("Status : clearing");
                    pendingClear = false;
                    activeCall = Call.createCall(clearControl, getAddress(), System.nanoTime(), CallArguments.EMPTY);
                    getPacketRouter().route(activeCall);
                } else if (pendingScript != null) {
                    PString scr = PString.valueOf(pendingScript);
                    pendingScript = null;
                    status.setText("Status : executing");
                    activeCall = Call.createCall(evalControl, getAddress(), System.nanoTime(), scr);
                    getPacketRouter().route(activeCall);
                } else {
                    if (call.getFromAddress().equals(clearControl)) {
                        status.setText("Status : idle");
                    } else if (call.getFromAddress().equals(evalControl)) {
                        status.setText("Status : running");
                    }
                    activeCall = null;
                    newAction.setEnabled(true);
                    openAction.setEnabled(true);
                }
            }
        }

        private void clear() {
            if (activeCall != null) {
                pendingClear = true;
            } else {
                status.setText("Status : clearing");
                activeCall = Call.createCall(clearControl, getAddress(), System.nanoTime(), CallArguments.EMPTY);
                getPacketRouter().route(activeCall);
            }
        }

        private void runScript(String script, boolean clearFirst) {
            newAction.setEnabled(false);
            openAction.setEnabled(false);
            if (clearFirst) {
                clear();
            }
            if (activeCall == null) {
                activeCall = Call.createCall(evalControl, getAddress(), System.nanoTime(), PString.valueOf(script));
                getPacketRouter().route(activeCall);
            } else {
                pendingScript = script;
            }
        }

        public ControlInfo getInfo() {
            return null;
        }
    }

    private class NewAction extends AbstractAction {

        private NewAction() {
            super("New");
            putValue(Action.MNEMONIC_KEY, KeyEvent.VK_N);
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control N"));
        }

        public void actionPerformed(ActionEvent e) {
            showScriptDialog(false);
        }
    }

    private class OpenAction extends AbstractAction {

        private OpenAction() {
            super("Open");
            putValue(Action.MNEMONIC_KEY, KeyEvent.VK_O);
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control O"));
        }

        public void actionPerformed(ActionEvent e) {
            showScriptDialog(true);
        }
    }

    private class ClearAction extends AbstractAction {

        private ClearAction() {
            super("Clear");
            putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control Z"));
        }

        public void actionPerformed(ActionEvent e) {
            scriptControl.clear();
        }
    }

    private class QuitAction extends AbstractAction {

        private QuitAction() {
            super("Quit");
            putValue(Action.MNEMONIC_KEY, KeyEvent.VK_Q);
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control Q"));
        }

        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    }

    private class TerminalAction extends AbstractAction {

        private TerminalAction() {
            super("Terminal");
            putValue(Action.MNEMONIC_KEY, KeyEvent.VK_T);
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control T"));
        }

        public void actionPerformed(ActionEvent e) {
            showTerminal();
        }
    }

    private class TerminalContext extends AbstractControl implements Terminal.Context {

        private Call activeCall;

        public void eval(String script) throws Exception {
            ControlAddress to = ControlAddress.create(
                    findService(ScriptService.INSTANCE),
                    ScriptService.EVAL);
            Call call = Call.createCall(to, getAddress(), System.nanoTime(), PString.valueOf(script));
            getPacketRouter().route(call);
            activeCall = call;
        }

        public void clear() throws Exception {
            ControlAddress to = ControlAddress.create(
                    findService(ScriptService.INSTANCE),
                    ScriptService.CLEAR);
            Call call = Call.createQuietCall(to, getAddress(), System.nanoTime(), CallArguments.EMPTY);
            getPacketRouter().route(call);
            activeCall = null;
        }

        public void call(Call call, PacketRouter router) throws Exception {
            switch (call.getType()) {
                case RETURN :
                    if (call.getMatchID() == activeCall.getMatchID()) {
                        terminal.processResponse(call.getArgs());
                        activeCall = null;
                    }
                    break;
                case ERROR :
                    if (call.getMatchID() == activeCall.getMatchID()) {
                        terminal.processError(call.getArgs());
                        activeCall = null;
                    }
                    break;
                default :

            }
        }

        public ControlInfo getInfo() {
            return null;
        }
    }

    private class AboutAction extends AbstractAction {

        private String aboutText;

        private AboutAction() {
            super("About");
            putValue(Action.MNEMONIC_KEY, KeyEvent.VK_A);
            String previewVersion = System.getProperty("net.neilcsmith.praxisplayer.version");
            if (previewVersion == null) {
                aboutText = "Praxis\n© 2010 Neil C Smith\nhttp://neilcsmith.net";
            } else {
                aboutText = "Praxis - build:" + previewVersion + "\n© 2010 Neil C Smith\n" +
                        "http://neilcsmith.net";
            }
        }

        public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(mainFrame, aboutText, "About", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}


