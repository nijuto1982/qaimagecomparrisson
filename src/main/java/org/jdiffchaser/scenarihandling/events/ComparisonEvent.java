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

package org.jdiffchaser.scenarihandling.events;

import java.awt.AWTEvent;
import java.awt.Event;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.List;


public class ComparisonEvent extends AWTEvent{
    //for backward compatibility with scenarios written with previous version of jdiffchaser
    static final long serialVersionUID = -313186075761774392L; 
    
    private boolean fullScreen;
    
    private List ignoredZones; //Shapes
    
    public ComparisonEvent(Event event) {
        super(event);
    }
    
    public ComparisonEvent(Object source, int id, List ignoredZones) {
        this(source, id, ignoredZones, false);
    }
    
    public ComparisonEvent(Object source, int id, List ignoredZones, boolean fullScreen) {
        super(source, id);
        this.ignoredZones = ignoredZones;
        this.fullScreen = fullScreen;
    }
    
    public List getIgnoredZones(){
        return this.ignoredZones;
    }

    public void setIgnoredZones(List zones){
        this.ignoredZones = zones;
    }

    public boolean isFullScreen(){
        return this.fullScreen;
    }

    public Point getLowerRighter(){
        return getLowerRighter(this.ignoredZones);
    }

    public static Point getLowerRighter(List ignoredZones){
        int x = 0;
        int y = 0;
        for(int i=0; i<ignoredZones.size(); i++){
            Shape ignoredZone = (Shape) ignoredZones.get(i);
            Rectangle bounds = ignoredZone.getBounds();
            int right  = bounds.x + bounds.width;
            int bottom = bounds.y + bounds.height;
            if(right>x){
                x = right;
            }
            if(bottom>y){
                y = bottom;
            }
        }
        return new Point(x, y);
    }
    
    public String toString(){
        StringBuffer sbuf = new StringBuffer();
        sbuf.append("fullscreen comparison? " + this.fullScreen).append(" / ");
        sbuf.append("Ignored Zones : " + this.ignoredZones);
        return sbuf.toString();
    }

    public static ComparisonEvent loadFromFile(File file) throws Exception{
        ObjectInputStream ois = null;
        ComparisonEvent comparisonEvent = null;
        try{
            ois = new ObjectInputStream(new FileInputStream(file));
            comparisonEvent = (ComparisonEvent) ois.readObject();
        }finally{
            if(ois!=null){
                try{
                    ois.close();
                }catch(Exception e){}
            }
        }
        return comparisonEvent;
    }

}
