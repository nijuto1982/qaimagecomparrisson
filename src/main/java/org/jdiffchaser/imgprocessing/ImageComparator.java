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

import java.awt.AlphaComposite;
import java.awt.RenderingHints;
import java.util.logging.Level;
import javax.swing.JFileChooser;
import org.jdiffchaser.gui.InternalResultWindow;
import org.jdiffchaser.gui.InternalShadowedWindow;
import org.jdiffchaser.gui.InternalWaitWindow;
import org.jdiffchaser.utils.ImageUtilities;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.JFrame;
import org.jdiffchaser.gui.ExternalWaitWindow;

public class ImageComparator {
    
    private static final Logger LOGGER = Logger.getLogger(ImageComparator.class.getName());
    
    private double              animationRatio;
    private JFrame              animationFrame;
    private BufferedPanel       animationPanel;
    private BufferedImage       animationBuffer;
    private String              commentImg1;
    private String              commentImg2;
    private BufferedImage       image1;
    private BufferedImage       image2;
    private BufferedImage       lastResultImage;
    private boolean             lastComparisonMatched = false;
    
    private static final int    MASK_BUBBLE_DIAMETER    = 20;
    private static final Color  MASK_COLOR              = Color.GREEN;
    private static final Color  NO_COLOR                = new Color(0.0f, 0.0f, 0.0f, 0.0f);
    private static final int    GRADIENT_LENGTH         = 1000;
    private static final int    BANNER_MARGIN           = 100;
    
    private Font                labelsFont = new Font("Dialog", Font.BOLD, 10);
    
    private static final int    SLIDES_NBFRAMES   = 25;
    private static final int    SCANLINE_NBFRAMES = 25;
    
    private static final int    TEXT_SHADOW_OFFSET = 1;
    
    private static ImageComparator instance = null;
    
    private GraphicsEnvironment locEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
    private GraphicsConfiguration gConf = locEnv.getDefaultScreenDevice().getDefaultConfiguration();

    private InternalShadowedWindow commentsWindow;
    
    private static class ScanLine{
        private static final float STROKE_WIDTH = 2.0f;
        private int     y;
        private int     width;
        private float   alpha = 1.0f;
        
        public ScanLine(int y, int width){
            this.y      = y;
            this.width  = width;
        }
        
        public int getY(){
            return  this.y;
        }
        
        public boolean isTransparent(){
            return this.alpha==0;
        }
        
        public float decrementAlpha(float amount){
            alpha-=amount;
            if(alpha<0){
                alpha = 0;
            }
            return alpha;
        }
        
        public void draw(Graphics g){
            if(alpha>0){
                g.setColor(new Color(0.0f,1.0f,0.0f,alpha));
                ((Graphics2D)g).setStroke(new BasicStroke(STROKE_WIDTH));
                ((Graphics2D)g).drawLine(0, y, width, y);
            }
        }
        
    }
    
    private static class HighlightDiffsResult{

        private BufferedImage diffs;
        private boolean areSimilar;

        public boolean areSimilar() {
            return areSimilar;
        }

        public BufferedImage getDiffs() {
            return diffs;
        }
        
        public HighlightDiffsResult(BufferedImage diffs, boolean areSimilar) {
            this.diffs = diffs;
            this.areSimilar = areSimilar;
        }
        
    }
    
    public static synchronized ImageComparator getInstance(  
                                                double animationRatio,
                                                Image  image1, 
                                                String commentImg1,
                                                Image  image2,
                                                String commentImg2 ){
        if(instance==null){
            instance = new ImageComparator(animationRatio, image1, commentImg1, image2, commentImg2);
        }else{
            instance.setAnimationRatio(animationRatio);
            
            instance.setImage1(image1);
            instance.setComment1(commentImg1);
        
            instance.setImage2(image2);
            instance.setComment2(commentImg2);
        }
                
        return instance;
    }
    
    private ImageComparator(double animationRatio,
                            Image  image1, 
                            String commentImg1,
                            Image  image2,
                            String commentImg2) {
        
        setAnimationRatio(animationRatio);
        
        setImage1(image1);
        setComment1(commentImg1);
        
        setImage2(image2);
        setComment2(commentImg2);
    }
    
