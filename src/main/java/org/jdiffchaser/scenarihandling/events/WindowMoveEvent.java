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
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;

public class WindowMoveEvent extends AWTEvent{
    
    private String key;
    private int x, y =0;
    
    public static String buildKey(Window window){
        StringBuffer sbuf = new StringBuffer();
        if(window instanceof Dialog){
            sbuf.append("Dialog").append('_');
        }else if(window instanceof Frame){
            sbuf.append("Frame").append('_');
        }else if(window instanceof Window){
            sbuf.append("Window").append('_');
        }
        sbuf.append(window.getWidth()).append('_');
        sbuf.append(window.getHeight());

        return sbuf.toString();
    }
    
    
    public WindowMoveEvent(String key, int x, int y) {
        super(new Object(), 0);
        this.key = key;
        this.x = x;
        this.y = y;
    }
    
    /**
     * @returns the new location x
     */
    public int getX(){
        return this.x;
    }
    
    /**
     * @returns the new location y
     */
    public int getY(){
        return this.y;
    }    
    
    /**
     * @returns the name of the moved window
     */
    public String getKey(){
        return this.key;
    }     
    
    public String toString(){
        return this.getKey() + " moved to " + this.getX() + "," + this.getY(); 
    }
    
}
