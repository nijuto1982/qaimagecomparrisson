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

import org.jdiffchaser.scenarihandling.events.RecordableEvent;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import javax.swing.JFileChooser;
import org.jdiffchaser.scenarihandling.events.ComparisonEvent;


public class ScenarioEditor {
        
    public ScenarioEditor() {
    }
    
    public static void main(String args[]){
          
        JFileChooser chooser = new JFileChooser(".");
        chooser.showOpenDialog(null);
        
        File file = chooser.getSelectedFile();
         
        try{
           
           parse(file);
            
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            System.exit(0);
        }
    }
    
    public static void parse(File file) throws PlayerException{
        ObjectInputStream in = null;
        try{
            in = new ObjectInputStream(new FileInputStream(file));

            RecordableEvent recEvent = null;

            Object obj = in.readObject();
            while( obj!=null ){
                recEvent = (RecordableEvent) obj;
                System.out.println(recEvent.toString());
                try{
                    obj = in.readObject();
                }catch(EOFException eofe){
                    obj = null;
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            try{
                in.close();
            }catch(Exception e){
                e.printStackTrace();
            }   
        }
    }

    public static void putIgnoredZonesIntoScenario(List ignoredZones, File file){
        File bakFile = new File(file.getAbsolutePath()+".bak");
        if(bakFile.exists()){
            bakFile.delete();
        }
        file.renameTo(bakFile);

        ObjectInputStream in   = null;
        ObjectOutputStream out = null;
        RecordableEvent recEvent = null;
        try{
            in  = new ObjectInputStream(new FileInputStream(bakFile));
            out = new ObjectOutputStream(new FileOutputStream(file));

            Object obj = in.readObject();
            while( obj!=null ){
                recEvent = (RecordableEvent) obj;
                if(recEvent.getEvent() instanceof ComparisonEvent){
                    ((ComparisonEvent)recEvent.getEvent()).setIgnoredZones(ignoredZones);
                }
                out.writeObject(recEvent);
                try{
                    obj = in.readObject();
                }catch(EOFException eofe){
                    obj = null;
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            try{
                in.close();
            }catch(Exception e){
                e.printStackTrace();
            }
            try{
                out.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public static ComparisonEvent extractComparison(File file){
        ObjectInputStream in = null;
        RecordableEvent recEvent = null;
        boolean compFound = false;
        try{
            in = new ObjectInputStream(new FileInputStream(file));

            Object obj = in.readObject();
            while( obj!=null ){
                recEvent = (RecordableEvent) obj;
                if(recEvent.getEvent() instanceof ComparisonEvent){
                    compFound = true;
                    break;
                }
                try{
                    obj = in.readObject();
                }catch(EOFException eofe){
                    obj = null;
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            try{
                in.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        if(compFound){
            return (ComparisonEvent) recEvent.getEvent();
        }else{
            return null;
        }
    }
    
}
