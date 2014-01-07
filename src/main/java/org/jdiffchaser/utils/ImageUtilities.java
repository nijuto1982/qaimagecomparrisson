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

package org.jdiffchaser.utils;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

public class ImageUtilities {

    private static final Logger LOGGER = Logger.getLogger(ImageUtilities.class.getName());
    
    private static GraphicsEnvironment locEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
    private static GraphicsConfiguration gConf = locEnv.getDefaultScreenDevice().getDefaultConfiguration();     
    
    public static final String IMG_EXTENSION = "png";
    
    public static void waitForImage(Image image){
        try{
            MediaTracker tracker = new MediaTracker(new java.awt.Panel());
            tracker.addImage(image,0);
            tracker.waitForAll();
            tracker.removeImage(image,0);
        }catch(InterruptedException ie){
            LOGGER.log(Level.SEVERE, "Can not wait for images", ie);
        }
    }    
    
    public static void copy(RenderedImage source, BufferedImage target) {
        Graphics2D g2 = target.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        double scalex = (double) target.getWidth()/ source.getWidth();
        double scaley = (double) target.getHeight()/ source.getHeight();
        AffineTransform xform = AffineTransform.getScaleInstance(scalex, scaley);
        g2.drawRenderedImage(source, xform);
        g2.dispose();
        ImageUtilities.waitForImage(target);
    }

    public static BufferedImage getScaledInstance(RenderedImage image, int width, int height, boolean keepAspectRation) {
        if( keepAspectRation ){
            double ratio = ((double)image.getWidth())/((double)image.getHeight());
            double targetRatio = ((double)width)/((double)height);
            if( targetRatio>ratio ){
                width = (int)(ratio*height);
            }else if( targetRatio<ratio ){
                height = (int)(width/ratio);
            }
        }
        GraphicsConfiguration gc = getDefaultConfiguration();
        int transparency = image.getColorModel().getTransparency();
        BufferedImage newImage = gc.createCompatibleImage(width, height, transparency);
        copy(image, newImage);
        return newImage;
    }
    
    public static Image getScaled(Image originalImage, 
                                    int scaledWidth, int scaledHeight){
     
        boolean ourScalingMethod = false;
        boolean preserveAlpha = true;
        if(ourScalingMethod){   
            int imageType = preserveAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
            GraphicsConfiguration gc = getDefaultConfiguration();
            BufferedImage scaledBI = gc.createCompatibleImage(scaledWidth, scaledHeight, imageType);
            Graphics2D g = scaledBI.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                               RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            if (preserveAlpha) {
               g.setComposite(AlphaComposite.Src);
            }
            g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null); 
            g.dispose();
            return scaledBI;
        }else{
            return originalImage.getScaledInstance(scaledWidth,
                                                   scaledHeight,
                                                   Image.SCALE_SMOOTH);
        }
    } 
       
    public static GraphicsConfiguration getDefaultConfiguration() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        return gd.getDefaultConfiguration();
    }
    
    public static BufferedImage readImageFromBytes(byte[] buffer) throws IOException, InterruptedException{
        BufferedImage image = null;
        ByteArrayInputStream in = null;
        try{
            in = new ByteArrayInputStream(buffer);
            image = readImage(in);
        }finally{
            in.close();
        }
        return image;
    }
    
    public static BufferedImage readImage(File file) throws IOException, InterruptedException{
        BufferedImage toReturn = null;
        InputStream stream = null;
        try{
            stream = new FileInputStream(file);
            toReturn = readImage(stream);
        }finally{
            stream.close();
        }
        return toReturn;
    }
    

    public static BufferedImage readImage(InputStream stream) throws IOException, InterruptedException{
        BufferedImage image = ImageIO.read(stream);
        waitForImage(image);
        return image;
    }    
        
    public static void storeImage(BufferedImage image, String filename) throws IOException{
        File file = new File(filename);
        if(file.getParentFile()!=null){
            file.getParentFile().mkdirs();
        }
        
        ImageIO.write((BufferedImage)image, IMG_EXTENSION, file);
        LOGGER.fine("Image: " + file.getPath() + " stored");
    }       
    
    public static BufferedImage createEmptyTransparentImage(int width, int height){
        BufferedImage transparentImage = gConf.createCompatibleImage(width, 
                                                                     height,
                                                                     Transparency.TRANSLUCENT);
            
        return transparentImage;
    }    

}
