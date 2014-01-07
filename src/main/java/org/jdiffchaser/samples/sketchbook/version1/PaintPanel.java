/*
 * PaintPanel.java
 *
 * Created on 21. mai 2007, 14:09
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.jdiffchaser.samples.sketchbook.version1;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

public class PaintPanel extends JPanel{
    
    private BufferedImage buffer;
    
    public PaintPanel() {
    }
    
    public void createBuffer(){
        if(this.buffer == null){
            this.buffer = new BufferedImage(this.getWidth(), this.getHeight(), 
                                            BufferedImage.TYPE_INT_RGB);
            this.buffer.getGraphics().setColor(Color.WHITE);
            erase();
        }
    }
    
    public Graphics2D getBufferGraphics(){
        return (Graphics2D)this.buffer.getGraphics();
    }
    
    public void paint(Graphics g){
        createBuffer();
        g.drawImage(buffer, 0, 0, null);
    }
    
    public void erase(){
        this.buffer.getGraphics().fillRect(0,0,this.getWidth(), this.getHeight());
    }
    
}