    public void setComment1(String commentImg1){
        this.commentImg1 = commentImg1;
    }
    
    public void setComment2(String commentImg2){
        this.commentImg2 = commentImg2;
    }

    public void setAnimationRatio(double ratio){
        this.animationRatio = ratio;
    }
    
    public double getAnimationRatio(){
        return this.animationRatio;
    }
    
    public void setImage1(Image image1){
        this.image1 = gConf.createCompatibleImage(image1.getWidth(null), 
                                                  image1.getHeight(null),
                                                  Transparency.TRANSLUCENT);
        
        this.image1.getGraphics().drawImage(image1, 0, 0, 
                                            this.image1.getWidth(), this.image1.getHeight(), null);        
    }
    
    public void setImage2(Image image2){
        this.image2 = gConf.createCompatibleImage(image2.getWidth(null), 
                                                  image2.getHeight(null),
                                                  Transparency.TRANSLUCENT);
        this.image2.getGraphics().drawImage(image2, 0, 0, 
                                            this.image2.getWidth(), this.image2.getHeight(), null);        
    }
    
    public boolean compareImages(boolean withAnimation){
        if(withAnimation){
            playAnimation();
            drawComparisonResultDialog();
        }else{
            ExternalWaitWindow waitWindow = null;
            try{
                waitWindow = new ExternalWaitWindow();
                waitWindow.setUseShadow(false);
                waitWindow.start("Comparing images");
            }catch(Throwable t){
                LOGGER.log(Level.WARNING, "Unable to start ExternalWaitWindow", t);
            }
            BufferedImage strokeImg = highlightDifferences();
            createDifferencesImg(strokeImg);      
            try{
                waitWindow.stop();
            }catch(Throwable t){
                LOGGER.log(Level.WARNING, "Unable to stop ExternalWaitWindow", t);
            }
        }
        return false;
    }
    
    
    private void playAnimation(){
        if(animationFrame==null){
            animationFrame = new JFrame("Image comparison processing...");
            animationFrame.getContentPane().setLayout(new BorderLayout());  
            
            animationBuffer = gConf.createCompatibleImage((int)Math.ceil(image1.getWidth(null) * getAnimationRatio()), 
                                                          (int)Math.ceil(image1.getHeight(null) * getAnimationRatio()),
                                                          Transparency.TRANSLUCENT);
                    
            animationPanel = new BufferedPanel(animationBuffer);            
            animationFrame.getContentPane().add(animationPanel, BorderLayout.CENTER);
            Dimension dim = new Dimension(animationBuffer.getWidth(), animationBuffer.getHeight());
            animationPanel.setPreferredSize(dim);
            animationFrame.pack();
        }
        
        if(   (int)Math.ceil(image1.getWidth(null) * getAnimationRatio())!=animationBuffer.getWidth()
           || (int)Math.ceil(image1.getHeight(null) * getAnimationRatio())!=animationBuffer.getHeight() ){
  
            animationBuffer = gConf.createCompatibleImage((int)Math.ceil(image1.getWidth(null) * getAnimationRatio()), 
                                                          (int)Math.ceil(image1.getHeight(null) * getAnimationRatio()),
                                                          Transparency.TRANSLUCENT);

            animationPanel.setBuffer(animationBuffer);
            Dimension dim = new Dimension(animationBuffer.getWidth(), animationBuffer.getHeight());
            animationPanel.setPreferredSize(dim);
            animationFrame.pack();
        }
        
        animationFrame.setVisible(true);
                
        animationFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        animationFrame.toFront(); 
        
        doWholeAnimation();
    }
    
    private void drawComments(String commentUpLft, String commentUpRgt, BufferedImage image){
        Graphics g = image.getGraphics();
        g.setFont(labelsFont);
        String rightStr = "<- " + commentUpRgt;
        String leftStr  = commentUpLft + " -";
        int rightStrWidth = g.getFontMetrics(labelsFont).stringWidth(rightStr);
        int leftStrWidth = g.getFontMetrics(labelsFont).stringWidth(leftStr);

        g.setColor(Color.WHITE);
        Graphics2D g2 = (Graphics2D) g;

        if(commentsWindow==null || commentsWindow.getWidth()!=image.getWidth()){
            commentsWindow = new InternalShadowedWindow(new Rectangle(-10, 30, 
                                                                      animationBuffer.getWidth()+20, 25), 
                                                        Color.WHITE);
        }
        commentsWindow.draw(g2);
            
        g.setColor(Color.BLACK);
        g.drawString(leftStr, 0 + TEXT_SHADOW_OFFSET, 40 + TEXT_SHADOW_OFFSET);
        g.drawString(rightStr, image.getWidth() -  rightStrWidth + TEXT_SHADOW_OFFSET, 50 + TEXT_SHADOW_OFFSET);

    }
    
