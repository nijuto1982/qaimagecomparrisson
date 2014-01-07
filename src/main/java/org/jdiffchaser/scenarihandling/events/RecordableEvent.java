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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class RecordableEvent implements Serializable{
    private long timeElapsed;
    private AWTEvent awte;
        
    /**
     * Safe because of Serialization
     */
    private static final long serialVersionUID = RecordableEvent.class.getName().hashCode();    

    public RecordableEvent(AWTEvent awte, long timeElapsed){
        this.timeElapsed = timeElapsed;
        this.awte        = awte;
    }

    private void writeObject(ObjectOutputStream out) throws IOException{
        out.writeLong(timeElapsed);
        out.writeObject(awte);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException{
        this.timeElapsed = in.readLong();
        this.awte        = (AWTEvent) in.readObject();
    }        
    
    public long getTimeElapsedBeforeEvent(){
        return timeElapsed;
    }
    
    public AWTEvent getEvent(){
        return awte;
    }
    
    public String toString(){
        StringBuffer sbuf = new StringBuffer();
        sbuf.append(this.timeElapsed).append(" ms later: ");
        sbuf.append(awte.getClass().getName());
        return sbuf.toString();
    }
}

