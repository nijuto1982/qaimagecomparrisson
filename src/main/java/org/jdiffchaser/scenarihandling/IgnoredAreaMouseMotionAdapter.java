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

import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class IgnoredAreaMouseMotionAdapter implements MouseMotionListener, MouseListener{

    public static class Mode{};
    public static final Mode RECTANGLE_MODE = new Mode();
    public static final Mode ELLIPSE_MODE   = new Mode();
    public static final Mode ERASE_MODE     = new Mode();

    private Point startPoint;
    private Point currentPoint = new Point();
    private Shape ignoredZone;
    private Container container;
    private Mode currentMode = RECTANGLE_MODE;
    
    private List ignoredZones = new ArrayList();
    
    /** Creates a new instance of IgnoredAreaAdapter */
    public IgnoredAreaMouseMotionAdapter(Container container) {
        this.container = container;
    }
    
    public void reset(){
        if(ignoredZones!=null){
            ignoredZones.clear();
        }
        ignoredZones = null;
        ignoredZone = null;
    }
    
    public Shape getCurrentIgnoredZone(){
        return this.ignoredZone;
    }

    public List getIgnoredZones(){
        return this.ignoredZones;
    }

    public void setIgnoredZones(List zones){
        this.ignoredZones = zones;
        SwingUtilities.windowForComponent(this.container).pack();
        this.container.invalidate();
        this.container.repaint();
    }
    
    public void mouseDragged(MouseEvent me){
        
        int newX = Math.max(me.getPoint().x, 0);
        newX = Math.min(newX, this.container.getWidth());
        int newY = Math.max(me.getPoint().y, 0);
        newY = Math.min(newY, this.container.getHeight());

        if(this.currentMode==RECTANGLE_MODE){
            ignoredZone = new Rectangle(Math.min(this.startPoint.x, newX),
                                        Math.min(this.startPoint.y, newY),
                                        Math.abs(newX - this.startPoint.x),
                                        Math.abs(newY - this.startPoint.y));
        }else if(this.currentMode==ELLIPSE_MODE){
            ignoredZone = new Ellipse2D.Double( Math.min(this.startPoint.x, newX),
                                                Math.min(this.startPoint.y, newY),
                                                Math.abs(newX - this.startPoint.x),
                                                Math.abs(newY - this.startPoint.y));
        }
        this.container.invalidate();
        this.container.repaint();
    }

    public void mouseMoved(MouseEvent me){
        this.currentPoint.x = me.getX();
        this.currentPoint.y = me.getY();
        if(this.currentMode==ERASE_MODE){
            this.container.invalidate();
            this.container.repaint();
        }
    }
    
    public void mousePressed(MouseEvent me){
        this.startPoint = me.getPoint();
    }
    
    public void mouseReleased(MouseEvent me){
        if(this.currentMode==ERASE_MODE){
            //need to erase all zones containing the position the cursor had at the release moment.
            List shapesToRemove = new ArrayList();
            //listing
            for(int i=0; i<this.ignoredZones.size(); i++){
                Shape shape = (Shape)this.ignoredZones.get(i);
                if(shape.contains(me.getPoint())){
                    shapesToRemove.add(shape);
                }
            }

            if( JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(this.container,
                                                            "Do you really want to remove\nthe"
                                                            + (shapesToRemove.size()>1?" "+shapesToRemove.size():"")
                                                            + " selected zone"
                                                            + (shapesToRemove.size()>1?"s":""),
                                                            "Are you sure?",
                                                            JOptionPane.OK_CANCEL_OPTION)){
                //removing
                for(int i=0; i<shapesToRemove.size(); i++){
                    this.ignoredZones.remove(shapesToRemove.get(i));
                }
            }
        }else{
            if(this.ignoredZone.getBounds().isEmpty()){
                JOptionPane.showMessageDialog(this.container,
                                              "Your area is empty, you can't add it.",
                                              "Oups...",
                                              JOptionPane.OK_OPTION);
            }else{
                if( JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(this.container,
                                                                "Do you really want to ignore\nthis zone during the tests?",
                                                                "Are you sure?",
                                                                JOptionPane.OK_CANCEL_OPTION)){
                    this.ignoredZones.add(this.ignoredZone);
                }
            }
        }
        
        this.ignoredZone=null;
        this.container.invalidate();
        this.container.repaint();
    }
    
    public void mouseEntered(MouseEvent me){
    }

    public void mouseExited(MouseEvent me){
    }

    public void mouseClicked(MouseEvent me){
    }
    
    public Mode getMode(){
        return this.currentMode;
    }

    public void setMode(Mode mode){
        this.currentMode = mode;
    }

    public Point getCursorPosition(){
        return this.currentPoint;
    }

}
