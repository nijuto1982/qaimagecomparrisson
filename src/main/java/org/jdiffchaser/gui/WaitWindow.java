/*
 * This file is part of jDiffChaser.
 *
 *  jDiffChaser is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  jDiffChaser is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with jDiffChaser; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.jdiffchaser.gui;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Toolkit;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;


public class WaitWindow extends javax.swing.JWindow {
    
    private JPanel mainPanel;
    private JLabel message;
    private JProgressBar waitProgress;
    
    private static final int WIDTH_MARGIN = 20;
    
    /** Creates new form WaitDialog */
    public WaitWindow(java.awt.Frame parent, String msg) {
        super(parent);
        initComponents();
        initialize(msg);
        setXy();
    }
    
    private void initialize(String msg){
        FontMetrics metrix = message.getFontMetrics(message.getFont());
        int width = metrix.stringWidth(msg)+ WIDTH_MARGIN;
        Dimension dim = new Dimension(width, this.getHeight());
        this.setSize(dim);
        mainPanel.setPreferredSize(dim);
        mainPanel.setMinimumSize(dim);
        this.message.setText(msg);
    }

    private void setXy(){
        int x = 0;
        int y = 0;
        if(this.getParent()!=null){
            Point parentLoc = this.getParent().getLocation();
            Dimension parentSize = this.getParent().getSize();
            x = (int) (parentSize.getWidth()/2.0d - this.getWidth()/2.0d) + parentLoc.x;
            y = (int) (parentSize.getHeight()/2.0d - this.getHeight()/2.0d) + parentLoc.y;
        }else{
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            x = (int) (screenSize.getWidth()/2.0d - this.getWidth()/2.0d);
            y = (int) (screenSize.getHeight()/2.0d - this.getHeight()/2.0d);
        }
        this.setLocation(x,y);    
    }

    private void initComponents() {
        mainPanel = new javax.swing.JPanel();
        mainPanel.setBorder(BorderFactory.createEtchedBorder());
        waitProgress = new javax.swing.JProgressBar();
        message = new javax.swing.JLabel();
        
        mainPanel.setLayout(new java.awt.BorderLayout());

        waitProgress.setIndeterminate(true);
        waitProgress.setMaximumSize(new java.awt.Dimension(148, 14));
        waitProgress.setMinimumSize(new java.awt.Dimension(148, 14));
        mainPanel.add(waitProgress, java.awt.BorderLayout.SOUTH);

        message.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        message.setText("waiting message...");
        message.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(1, 5, 1, 5)));
        mainPanel.add(message, java.awt.BorderLayout.CENTER);

        getContentPane().add(mainPanel, java.awt.BorderLayout.CENTER);

        pack();
    }
    
    /** Closes the dialog */    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        new WaitWindow(null, "Closing monitors: this is the message").setVisible(true);
    }
    
    
}
