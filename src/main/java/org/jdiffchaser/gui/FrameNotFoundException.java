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

public class FrameNotFoundException extends Exception{
    
    public  FrameNotFoundException(){
        super();
    }
    
    public FrameNotFoundException(String message){
        super(message);
    }
    
    public FrameNotFoundException(String message, Throwable t){
        super(message, t);
    }
    
    public FrameNotFoundException(Throwable t){
        super(t);
    } 
    
}
