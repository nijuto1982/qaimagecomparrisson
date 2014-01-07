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

package org.jdiffchaser.imgprocessing;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;

public class IgnoredBgPattern {
    
   
    private static final int PIXELS_BETWEEN_LINES = 5; 
    private static Font smallFont  = new Font("Arial", Font.PLAIN, 10);
    private static final int SHADOW_OFFSET = 1;
    
    private static GraphicsEnvironment locEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
    private static GraphicsConfiguration gConf = locEnv.getDefaultScreenDevice().getDefaultConfiguration();    
    
    /** Creates a new instance of ComparisonBgPattern */
    private IgnoredBgPattern() {
    }
       
   
    private static void drawSquares(Graphics g, 
                                    Color bgColor, 
                                    Color fgColor,
                                    int width,
                                    int height){
        g.setColor(bgColor);
        g.fillRect(0, 0, width, height);
        g.setColor(fgColor);
        int i = PIXELS_BETWEEN_LINES;
        while(i<width){
            g.drawLine(i, 0, i, height);
            i += PIXELS_BETWEEN_LINES;
        }
        i = PIXELS_BETWEEN_LINES;
        while(i<height){
            g.drawLine(0, i, width, i);
            i += PIXELS_BETWEEN_LINES;
        }        
    }

    private static void drawWatermark(  Graphics g,
                                        Color bgColor, 
                                        Color fgColor,
                                        Color shadowColor,
                                        int width,
                                        int height){
        g.setColor(bgColor);
        g.fillRect(0, 0, width, height);    
        drawWmStrings(g, shadowColor, true, width, height);
        drawWmStrings(g, fgColor, false, width, height);
    }
    
    private static void drawWmStrings(Graphics g, Color color, boolean isShadow, int width, int height){
        g.setColor(color);
        g.setFont(smallFont);
        String txt = " / let's do some gui tests: regression hunting started";
        int textWidth  = g.getFontMetrics(smallFont).stringWidth(txt);
        int textHeight = g.getFontMetrics(smallFont).getHeight();
        for(int line=(isShadow?SHADOW_OFFSET:0); line<=height; line+=textHeight){
            for(int col=(isShadow?SHADOW_OFFSET:0); col<=width; col+=textWidth){
                g.drawString(txt, col, line);
            }
        }        
    }
    
    /**
     * @see IgnoredBgPattern constants for patternTypes
     */
    public static BufferedImage createImage(int width, int height){   
        return createImage(width, height, Color.LIGHT_GRAY, Color.DARK_GRAY, Color.WHITE);
    }

    public static BufferedImage createImage(int width, int height,
                                            Color lightColor, Color darkColor, Color bevelColor){

        BufferedImage patternImage = gConf.createCompatibleImage(width,
                                                                 height,
                                                                 Transparency.TRANSLUCENT);

        drawWatermark(patternImage.getGraphics(), lightColor, darkColor, bevelColor, width, height);
        return patternImage;
    }


    public static BufferedImage getDisplayedImageWithBg(BufferedImage transparentImgToDisplay){
        BufferedImage displayedImg = IgnoredBgPattern.createImage(  transparentImgToDisplay.getWidth(), 
                                                                    transparentImgToDisplay.getHeight());
        Graphics g = displayedImg.getGraphics();
        g.drawImage(transparentImgToDisplay, 0, 0, transparentImgToDisplay.getWidth(), 
                                                   transparentImgToDisplay.getHeight(), null);
        return displayedImg;
    }    
    
    public static void main(String args[]){
        JFrame frame = new JFrame("pattern test"){
            public void paint(Graphics g){
                g.drawImage(IgnoredBgPattern.createImage(800,600), 
                                                         0, 0,
                                                         800, 600, this.getContentPane());
            }
        };
        frame.setSize(800,600);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
}
