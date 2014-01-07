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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class RoundedRectanglePanel extends JPanel{

    private Surface roundedRectangleSurface;
    private int arcAngle = 30;    
    
    public RoundedRectanglePanel(int arcAngle) {
        super();
        this.setBackground(Color.GRAY);
        this.arcAngle = arcAngle;
        this.setOpaque(false);
    }

    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        roundedRectangleSurface = new SplineDecorator(new RoundedRectangleSurface(width, height, 
                                                                                  this.arcAngle, 
                                                                                  this.getBackground()));
    }

    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        roundedRectangleSurface.draw(g2d);
        super.paintComponent(g);
    }    
    
    public static void main(String args[]){
        JFrame frame = new JFrame("RoundedRectanglePanel test");
        
        RoundedRectanglePanel roundedRectanglePanel = new RoundedRectanglePanel(30);
        roundedRectanglePanel.setLayout(new java.awt.BorderLayout());
        
        RoundedRectanglePanel roundedRectanglePanelNorth = new RoundedRectanglePanel(30);
        roundedRectanglePanelNorth.setBackground(Color.GREEN);
        roundedRectanglePanel.add(roundedRectanglePanelNorth, BorderLayout.NORTH);
        roundedRectanglePanelNorth.setPreferredSize(new Dimension(800,100));
        roundedRectanglePanelNorth.add(new javax.swing.JLabel("NORTH"));
        
        RoundedRectanglePanel roundedRectanglePanelSouth = new RoundedRectanglePanel(30);
        roundedRectanglePanelSouth.setBackground(Color.YELLOW);
        roundedRectanglePanel.add(roundedRectanglePanelSouth, BorderLayout.SOUTH);
        roundedRectanglePanelSouth.setPreferredSize(new java.awt.Dimension(800,100));
        roundedRectanglePanelSouth.add(new javax.swing.JLabel("SOUTH"));

        RoundedRectanglePanel roundedRectanglePanelCenter = new RoundedRectanglePanel(30);
        roundedRectanglePanelCenter.setBackground(Color.BLUE);
        roundedRectanglePanel.add(roundedRectanglePanelCenter, BorderLayout.CENTER);
        
        frame.getContentPane().add(roundedRectanglePanel, BorderLayout.CENTER);
        
        frame.setBounds(0, 0, 800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
    
    
    
}