    private void doSlidesAnimation(BufferedImage animationBuffer, Image image1, Image image2){
        Graphics g = animationBuffer.getGraphics();
        BufferedImage bgImage = IgnoredBgPattern.createImage(animationBuffer.getWidth(), animationBuffer.getHeight());
        int x1 = -animationBuffer.getWidth();
        int x2 = animationBuffer.getWidth();
        int speed = animationBuffer.getWidth() / SLIDES_NBFRAMES;
        boolean slidesTerminated = false;
        g.drawImage(bgImage, 0, 0, animationBuffer.getWidth(), animationBuffer.getHeight(), null);
        g.drawImage( image1,
                     0, 0, 
                     image1.getWidth(null), image1.getHeight(null),
                     null);
        drawComments(this.commentImg1, this.commentImg2, animationBuffer);
        animationPanel.repaint();
        while(!slidesTerminated){
            if(x2==0){
                slidesTerminated = true;
            }
            g.drawImage( image2,
                         x2, 0, 
                         image2.getWidth(null), image2.getHeight(null),
                         null);            
            //just to delimitate
            g.setColor(Color.BLACK);
            g.drawRect(x2, 0, image2.getWidth(null)-x2, image2.getHeight(null));

            drawComments(this.commentImg1, this.commentImg2, animationBuffer);

            animationPanel.repaint(x2, 0, image2.getWidth(null)-x2, image2.getHeight(null));
            
            if(x2-speed<0){
                x2=0;
            }else{
                x2-=speed;
            }                        
            
            try{
                Thread.sleep(50);
            }catch(InterruptedException ie){
                //ignore this ex.
            }
        }
        
    }
    
    private boolean allScanLinesInvisible(List scanLinesList, int imgHeight){
        boolean allInFrameScanLinesInvisible = true;
        for(int i=0; i<scanLinesList.size(); i++){
            ScanLine scanLine = (ScanLine) scanLinesList.get(i);
            if(!scanLine.isTransparent()){
                allInFrameScanLinesInvisible = false;
                break;
            }
        }
        return allInFrameScanLinesInvisible;
    }
    
    private void doScanAnimation(BufferedImage animationBuffer, Image resultImg){
        BufferedImage buffResImg = gConf.createCompatibleImage(resultImg.getWidth(null),
                                                               resultImg.getHeight(null),
                                                               Transparency.TRANSLUCENT);
        buffResImg.getGraphics().drawImage(resultImg, 0, 0, null);
        
        Graphics g = animationBuffer.getGraphics();
        int yLine = 1;
        int lineSpeed = 5;
        ArrayList scanLines = new ArrayList();
        boolean scanTerminated = false;
        while(!scanTerminated){
            
            yLine+=lineSpeed;

            if(yLine > resultImg.getHeight(null)){
                yLine=resultImg.getHeight(null);
            }
            
            Image subImage = buffResImg.getSubimage(0, 0, resultImg.getWidth(null), yLine);
            g.drawImage( subImage,
                         0, 0, 
                         resultImg.getWidth(null), yLine,
                         null);     
            
            if(yLine < resultImg.getHeight(null)){
                scanLines.add(new ScanLine(yLine, resultImg.getWidth(null)));
            }
            
            int yRepaint = 0;
            
            ScanLine scanLine = null;
            for(int i=0; i<scanLines.size(); i++){
                scanLine = (ScanLine) scanLines.get(i);
            float alpha = scanLine.decrementAlpha(0.05f);
                scanLine.draw(g);                
                if(alpha==0.0f){
                    yRepaint = scanLine.getY();
                }
            }
            
            int heightRepaint = scanLine.getY() - yRepaint;
            if(scanLine.getY()>resultImg.getHeight(null)){
                heightRepaint = resultImg.getHeight(null) - yRepaint;
            }
            
            animationPanel.repaint(0, yRepaint, resultImg.getWidth(null), heightRepaint);
            
            if(allScanLinesInvisible(scanLines, resultImg.getHeight(null))){
                scanTerminated = true;
            }            
        }
        
    }
    
