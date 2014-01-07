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
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.jdiffchaser.imgprocessing.*;


public class InternalWaitWindow extends InternalShadowedWindow{
    
    private static final Logger LOGGER = Logger.getLogger(InternalWaitWindow.class.getName());
    
    private int x = 0;
    private int y = 0;
    
    private static WaitSatelliteAnimation animation = new WaitSatelliteAnimation(70);
    private static Font TEXTFONT = new Font("Arial", Font.BOLD, 16);
    private String waitMessage = "";
    
    public static String title = "searching for diffs...";
    
    private static final InternalWaitWindow WAIT_WINDOW = 
                                                    new InternalWaitWindow(Color.LIGHT_GRAY);

    private static Thread runningThread = null;
    
    private static GraphicsEnvironment locEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
    private static GraphicsConfiguration gConf = locEnv.getDefaultScreenDevice().getDefaultConfiguration();          
        
    private InternalWaitWindow(Rectangle bounds, Color color) {
        super(bounds, color);
        x = bounds.x+SHADOW_MARGIN;
    }
    
    private InternalWaitWindow(Color color){
        this(new Rectangle(0,0,0,0), color);    
    }    
    
    public void setBounds(Rectangle bounds){
        super.setBounds(bounds);
        x = bounds.x+SHADOW_MARGIN;
    }
    
    public static Shape getMaskBounds(){
        return WAIT_WINDOW.getShape();
    }
    
    public void setMessage(String waitMessage){
        this.waitMessage = waitMessage;
    }
    
    public void drawContent(Graphics g){
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);         
        
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(bounds.x+SHADOW_MARGIN, bounds.y+SHADOW_MARGIN, 
                   bounds.width-(2*SHADOW_MARGIN), bounds.height-(int)(SHADOW_MARGIN*1.5));
        g.setColor(Color.BLACK);
        
        animation.draw(g, bounds.x + SHADOW_MARGIN,  bounds.y + SHADOW_MARGIN);
        
        while((4*SHADOW_MARGIN + animation.getWidth()) 
               + 
                g2d.getFontMetrics(TEXTFONT).stringWidth(waitMessage) > bounds.width-(2*SHADOW_MARGIN)){
            TEXTFONT = TEXTFONT.deriveFont((float)(TEXTFONT.getSize()-1));
            LOGGER.finer("decreasing font size is " + TEXTFONT.getSize());
        }
        g2d.setFont(TEXTFONT);        
                
        g2d.drawString("Please wait:", bounds.x + 4*SHADOW_MARGIN + animation.getWidth(), bounds.y + bounds.height/2);
        int fontHeight = g2d.getFontMetrics(TEXTFONT).getHeight();
        g2d.drawString(waitMessage, bounds.x + 4*SHADOW_MARGIN + animation.getWidth(), 
                                    bounds.y + bounds.height/2 + (int)(fontHeight*1.25));
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_OFF);         
        
    }
            
    public static synchronized void start(final BufferedImage bgImage, 
                                          final Graphics g,
                                          int width, int height, final Component compToRepaint, String waitMessage, boolean withShadow){
        float ratio = (float) width / (float)height;
        int scaledWidth  = width - 20 ;
        int scaledHeight = (int) Math.ceil((float)scaledWidth / ratio);
        while(scaledWidth > bgImage.getWidth()- 20 || scaledHeight > bgImage.getHeight()- 20){
            scaledWidth = (int) ((float) scaledWidth / 1.1f);
            scaledHeight = (int) Math.ceil((float)scaledWidth / ratio);
        }
        width  = scaledWidth;
        height = scaledHeight;
        float animationWidth = 70;
        while(animationWidth > width - 40  ||animationWidth > height - 40){
            animationWidth /= 1.1f;
        }
        animation = new WaitSatelliteAnimation((int)animationWidth);

        final Rectangle locBounds = InternalShadowedWindow.computeCenterDialogLocation( bgImage.getWidth(),
                                                                                        bgImage.getHeight(),
                                                                                        scaledWidth,
                                                                                        scaledHeight);
        WAIT_WINDOW.setMessage(waitMessage);
        WAIT_WINDOW.setBounds(locBounds);
        WAIT_WINDOW.setWithShadow(withShadow);
        WAIT_WINDOW.draw(g);
        runningThread = new Thread(){
            public void run(){
                try{
                    while(runningThread == Thread.currentThread()){
                        WAIT_WINDOW.drawContent(g);
                        Thread.sleep(150);
                        compToRepaint.repaint(locBounds.x - SHADOW_MARGIN, locBounds.y, 
                                              locBounds.width+(2*SHADOW_MARGIN), locBounds.height+(3*SHADOW_MARGIN));                                
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        };
        runningThread.setPriority(Thread.MAX_PRIORITY);
        runningThread.start();
    }
    
    public static synchronized void stop(){
        runningThread=null;
    }
    
    private static BufferedImage createBufferedImage(Image image){
        BufferedImage buf = gConf.createCompatibleImage(image.getWidth(null), 
                                                        image.getHeight(null),
                                                        Transparency.TRANSLUCENT);
        Graphics g = buf.getGraphics();
        g.drawImage(image, 0, 0, image.getWidth(null), image.getHeight(null), null);
        return buf;
    }
    
    public static void main(String[] args){
        try{
            int sqSize = 1000;
            JFrame frame = new JFrame("satellite anim test");
            BufferedImage buffer = gConf.createCompatibleImage(sqSize, sqSize, Transparency.TRANSLUCENT);
            Graphics g = buffer.getGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, sqSize, sqSize);
            BufferedPanel panel = new BufferedPanel(buffer);
            frame.getContentPane().add(panel, BorderLayout.CENTER);
            frame.setBounds(0, 0, sqSize, sqSize);
            frame.setVisible(true);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            Thread.sleep(1000);
            InternalWaitWindow.start(buffer, g, 275, 120, panel, "searching for diffs...", true);
        }catch(Exception e){
            e.printStackTrace();
        }
    }      
    
    
}
