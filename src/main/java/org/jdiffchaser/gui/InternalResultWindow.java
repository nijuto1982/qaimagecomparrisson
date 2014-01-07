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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.jdiffchaser.imgprocessing.*;

public class InternalResultWindow extends InternalShadowedWindow{
    
    
    private ImageIcon smileIcon = new ImageIcon(InternalResultWindow.class.getResource("face-smile.png"));
    private ImageIcon sadIcon   = new ImageIcon(InternalResultWindow.class.getResource("face-sad.png"));
    
    private BufferedImage failedImg  = null;
    private BufferedImage successImg = null;
    
    private boolean success = false;
    
    private Font resultFont = new Font("Dialog",  Font.BOLD, 20);    
    
    private static GraphicsEnvironment locEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
    private static GraphicsConfiguration gConf = locEnv.getDefaultScreenDevice().getDefaultConfiguration();      
    
    public InternalResultWindow(Rectangle bounds, boolean success) { 
        super(bounds, success?Color.GREEN:Color.RED);
        int contentWidth  = (int)bounds.getWidth() - (2*SHADOW_MARGIN);
        int contentHeight = (int)bounds.getHeight() - (2*SHADOW_MARGIN);
        
        this.failedImg  = gConf.createCompatibleImage(contentWidth, contentHeight,
                                                      Transparency.TRANSLUCENT);
        this.successImg = gConf.createCompatibleImage(contentWidth, contentHeight,
                                                      Transparency.TRANSLUCENT);
        
        this.success = success;
    }

    public void draw(Graphics g){
        super.draw(g);
        int x = (int)this.bounds.getX() + SHADOW_MARGIN;
        int y = (int)this.bounds.getY() + SHADOW_MARGIN;        
        if(success){
            this.prepareImg("Matches !", successImg, smileIcon, Color.GREEN);
            g.drawImage(successImg, x, y, successImg.getWidth(), successImg.getHeight(), null);
        }else{
            this.prepareImg("Mismatch found !", failedImg, sadIcon, Color.RED);
            g.drawImage(failedImg, x, y, failedImg.getWidth(), failedImg.getHeight(), null);
        }

    }
    
    private void prepareImg(String msg, BufferedImage dest, ImageIcon icon, Color color){
        Graphics g = dest.getGraphics();
        int x = 0;
        int y = 0;
        g.setColor(color);
        g.fillRect(x, y, (int)this.bounds.getWidth(), (int)this.bounds.getHeight());
        g.drawImage(icon.getImage(), x, y, icon.getIconWidth(), icon.getIconHeight(), null);
        g.setColor(Color.BLACK);
        g.setFont(resultFont);
        int stringWidth = g.getFontMetrics(resultFont).stringWidth(msg);
        g.drawString(msg,
                     (dest.getWidth()-icon.getIconWidth()) - stringWidth/2,
                     dest.getHeight()/2);
    }
    
    
    public static void main(String[] args){
        JFrame frame = new JFrame("internal window test");
        
        final InternalResultWindow success = new InternalResultWindow(new Rectangle(50, 50, 320, 120), true);
        final InternalResultWindow failure = new InternalResultWindow(new Rectangle(200, 140, 320, 120), false);
        final InternalShadowedWindow grayOne = new InternalShadowedWindow(new Rectangle(400, 400, 100, 50), Color.GRAY);

        JPanel panel = new JPanel(){
            public void paint(Graphics g){
                super.paint(g);
                success.draw(g);
                failure.draw(g);
                grayOne.draw(g);
            }
        };
        
        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.setBounds(0, 0, 800, 600);
        frame.show();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Graphics g = panel.getGraphics();
    }    
    
}
