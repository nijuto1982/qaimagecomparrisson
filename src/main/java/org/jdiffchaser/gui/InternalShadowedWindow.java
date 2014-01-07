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
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class InternalShadowedWindow {
    
    private Color            color;
    protected Rectangle      bounds;
    protected static final int   SHADOW_MARGIN      = 10;
    private static final int   SHADOW_ARCANGLE    = 40;
    private static final int   WINDOW_ARCANGLE    = 30;
    
    private RoundRectangle2D.Double roundRectangle2D;
    private RoundRectangle2D.Double shape;
    
    private boolean withShadow = true;

    public boolean isWithShadow() {
        return withShadow;
    }

    public void setWithShadow(boolean withShadow) {
        this.withShadow = withShadow;
    }
    
    /** Creates a new instance of InternalShadowedWindow */
    public InternalShadowedWindow(Rectangle bounds, Color color) {
        this.bounds = bounds;
        this.color = color;
    }
    
    public void setBounds(Rectangle bounds){
        this.bounds = bounds;
    }
    
    public int getWidth(){
        return this.bounds.width;
    }
    
    public int getHeight(){
        return this.bounds.height;
    }
    
    public Shape getShape(){
        return this.shape;
    }

    /**
     * @returns the whole occupied area, included shadow
     */
    public Rectangle getWholeArea(){
        return new Rectangle(this.bounds.x-SHADOW_MARGIN,
                             this.bounds.y,
                             this.bounds.width  + (2*SHADOW_MARGIN),
                             this.bounds.height + (3*SHADOW_MARGIN));
    }
    
    private void drawShadow(RoundRectangle2D roundRectangle2D, Graphics2D g2d){
        
        float alpha = 0.0f;
        for(int i=0; i<3*SHADOW_MARGIN; i++){    
            double arcWidth = roundRectangle2D.getArcWidth();
            double arcHeight = roundRectangle2D.getArcHeight();
            roundRectangle2D.setRoundRect(roundRectangle2D.getX()+1,
                                          roundRectangle2D.getY()+1,
                                          roundRectangle2D.getWidth()-2,
                                          roundRectangle2D.getHeight()-2,
                                          arcWidth>2?arcWidth-2:arcWidth,
                                          arcHeight>2?arcHeight-2:arcHeight);
            g2d.setColor(new Color(0.0f,0.0f,0.0f,alpha));
            g2d.draw(roundRectangle2D); 
            if(alpha<0.98f){
                alpha+=0.02f;
            }else{
                alpha=1.0f;
            }
        }
        g2d.fill(roundRectangle2D);
    }
    
    public void draw(Graphics g){
        Graphics2D g2d = (Graphics2D) g;

        int x       = this.bounds.x - SHADOW_MARGIN;
        int y       = this.bounds.y + SHADOW_MARGIN;
        int width   = this.bounds.width  + (2*SHADOW_MARGIN);
        int height  = this.bounds.height + (2*SHADOW_MARGIN);
        int arcw    = SHADOW_ARCANGLE;
        int arch    = SHADOW_ARCANGLE;
        
        roundRectangle2D = new RoundRectangle2D.Double( x, y, width, height, arcw, arch);
        shape = new RoundRectangle2D.Double( this.bounds.x-1, 
                                             this.bounds.y+1, 
                                             this.bounds.width+2, 
                                             this.bounds.height+2,
                                             WINDOW_ARCANGLE, WINDOW_ARCANGLE);
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);      
        
        if(withShadow){
            drawShadow(roundRectangle2D, g2d);
        }

        //----------------------- the window
        //----------------------- the light bevel effect
        roundRectangle2D.setRoundRect(  this.bounds.x, 
                                        this.bounds.y, 
                                        this.bounds.width, 
                                        this.bounds.height,
                                        WINDOW_ARCANGLE, WINDOW_ARCANGLE);
        g2d.setColor(Color.WHITE);
        g2d.fill(roundRectangle2D);
        
        //----------------------- the window
        //----------------------- the dark bevel effect
        roundRectangle2D.setRoundRect(  this.bounds.x, 
                                        this.bounds.y+1, 
                                        this.bounds.width, 
                                        this.bounds.height,
                                        WINDOW_ARCANGLE, WINDOW_ARCANGLE);
        g2d.setColor(color.darker());
        g2d.fill(roundRectangle2D);        
        
        //----------------------- the window
        //----------------------- the window panel
        g2d.setColor(color);
        roundRectangle2D.setRoundRect(  this.bounds.x+1, 
                                        this.bounds.y+1, 
                                        this.bounds.width-1, 
                                        this.bounds.height-1,
                                        WINDOW_ARCANGLE, WINDOW_ARCANGLE);
        g2d.fill(roundRectangle2D);

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_OFF);        
        
    }
    
    public static Rectangle computeCenterDialogLocation(int hostingPanelWidth, int hostingPanelHeight, int w, int h){
        return new Rectangle((int)((hostingPanelWidth-w)/2.0),
                             (int)((hostingPanelHeight-h)/2.0),
                             w, h);
    }    
    
    public static void main(String[] args){
        JFrame frame = new JFrame("internal window test");
        JPanel panel = new JPanel();
        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.setBounds(0, 0, 800, 600);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Graphics g = panel.getGraphics();
        new InternalShadowedWindow(new Rectangle(50, 50, 320, 100), Color.RED).draw(g);
    }
    
}
