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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class RoundedRectangleSurface extends BufferedImage implements Surface{

    private Color background;
    private int arcAngle;
    
    public static class SurfaceDecorator implements Surface{
        
        protected RoundedRectangleSurface roundedRectangleSurface;
        
        public SurfaceDecorator(RoundedRectangleSurface roundedRectangleSurface){
            this.roundedRectangleSurface = roundedRectangleSurface;
        }
        
        public void draw(Graphics2D g2d){
            this.roundedRectangleSurface.draw(g2d);
        }
        
    }
    
    public RoundedRectangleSurface(int width, int height, int arcAngle, Color background){
        super(Math.max(width, 4), Math.max(height, 4), BufferedImage.TYPE_INT_ARGB);
        this.arcAngle = arcAngle;
        this.background = background;
        Graphics2D g2d = (Graphics2D) this.getGraphics();
        draw(g2d);
    }
    
    public int getWidth() {
        return Math.max(super.getWidth(), 4);
    }

    public int getHeight() {
        return Math.max(super.getHeight(), 4);    
    }
    
    public void draw(Graphics2D g2d){
        
        int x       = 1;
        int y       = 0;
        int width   = this.getWidth();
        int height  = this.getHeight();
        
        RoundRectangle2D roundRectangle2D = new RoundRectangle2D.Double( x, y, width, height, 
                                                                         this.arcAngle, this.arcAngle);
                
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);      
                
        //----------------------- the window
        //----------------------- the light bevel effect
        roundRectangle2D.setRoundRect(  x, 
                                        y, 
                                        width-2, 
                                        height-2,
                                        this.arcAngle, this.arcAngle);
        g2d.setColor(Color.WHITE);
        g2d.fill(roundRectangle2D);
        
        //----------------------- the window
        //----------------------- the dark bevel effect
        roundRectangle2D.setRoundRect(  x, 
                                        y+1, 
                                        width-2, 
                                        height-2,
                                        this.arcAngle, this.arcAngle);
        g2d.setColor(this.background.darker());
        g2d.fill(roundRectangle2D);        
        
        //----------------------- the window
        //----------------------- the window panel
        g2d.setColor(this.background);
        roundRectangle2D.setRoundRect(  x+1, 
                                        y+1, 
                                        width-3, 
                                        height-3,
                                        this.arcAngle, this.arcAngle);
        g2d.fill(roundRectangle2D);

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_OFF);        
        
    }

    public static void main(String args[]){
        JFrame frame = new JFrame("RoundedWindowImage test");
        JPanel panel = new JPanel(){

            private RoundedRectangleSurface roundedWindowImage = new RoundedRectangleSurface(320, 100, 30, Color.RED);
            
            public void paintComponent(Graphics g){
                Graphics2D g2d = (Graphics2D) g;
                g2d.drawImage(roundedWindowImage, 0, 0, null);
            }
        };
        frame.getContentPane().add(panel, BorderLayout.CENTER);
        
        frame.setBounds(0, 0, 800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
    
}
