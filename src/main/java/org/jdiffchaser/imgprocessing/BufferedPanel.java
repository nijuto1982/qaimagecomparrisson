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

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 * A Panel that uses a buffered image to draw itself
 */
public class BufferedPanel extends JPanel{

    private BufferedImage buffer;

    public BufferedPanel(BufferedImage buffer){
        this.setDoubleBuffered(true);
        this.buffer = buffer;
    }

    public void paint(Graphics g){
        super.paint(g);
        g.drawImage(buffer, 0, 0, buffer.getWidth(), buffer.getHeight(), null);
    }
    
    public void setBuffer(BufferedImage buffer){
        this.buffer = buffer;
    }
}
