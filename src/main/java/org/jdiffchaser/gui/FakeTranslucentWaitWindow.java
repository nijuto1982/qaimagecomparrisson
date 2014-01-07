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

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.image.BufferedImage;
import javax.swing.JWindow;
import org.jdiffchaser.imgprocessing.*;

public class FakeTranslucentWaitWindow extends JWindow{
    
    private static final int WIDTH  = 300;
    private static final int HEIGHT = 140;
    
    private boolean started = false;
    
    public FakeTranslucentWaitWindow() {
        super();
        initialize();
    }
    
    public FakeTranslucentWaitWindow(Frame owner) {
        super(owner);
        initialize();
    }    
    
    public FakeTranslucentWaitWindow(GraphicsConfiguration gc) {
        super(gc);
        initialize();
    }  
    
    public FakeTranslucentWaitWindow(Window owner) {
        super(owner);
        initialize();
    }  
    
    public FakeTranslucentWaitWindow(Window owner, GraphicsConfiguration gc) {
        super(owner, gc);
        initialize();
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
        BufferedImage buffer = new BufferedImage(WIDTH, 
                                                 HEIGHT,
                                                 BufferedImage.TYPE_INT_RGB);
        Graphics g = buffer.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        
        Rectangle waitWindowBounds = getWaitWindowBoundsCenteredOnScreen();
        
        BufferedImage belowWaitWindow = new Robot().createScreenCapture(waitWindowBounds);
        g.drawImage(belowWaitWindow, 0, 0, null);
        BufferedPanel panel = new BufferedPanel(buffer);
        this.getContentPane().add(panel, BorderLayout.CENTER);
        this.setBounds(waitWindowBounds);
        this.setVisible(true);
        InternalWaitWindow.start(buffer, g, WIDTH-10, HEIGHT-50, panel, message, true);        
    }
    
    public void stop(){
        InternalWaitWindow.stop();
        this.dispose();
    }
    
    public static void main(String[] args){
        FakeTranslucentWaitWindow ftww = new FakeTranslucentWaitWindow();
        try{
            ftww.start("Waiting for Remote Control...");
        }catch(Throwable t){
            t.printStackTrace();
        }
    }
    
}
