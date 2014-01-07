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

import java.awt.Frame;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;
import javax.swing.JFrame;

public class FrameFacilities {
    
    private static final Logger LOGGER = Logger.getLogger(FrameFacilities.class.getName());

    
    private FrameFacilities(){}
    

    public static Frame findClientWindow(String name, int tries) throws FrameNotFoundException{
        Frame window = null;
        boolean isObjectName = true;
        if(isClassName(name)){
            isObjectName = false;
        }
        while ((window == null || !window.isVisible()) && tries>0) {
            if(isObjectName){
                window = findClientWindowNamed(name);
            }else{
                try{
                    window = findClientWindowFromClass(extractClassName(name));
                }catch(Exception e){
                    throw new FrameNotFoundException("Unable to load frame from class : " + name, e);
                }
            }
            if(window!=null){
                LOGGER.fine("Window " + name + " FOUND, its title is " + window.getTitle());
            }else{
                tries--;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                }
            }
        }
        return window;
    }
    
    private static boolean isClassName(String className){
        return className.toLowerCase().startsWith("class:");
    }
    
    private static String extractClassName(String className){
        return className.substring("class:".length());
    }
    
    /**
     * This methods search for a frame which object name (Component getName() method) is the given parameter. 
     * 
     * @param objectName
     * @return the Frame.
     */
    private static Frame findClientWindowNamed(String name) {
        LOGGER.info("Displayed frames are: " + FrameFacilities.getDisplayedFramesString());

        Frame[] frames = JFrame.getFrames();
        for (int i = 0; i < frames.length; i++) {
            if (name.equals(frames[i].getName())) {
                return frames[i];
            }
        }
        LOGGER.fine("window " + name + " not found");
        return null;
    }   
    
    /**
     * For backward compatibility with jDiffChaser versions prior to 0.9
     * @param fullClassName
     * @return the Frame returned by the getFrame method the v.<0.9 required
     */
    public static Frame findClientWindowFromClass(String fullClassName) throws ClassNotFoundException, 
                                                                               NoSuchMethodException,
                                                                               IllegalAccessException,
                                                                               InvocationTargetException{
        Class clazz = Class.forName(fullClassName);
        Method getFrameMethod = clazz.getMethod("getFrame", (Class[]) null);
        LOGGER.finer("getFrameMethod is " + getFrameMethod);
        return (Frame) getFrameMethod.invoke((Object) null, (Object[]) null);
    }
     
    public static Frame findClientWindow(String objectName) throws FrameNotFoundException {
        return findClientWindow(objectName, 1);
    } 
    
    public static String getDisplayedFramesString() {
        StringBuffer sbuf = new StringBuffer();
        Frame[] frames = Frame.getFrames();
        for(int i=0; i<frames.length; i++){
            sbuf.append(frames[i].getName());
            sbuf.append("[").append(frames[i].getTitle()).append(", ").append(frames[i].getClass().getName()).append("]");
            sbuf.append(" , ");
        }
        return sbuf.toString();
    }    
    
}
