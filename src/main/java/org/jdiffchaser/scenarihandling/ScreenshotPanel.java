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

package org.jdiffchaser.scenarihandling;
import java.awt.BasicStroke;
import org.jdiffchaser.imgprocessing.IgnoredBgPattern;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.JPanel;

public class ScreenshotPanel extends JPanel{

    private BufferedImage image;
    private BufferedImage patternImage;
    private IgnoredAreaMouseMotionAdapter ignoredAreaAdapter;
    private static final Stroke WIDE_STROKE = new BasicStroke(3.0f);
    private static final Stroke THIN_STROKE = new BasicStroke(1.0f);

    public ScreenshotPanel(BufferedImage image, boolean editable){
        this.image = image;
        patternImage = IgnoredBgPattern.createImage(image.getWidth(), image.getHeight(),
                                                    Color.LIGHT_GRAY.darker(),
                                                    Color.DARK_GRAY.darker(),
                                                    Color.LIGHT_GRAY);
        this.setSize(image.getWidth(), image.getHeight());
        this.setMaximumSize(new Dimension(image.getWidth(), image.getHeight()));
        this.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        if(editable){
            ignoredAreaAdapter = new IgnoredAreaMouseMotionAdapter(this);

            this.addMouseListener(ignoredAreaAdapter);
            this.addMouseMotionListener(ignoredAreaAdapter);
        }
    }

    public void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        Shape originalClip = g2d.getClip();
        g2d.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), this);
        if(ignoredAreaAdapter!=null){
            if(ignoredAreaAdapter.getIgnoredZones()!=null){
                for(int i=0; i<ignoredAreaAdapter.getIgnoredZones().size(); i++){
                    g2d.setClip(originalClip);
                    Shape shape = (Shape)ignoredAreaAdapter.getIgnoredZones().get(i);
                    Rectangle bounds = shape.getBounds();
                    g2d.clip(shape);
                    Image subImage = patternImage.getSubimage(bounds.x,
                                                              bounds.y,
                                                              bounds.width,
                                                              bounds.height);
                    g2d.drawImage(subImage, bounds.x, bounds.y, bounds.width, bounds.height, this);
                }
                g2d.setClip(originalClip);
                if(getToolMode()==IgnoredAreaMouseMotionAdapter.ERASE_MODE){
                    for(int i=0; i<ignoredAreaAdapter.getIgnoredZones().size(); i++){
                        Shape shape = (Shape)ignoredAreaAdapter.getIgnoredZones().get(i);
                        if(shape.contains(this.ignoredAreaAdapter.getCursorPosition())){
                            g2d.setColor(Color.ORANGE);
                            g2d.setStroke(WIDE_STROKE);
                            g2d.draw(shape);
                        }
                    }
                }
            }
            if(ignoredAreaAdapter.getCurrentIgnoredZone()!=null){
                g2d.setStroke(THIN_STROKE);
                g2d.setColor(Color.CYAN);
                g2d.draw(ignoredAreaAdapter.getCurrentIgnoredZone());
            }
        }
        g2d.dispose();
    }
    
    public void resetIgnoredAreas(){
        ignoredAreaAdapter.reset();
    }
    
    public List getIgnoredZones(){
        return this.ignoredAreaAdapter.getIgnoredZones();
    }

    public void setIgnoredZones(List zones){
        this.ignoredAreaAdapter.setIgnoredZones(zones);
    }

    public void setToolMode(IgnoredAreaMouseMotionAdapter.Mode mode){
        this.ignoredAreaAdapter.setMode(mode);
    }

    public IgnoredAreaMouseMotionAdapter.Mode getToolMode(){
        return this.ignoredAreaAdapter.getMode();
    }
    
}

