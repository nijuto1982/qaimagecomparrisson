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

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import org.jdiffchaser.imgprocessing.*;

public class WaitSatelliteAnimation {
    
    private int satelliteAngle      = 0;
    private int lilSatelliteAngle   = 0;
    private double trajectoryCoeff  = 0.8;
    private int bigCircleDiam = 0;
    private int firstSatelliteDiam = 0;
    private int secondTrajectoryDiam = 0;
    private int secondSatelliteDiam = 0;
    private int margin = 0;
    private int width  = 0;

    private static final Color  FG_COLOR        = Color.WHITE;
    private static final Color  SHADOW_COLOR    = Color.DARK_GRAY;
    private static final Stroke PLAIN_STROKE    = new BasicStroke(2.0f);
    private static final float  BIG_STROKE_SIZE = 4.0f;    
    private static final Stroke BIG_STROKE      = new BasicStroke(BIG_STROKE_SIZE);
    
    private static GraphicsEnvironment locEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
    private static GraphicsConfiguration gConf = locEnv.getDefaultScreenDevice().getDefaultConfiguration();                  
    
    public WaitSatelliteAnimation(int width) {
        bigCircleDiam           = (int)(width*trajectoryCoeff);
        firstSatelliteDiam      = (int) bigCircleDiam/3;
        secondTrajectoryDiam    = (int) (firstSatelliteDiam * 1.8);
        secondSatelliteDiam     = (int) secondTrajectoryDiam/5;
        margin                  = (width-bigCircleDiam)/2;      
        this.width              = width;
    }

    public int getWidth(){
        return this.width;
    }
    
    public void draw(Graphics g, int x, int y){
        Graphics2D g2d = (Graphics2D) g;
        x += (BIG_STROKE_SIZE + 1 + margin);
        y += (BIG_STROKE_SIZE + 1 + margin);  
        
        satelliteAngle+=2;
        lilSatelliteAngle+=10;
        
        AffineTransform bigTrajectoryMatrix = AffineTransform.getRotateInstance(Math.toRadians(satelliteAngle), 
                                                                                x + margin + bigCircleDiam/2,
                                                                                y + margin + bigCircleDiam/2);
        g2d.setTransform(bigTrajectoryMatrix);
                                           
        g2d.setColor(SHADOW_COLOR);
        g2d.setStroke(BIG_STROKE);
        drawBigSatellite(g2d, x, y, width);

        g2d.setColor(FG_COLOR);
        g2d.setStroke(PLAIN_STROKE);
        drawBigSatellite(g2d, x, y, width);
        
        AffineTransform lilTrajectoryMatrix = AffineTransform.getRotateInstance( Math.toRadians(lilSatelliteAngle), 
                                                                                 x + margin,
                                                                                 y + margin + bigCircleDiam/2);
        
        bigTrajectoryMatrix.concatenate(lilTrajectoryMatrix);
        
        g2d.setTransform(bigTrajectoryMatrix);
        
        g2d.setColor(SHADOW_COLOR);
        g2d.setStroke(BIG_STROKE);
        drawLilSatellite(g2d, x, y, width);

        g2d.setColor(FG_COLOR);
        g2d.setStroke(PLAIN_STROKE);
        drawLilSatellite(g2d, x, y, width);
        
        g2d.setTransform(new AffineTransform());
        
    }
    
    private void drawBigSatellite(Graphics2D g2d, int x, int y, int width){
        
        g2d.drawOval(x + margin, y + margin, bigCircleDiam, bigCircleDiam);
        g2d.fillOval(x + margin - firstSatelliteDiam/2, 
                     y + margin + bigCircleDiam/2 - firstSatelliteDiam/2, 
                     firstSatelliteDiam,
                     firstSatelliteDiam);

        g2d.drawOval(x + margin - secondTrajectoryDiam/2, 
                     y + margin + bigCircleDiam/2 - secondTrajectoryDiam/2, 
                     secondTrajectoryDiam, 
                     secondTrajectoryDiam);
        
    }
    
    private void drawLilSatellite(Graphics2D g2d, int x, int y, int width){
        
        g2d.fillOval(x + margin - secondTrajectoryDiam/2 - secondSatelliteDiam/2, 
                     y + margin + bigCircleDiam/2 - secondSatelliteDiam/2, 
                     secondSatelliteDiam, 
                     secondSatelliteDiam);
    }
    
    
    public static void main(String[] args){
        try{
            int sqSize = 300;
            JFrame frame = new JFrame("satellite anim test");
            BufferedImage buffer = gConf.createCompatibleImage(sqSize, 
                                                               sqSize,
                                                               Transparency.TRANSLUCENT);
            Graphics g = buffer.getGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, sqSize, sqSize);
            BufferedPanel panel = new BufferedPanel(buffer);
            frame.getContentPane().add(panel, BorderLayout.CENTER);
            frame.setBounds(0, 0, sqSize, sqSize);
            frame.setVisible(true);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            Thread.sleep(1000);
            InternalWaitWindow.start(buffer, g, 275, 120, panel, "searching for diffs", true);
        }catch(Exception e){
            e.printStackTrace();
        }
    }      

    
    
}
