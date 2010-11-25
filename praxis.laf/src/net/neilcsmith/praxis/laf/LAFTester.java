/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Neil C Smith.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License,
 * or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this work; if not, see http://www.gnu.org/licenses/
 * 
 *
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 *
 */

package net.neilcsmith.praxis.laf;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 *
 * @author Neil C Smith
 */
public class LAFTester {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                PraxisLAFManager.getInstance().installUI();
                final JFrame frame = new JFrame("Test");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                Box box = Box.createHorizontalBox();
                frame.add(box);
                JButton button = new JButton("Open");
                final JTextField field = new JTextField(30);
                field.setEditable(false);
                button.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        JFileChooser chooser = new JFileChooser();
                        int result = chooser.showOpenDialog(frame);
                        if (result == JFileChooser.APPROVE_OPTION) {
                            field.setText(new File(chooser.getSelectedFile().toURI()).getName());
                        } else {
                            field.setText("No File");
                        }
                    }
                });
                box.add(button);
                box.add(field);
                frame.pack();
                frame.setVisible(true);
            }
        });
    }

}