    private BufferedImage highlightDifferences(){
        HighlightDiffsResult highlightDiffsResult = getHighlightedDiffsImage(this.image1, this.image2);
        BufferedImage strokeImg = highlightDiffsResult.getDiffs();
        this.lastComparisonMatched = highlightDiffsResult.areSimilar();
        return strokeImg;
    }

    private void createDifferencesImg(BufferedImage strokeImg){
        BufferedImage tmpImg = IgnoredBgPattern.getDisplayedImageWithBg(strokeImg);
        tmpImg.getGraphics().drawImage(this.lastResultImage,0,0, null);
        this.lastResultImage = tmpImg;
    }    
    
    private void doWholeAnimation(){
        
        int scaledImageWidth  = (int)Math.ceil(image1.getWidth(null)  * getAnimationRatio());
        int scaledImageHeight = (int)Math.ceil(image1.getHeight(null) * getAnimationRatio());
        
        doSlidesAnimation(animationBuffer,
                          ImageUtilities.getScaled(IgnoredBgPattern.getDisplayedImageWithBg(this.image1),
                                                   scaledImageWidth, scaledImageHeight),
                          ImageUtilities.getScaled(IgnoredBgPattern.getDisplayedImageWithBg(this.image2),
                                                   scaledImageWidth, scaledImageHeight));
                
        InternalWaitWindow.start(animationBuffer, 
                                 animationBuffer.getGraphics(),
                                 272, 120,
                                 animationPanel, "Searching for diffs...", true);

          
        BufferedImage strokeImg = highlightDifferences();
        
        InternalWaitWindow.stop();
        
        doScanAnimation(animationBuffer, 
                        ImageUtilities.getScaled(IgnoredBgPattern.getDisplayedImageWithBg(this.lastResultImage),
                                                 scaledImageWidth, scaledImageHeight));
        
        Graphics2D g2d = (Graphics2D) animationBuffer.getGraphics();
        g2d.setBackground(new Color(0.0f, 0.0f, 0.0f, 0.0f));
        g2d.clearRect(0,0, this.lastResultImage.getWidth(), this.lastResultImage.getHeight());

        g2d.drawImage( ImageUtilities.getScaled(strokeImg, scaledImageWidth, scaledImageHeight),
                       0,0,
                       null);
        g2d.drawImage( ImageUtilities.getScaled(this.lastResultImage, scaledImageWidth, scaledImageHeight),
                       0,0,
                       null);
        
        LOGGER.fine("Img process done!");
        animationPanel.repaint();

        createDifferencesImg(strokeImg);
    }
    
    /**
     * @param image1 first image to compare to...   
     * @param image2 ... second image
     * @return the transparent BufferedImage containing the pixels that are not common between both images
     */
    private HighlightDiffsResult getHighlightedDiffsImage(BufferedImage image1, 
                                                   BufferedImage image2){
        
        BufferedImage transBg = ImageUtilities.createEmptyTransparentImage(image1.getWidth(),
                                                                           image1.getHeight());        
        List diffsPixels = new ArrayList();
        boolean areSimilar = getDiffsPixels(diffsPixels, image1, image2);
        
        drawDiffsBubbles(   transBg.getGraphics(), 
                            diffsPixels, 
                            MASK_COLOR,
                            MASK_BUBBLE_DIAMETER);
        
        this.lastResultImage = applyMask(transBg, this.image1, this.image2);

        return new HighlightDiffsResult(createBubblesStrokeImage(this.lastResultImage, Color.BLUE, 10),
                                        areSimilar);        
    }
    
