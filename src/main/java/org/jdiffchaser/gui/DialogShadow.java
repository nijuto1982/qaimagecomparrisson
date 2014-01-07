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

import com.sun.jna.examples.WindowUtils;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import org.jdiffchaser.imgprocessing.BufferedPanel;
import org.jdiffchaser.gui.ExternalWaitWindow;
import org.jdiffchaser.gui.InternalWaitWindow;

public class DialogShadow extends JDialog{
    
    private ImageIcon mask;
    
    public DialogShadow(Frame owner, Shape windowShape, int x, int y, double factor, int shadowMargin){
        super(owner);
        this.setUndecorated(true);
        this.setBackground(new Color(0, 0, 0, 0));
        
        AffineTransform scaleup = new AffineTransform();
        scaleup.setToScale(factor, factor);
        Shape wholeShadow = scaleup.createTransformedShape(windowShape);
        
        Area shadow = new Area(wholeShadow);
        AffineTransform translate = new AffineTransform();
        translate.setToTranslation(-shadowMargin, -shadowMargin);
        Area windowArea = new Area(translate.createTransformedShape(windowShape));
        shadow.subtract(windowArea);
        BufferedImage buffer = createShadowImage(shadow, factor);
        this.mask = new ImageIcon(buffer);
        this.setContentPane(new BufferedPanel(buffer));
        
        this.setSize((int)shadow.getBounds().getWidth() + 2 * (int)shadow.getBounds().getX(), 
                     (int)shadow.getBounds().getHeight() + 2 * (int)shadow.getBounds().getY());
        
        this.setLocation(owner.getX() + shadowMargin, owner.getY() + shadowMargin);
        
        makeItMasked();
    }

    private BufferedImage createShadowImage(Shape shadow, double factor){
        BufferedImage buffer = new BufferedImage((int)shadow.getBounds().getWidth() + 2 * (int)shadow.getBounds().getX(),
                                                 (int)shadow.getBounds().getHeight() + 2 * (int)shadow.getBounds().getY(),
                                                 BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2d = (Graphics2D) buffer.getGraphics();
        g2d.setColor(Color.BLACK);
        g2d.fill(shadow);
        return buffer;
    }
            
    private void makeItMasked(){
        try {
            WindowUtils.setWindowMask(this, this.mask);
            this.setVisible(true);
            WindowUtils.setWindowAlpha(this, 0.5f);
        }catch(UnsatisfiedLinkError e) {
            handleLinkError(e);
        }        
    }    

    private void handleLinkError(Error e){
            e.printStackTrace();
            String msg = e.getMessage() 
                         + "\nError loading the JNA library after looking in "
                         + System.getProperty("jna.library.path");
            JTextArea area = new JTextArea(msg);
            area.setOpaque(false);
            area.setFont(UIManager.getFont("Label.font"));
            area.setEditable(false);
            area.setColumns(80);
            area.setRows(8);
            area.setWrapStyleWord(true);
            area.setLineWrap(true);
            JOptionPane.showMessageDialog(this, new JScrollPane(area), 
                                          "Library Load Error: "
                                          + System.getProperty("os.name")
                                          + "/" + System.getProperty("os.arch"),
                                          JOptionPane.ERROR_MESSAGE);
            System.exit(1);        
    }
    
    public static void main(String args[]){
        try{
            System.setProperty("sun.java2d.noddraw", "true");
            ExternalWaitWindow ftww = new ExternalWaitWindow();
            ftww.start("Waiting for Remote Control...");        
            DialogShadow shadow = new DialogShadow((Frame) ftww,
                                                   InternalWaitWindow.getMaskBounds(), 
                                                   ftww.getX(), ftww.getY(),1.05, 10);
            
        }catch(Exception e){
            e.printStackTrace();
        }
    }

}
