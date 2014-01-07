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

import javax.swing.SwingUtilities;
import org.jdiffchaser.utils.ImageUtilities;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Robot;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;


public class Screenshot {
    
    private static final Logger LOGGER = Logger.getLogger(Screenshot.class.getName());
    
    private static GraphicsEnvironment locEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
    private static GraphicsConfiguration gConf = locEnv.getDefaultScreenDevice().getDefaultConfiguration();              
    
    private Screenshot() {
    }
    
    public static BufferedImage loadImageFromFile(File file) throws Exception{
        InputStream stream = new FileInputStream(file);
        LOGGER.info("stream is " + stream);
        BufferedImage image = ImageIO.read(stream);
        LOGGER.info("image is " + image);
        try{
            MediaTracker tracker = new MediaTracker(new java.awt.Panel());
            tracker.addImage(image,0);
            tracker.waitForAll();
            tracker.removeImage(image,0);
        }catch(InterruptedException ie){
            LOGGER.log(Level.SEVERE, "Can not wait for images", ie);
        }
        stream.close();
        return image;
    } 
    
    public static BufferedImage createScreenCapture(Component component, boolean fullScreen) throws ScreenshotException{
        if(fullScreen){
            return createScreenCapture(0, 0, (int) gConf.getBounds().getWidth(), (int)gConf.getBounds().getHeight());
        }else{
            return createScreenCapture(component.getLocationOnScreen().x, component.getLocationOnScreen().y,
                                component.getWidth(), component.getHeight());
        }
                                         
    }
    
    public static BufferedImage createScreenCapture(final int x, final int y, 
                                                    final int w, final int h) throws ScreenshotException{
        final BufferedImage[] imgBufArray = new BufferedImage[1];
        final ScreenshotException[] thrownException = new ScreenshotException[1];
        synchronized(imgBufArray){
            SwingUtilities.invokeLater(new Runnable(){                
                public void run(){
                    try{
                        Robot robot = new Robot();
                        BufferedImage sc = robot.createScreenCapture(gConf.getBounds());
                        ImageUtilities.waitForImage(sc);
                        Thread.sleep(4000);          

                        LOGGER.info("image read is " + (sc==null?"null": "not null"));
                        BufferedImage imgBuf = gConf.createCompatibleImage(w, h, Transparency.TRANSLUCENT);

                        Graphics g = imgBuf.getGraphics(); //by copying, we add the alpha channel
                        Image subImage = sc.getSubimage(x, y, w, h);
                        g.drawImage(subImage, 0, 0, w, h, null);
                        imgBufArray[0] = imgBuf;
                    }catch(Exception e){
                        thrownException[0] = new ScreenshotException("Unable to take native screenshot ", e);
                    }finally{
                        synchronized(imgBufArray){
                            imgBufArray.notify();
                        }
                    }
                }

            });
            try{
                imgBufArray.wait();
            }catch(InterruptedException ie){
                ie.printStackTrace();
            }
        } 
        if(imgBufArray[0]!=null){
            return imgBufArray[0];
        } else {
            throw thrownException[0];  
        }
        
    }    

    public static BufferedImage createComponentScreenshot(Component component){
        
        final BufferedImage imgBuf = gConf.createCompatibleImage(component.getWidth(), 
                                                                 component.getHeight(),
                                                                 Transparency.TRANSLUCENT);
        Graphics gr = imgBuf.createGraphics();
        gr.setColor(Color.BLACK);
        gr.fillRect(0, 0, component.getWidth(), component.getHeight());
        component.paintAll(gr);
        
        return imgBuf;
    }

    public static void takeRobotScreenshot(String fileName) throws ScreenshotException{
        try{
            Robot robot = new Robot();
            System.out.println("will store screen capture in " + fileName);
            BufferedImage image = robot.createScreenCapture(gConf.getBounds());
            ImageUtilities.waitForImage(image);
            ImageUtilities.storeImage(image, fileName);
            System.out.println("Image stored");
        }catch(Throwable t){
            throw new ScreenshotException("Unable to take a robot screenshot", t);
        }
    }
    
    public static void main(String[] args){
        try{
            if(args.length>0){
                takeRobotScreenshot(args[0]);
            }else{
                System.out.println("Usage: Screenshot [filename]");
            }
        }catch(Throwable t){
            t.printStackTrace();
        }
    }
    
}