    public void drawComparisonResultDialog(){
        int resultWindowWidth  = 320;
        int resultWindowHeight = 120;
        int wMargin = 20;
        int hMargin = 60;
        float ratio = (float)resultWindowWidth / (float)resultWindowHeight;
        System.out.println("ratio is " + ratio);
        BufferedImage resultBuffer = ImageUtilities.createEmptyTransparentImage(resultWindowWidth + wMargin, resultWindowHeight + hMargin);
        Graphics2D g2d = (Graphics2D) resultBuffer.getGraphics();        
        new InternalResultWindow(InternalShadowedWindow.computeCenterDialogLocation(resultWindowWidth + wMargin, 
                                                                                    resultWindowHeight + hMargin,
                                                                                    320, 120), this.lastComparisonMatched).draw(g2d);
        
        Graphics2D g2d2 = (Graphics2D) animationBuffer.getGraphics();
        if(resultWindowWidth + wMargin > animationBuffer.getWidth()
            || resultWindowHeight + hMargin > animationBuffer.getHeight()){
            int scaledWidth  = animationBuffer.getWidth() - 20 ;
            System.out.println("initial ScaledWidth = " + scaledWidth);
            int scaledHeight = (int) Math.ceil((float)scaledWidth / ratio);
            while(scaledWidth > animationBuffer.getWidth() || scaledHeight > animationBuffer.getHeight()){
                scaledWidth = (int) ((float) scaledWidth / 1.1f);
                scaledHeight = (int) Math.ceil((float)scaledWidth / ratio);
                System.out.println("scaled: width = " + scaledWidth + ", height = " + scaledHeight);
            }
            Rectangle rec =  InternalShadowedWindow.computeCenterDialogLocation( animationBuffer.getWidth(), 
                                                                                 animationBuffer.getHeight(),
                                                                                 scaledWidth, 
                                                                                 scaledHeight);
            g2d2.drawImage(ImageUtilities.getScaled(resultBuffer, rec.width, rec.height), rec.x, rec.y, null);
        }else{
            Rectangle rec =  InternalShadowedWindow.computeCenterDialogLocation( animationBuffer.getWidth(), 
                                                                                 animationBuffer.getHeight(), 
                                                                                 resultWindowWidth + wMargin, 
                                                                                 resultWindowHeight + hMargin);
            g2d2.drawImage(resultBuffer, rec.x, rec.y, null);
        }
        animationPanel.repaint();
    }
    
    private void drawDiffsBubbles( Graphics g,
                                   List diffsPixels,
                                   Color bubbleColor,
                                   int diameter){
        g.setColor(bubbleColor);
        for(int pixel = 0; pixel<diffsPixels.size(); pixel++){
            int[] pixelCoords = (int[]) diffsPixels.get(pixel);
            g.fillOval(pixelCoords[0] - diameter/2, 
                       pixelCoords[1] - diameter/2, 
                       diameter, 
                       diameter);
        }
    }
    
    private BufferedImage createBubblesStrokeImage( BufferedImage alphaImage,
                                                    Color bubbleColor,
                                                    int diameter){
        BufferedImage bgImage = IgnoredBgPattern.createImage(alphaImage.getWidth(), 
                                                             alphaImage.getHeight());
        Graphics g = bgImage.getGraphics();
        g.setColor(bubbleColor);        
        for(int x=0; x<alphaImage.getWidth(); x++){
            for(int y=0; y<alphaImage.getHeight(); y++){
                int maskPixel = alphaImage.getRGB(x,y);
                int maskAlpha = maskPixel>>>24;
                if(maskAlpha>0){
                    g.fillOval(x - diameter/2, 
                               y - diameter/2, 
                               diameter, 
                               diameter);
                }
            }
        }    
        return bgImage;
    }

    private BufferedImage applyMask(BufferedImage mask, BufferedImage image1, BufferedImage image2){
        BufferedImage resultMaskedImage1 = applyMask(mask, image1, false);
        BufferedImage resultMaskedImage2 = applyMask(mask, image2, true);
    
        BufferedImage resultImage = gConf.createCompatibleImage(image1.getWidth(null), 
                                                                image1.getHeight(null),
                                                                Transparency.TRANSLUCENT);

        Graphics g = resultImage.getGraphics();
        g.drawImage(resultMaskedImage1, 0, 0, resultImage.getWidth(), resultImage.getHeight(), null);
        g.drawImage(resultMaskedImage2, 0, 0, resultImage.getWidth(), resultImage.getHeight(), null);
        return resultImage;
    }    
    
