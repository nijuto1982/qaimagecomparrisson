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
import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import org.jdiffchaser.imgprocessing.*;

public class ExternalWaitWindow extends JFrame{
        
    private static final int WIDTH  = 300;
    private static final int HEIGHT = 140;
    
    private BufferedImage   buffer;     
    private boolean         useShadow = true;
    private DialogShadow    shadow;
    
    public ExternalWaitWindow() {
        super();
        this.setUndecorated(true);
        initialize();
    }
    
    public void setUseShadow(boolean use){
        this.useShadow = use;
    }
    
    public void initialize(){
        this.setSize(WIDTH, HEIGHT);
    }
    
    private Rectangle getWaitWindowBoundsCenteredOnScreen(){
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int)((screenSize.getWidth() - WIDTH) / 2.0);
        int y = (int)((screenSize.getHeight() - HEIGHT) / 2.0);
        return new Rectangle(x, y, WIDTH, HEIGHT);
    }
    
    public void start(String message) throws AWTException{
        buffer = new BufferedImage(WIDTH, 
                                   HEIGHT,
                                   BufferedImage.TYPE_INT_ARGB);
        Graphics g = buffer.getGraphics();
        
        Rectangle waitWindowBounds = getWaitWindowBoundsCenteredOnScreen();
        
        BufferedPanel panel = new BufferedPanel(buffer);
        this.getContentPane().add(panel, BorderLayout.CENTER);
        this.setBounds(waitWindowBounds);
        String os = System.getProperty("os.name").toLowerCase();
        boolean withShadow = (useShadow && os.indexOf("mac")<0);
        
        InternalWaitWindow.start(buffer, g, WIDTH-10, HEIGHT-50, 
                                 panel, message, withShadow);      
        makeItMasked();  
        if(withShadow){
            shadow = new DialogShadow(  (Frame) this,
                                        InternalWaitWindow.getMaskBounds(),
                                        this.getX(), 
                                        this.getY(),1.05, 10);        
        }
    }
    
    public void stop(){
        InternalWaitWindow.stop();
        if(this.shadow!=null){
            this.shadow.dispose();
            this.dispose();
        }
        this.setVisible(false);
    }
    
    private void makeItMasked(){
        try {
            this.setBackground(new Color(0,0,0,0));
            WindowUtils.setWindowMask(this, InternalWaitWindow.getMaskBounds());
            this.setVisible(true);

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
    
    public static void main(String[] args){
        try{
            System.setProperty("sun.java2d.noddraw", "true");
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());        
            ExternalWaitWindow ftww = new ExternalWaitWindow();
            ftww.start("Waiting for Remote Control...");
        }catch(Throwable t){
            t.printStackTrace();
        }
    }
    
}