    private BufferedImage applyMask(BufferedImage mask, BufferedImage imageToKeep, boolean translucent){
        int maskRGB = MASK_COLOR.getRGB();

        BufferedImage resultImage = gConf.createCompatibleImage(image1.getWidth(null), 
                                                                image1.getHeight(null),
                                                                Transparency.TRANSLUCENT);

        for(int x=0; x<mask.getWidth(); x++){
            for(int y=0; y<mask.getHeight(); y++){
                int maskPixel = mask.getRGB(x,y);
                int resultPixel = -1;
                if(maskPixel==maskRGB){
                    if(translucent){
                        resultPixel = imageToKeep.getRGB(x,y) & 0x7FFFFFFF;
                    }else{
                        resultPixel = imageToKeep.getRGB(x,y);
                    }
                    
                }else{
                    resultPixel = maskPixel;
                }
                resultImage.setRGB(x, y, resultPixel);
            }
        }
        return resultImage;
    }    
    
    private boolean getDiffsPixels(List diffsPixels,
                                   BufferedImage image1, BufferedImage image2){
        boolean areSimilar = true;
        for(int x=0; x<image1.getWidth(); x++){
            for(int y=0; y<image1.getHeight(); y++){
                int image1Pixel = image1.getRGB(x,y);
                int image2Pixel = image2.getRGB(x,y);
                if(image1Pixel!=image2Pixel){
                    diffsPixels.add(new int[]{x, y});
                    areSimilar = false;
                }
            }
        }
        return areSimilar;
    }
        
    public boolean getLastComparisonMatched(){
        return lastComparisonMatched;
    }
           
    public BufferedImage getLastResultImage(){
        return lastResultImage;
    }
    
    public void clean(){
        cleanAnimationObjects();
        this.image1          = null;
        this.commentImg1     = null;
        this.commentImg2     = null;
        this.image2          = null;
        this.lastResultImage = null;       
    }
    
    private void cleanAnimationObjects(){
        if(this.animationBuffer!=null){
            Graphics g = this.animationBuffer.getGraphics();
            g.setColor(Color.GRAY);
            g.fillRect(0, 0, this.animationBuffer.getWidth(), this.animationBuffer.getHeight());
        }
        if(this.animationFrame!=null){
            this.animationFrame.hide();
        }        
    }
    
    public Frame getAnimationFrame(){
        return this.animationFrame;
    }
    
    public static void main(String args[]){
        try{
            System.out.println("step 1.2");
            JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));
            System.out.println("step 1.3");
            fileChooser.setDialogTitle("Choose first image:");
            System.out.println("step 1.4");
            fileChooser.showOpenDialog(new javax.swing.JFrame());
            System.out.println("step 2");
            File selectedFile = fileChooser.getSelectedFile();
            Image image1 = Screenshot.loadImageFromFile(selectedFile);
            fileChooser = new JFileChooser(selectedFile.getParentFile());
            fileChooser.setDialogTitle("Choose second image:");
            System.out.println("step 3");
            fileChooser.showOpenDialog(null);
            System.out.println("step 4");
            Image image2 = Screenshot.loadImageFromFile(fileChooser.getSelectedFile());
            ImageComparator imgComp = ImageComparator.getInstance(0.6,
                                                                  image1, 
                                                                  "From xxx.xxx.xx.xx1 (ihdsifoshfsdousfd)",
                                                                  image2,
                                                                  "From xxx.xxx.xx.xx2 (ihdsifoshfsdousfd)");
            long time = System.currentTimeMillis();
            imgComp.compareImages(true); //try with false to avoid animations...
            System.out.println("Done in: " + (System.currentTimeMillis()-time) 
                        + " ms. last match is : " + imgComp.lastComparisonMatched);
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
            fileChooser.setSelectedFile(new File("diff.png"));
            if(fileChooser.showSaveDialog(null)==JFileChooser.APPROVE_OPTION){
                ImageUtilities.storeImage(imgComp.getLastResultImage(), 
                                          fileChooser.getSelectedFile().getPath());
            }
            System.exit(0);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
