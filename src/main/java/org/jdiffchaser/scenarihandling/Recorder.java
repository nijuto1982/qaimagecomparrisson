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

import java.awt.Window;
import java.awt.event.ComponentEvent;
import org.jdiffchaser.imgprocessing.ScreenshotException;
import org.jdiffchaser.scenarihandling.events.ComparisonEvent;
import org.jdiffchaser.scenarihandling.events.WindowMoveEvent;
import org.jdiffchaser.scenarihandling.events.InputModeEvent;
import org.jdiffchaser.scenarihandling.events.StartEvent;
import org.jdiffchaser.scenarihandling.notifications.ControlNotification;
import org.jdiffchaser.scenarihandling.notifications.ScreenshotNotification;
import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Frame;
import java.awt.IllegalComponentStateException;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.logging.Logger;
import javax.management.NotificationBroadcasterSupport;
import javax.swing.JOptionPane;
import org.jdiffchaser.scenarihandling.events.RecordableEvent;
import java.awt.Toolkit;
import java.io.File;
import org.jdiffchaser.gui.FrameFacilities;
import org.jdiffchaser.gui.FrameNotFoundException;

public class Recorder extends NotificationBroadcasterSupport implements RecorderMBean{
    
    private static final Logger LOGGER = Logger.getLogger(Recorder.class.getName());
    
    private Frame               clientWindow;
    private long                previousActionTime;
    private long                newActionTime;
    private String              fullFilePath;
    private ObjectOutputStream  eventsOutputStream;
    private String              outputDirPath;
    private boolean             recording;
    private boolean             controlling;
    private boolean             screenshoting;
    public  static final String SCENARIO_EXTENSION = ".sc";
        
    private boolean             oneSideInputMode = false;
    
    private int                 contSeq   = 0;
    private int                 screenSeq = 0;
        
    public Recorder( String windowObjectName, String outputDirPath,
                    String host, int jmxPort) throws FileNotFoundException, IOException, FrameNotFoundException{
                    
        //tries 2 minutes to find the window to record actions from
        this(FrameFacilities.findClientWindow(windowObjectName, 120), outputDirPath, host, jmxPort);
    }
    
    
    public Recorder(Frame clientWindow, String outputDirPath,
                    String host, int jmxPort) throws FileNotFoundException, IOException{
        
        new RecorderServer(this, host, jmxPort);
                
        this.clientWindow = clientWindow;

        clientWindow.requestFocus();
        
        this.outputDirPath  = outputDirPath;
        initTimes();
        
        Toolkit.getDefaultToolkit().addAWTEventListener(
                new AWTEventListener() {
            public void eventDispatched(AWTEvent e) {

                setControlling(false);
                
                if(recording && !controlling && !screenshoting){ //in a recording session but not a remote control event
                    previousActionTime = newActionTime;
                    newActionTime = System.currentTimeMillis();
                    storeAction(e, newActionTime-previousActionTime);
                }
            }
        }, AWTEvent.MOUSE_MOTION_EVENT_MASK
                + AWTEvent.MOUSE_EVENT_MASK
                + AWTEvent.MOUSE_WHEEL_EVENT_MASK
                + AWTEvent.KEY_EVENT_MASK
                + AWTEvent.COMPONENT_EVENT_MASK
                );
        
    }
    
    private void initTimes(){
        previousActionTime  = System.currentTimeMillis();
        newActionTime       = System.currentTimeMillis();
    }
    
    public void setControlling(boolean controlling){
        this.controlling = controlling;
        //notify RC state (is recorder being controlled or not? (mouse in RC))
        this.sendNotification(new ControlNotification(String.valueOf(controlling), this, contSeq));
    }
    
    public boolean handleScenarioStart(String scenarioName){
        recording = !recording;
        if(recording){
            StartEvent startEvent = new StartEvent(this.clientWindow, Integer.MIN_VALUE,
                    scenarioName, true); //true is used here for backward compatibility
            //with already written scenarii
            oneSideInputMode = false;
            initTimes();
            createStream(startEvent);
        }else{
            closeStream();
        }
        return recording;
    }
    
    public void setSingleInput(boolean singleInput){
        controlling = true;
        
        InputModeEvent inputModeEvent = new InputModeEvent( this.clientWindow, Integer.MIN_VALUE,
                singleInput);
        
        oneSideInputMode = singleInput;
        storeAction(inputModeEvent, 250);
        
        controlling = false;
    }
    
    /**
     * @param scName teh scenario name
     * @param soloInputScenario true if this scenario must be played only one one client because the other will
     * receive data from this input
     */
    private void createStream(StartEvent startEvent){
        this.fullFilePath = outputDirPath + '/'
                + startEvent.getScenarioName() + SCENARIO_EXTENSION;
        File file = new File(outputDirPath);
        file.mkdirs();
        try{
            eventsOutputStream = new ObjectOutputStream(new FileOutputStream(fullFilePath));
            storeAction(startEvent, 250);
            setSingleInput(false);
        }catch(Exception e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(this.clientWindow,
                    "Unable to create the scenario file " + fullFilePath
                    + ": " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void closeStream(){
        try{
            eventsOutputStream.close();
            LOGGER.info("Scenario stored to : " + this.fullFilePath);
        }catch(Exception e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(this.clientWindow,
                    "Unable to create the scenario file (" + this.fullFilePath + "): " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void handleComparison(final boolean fullScreen, final long delay) throws ScreenshotException{
        new Thread(){
            public void run(){
                try{
                    Thread.sleep(delay);
                    //we store time elpased BEFORE showing dialog because when the scenario is played
                    //we don't want to wait the time we had spent setting the comparison zones.
                    previousActionTime = newActionTime;
                    newActionTime = System.currentTimeMillis();
                    
                    screenshoting = true;
                    LOGGER.info("preparing for screenshot on " + Recorder.this.clientWindow.getName());
                    ComparisonEvent comparisonEvent = ComparisonDialog.show(Recorder.this.clientWindow, fullScreen);
                    if(comparisonEvent!=null){
                        LOGGER.info("ComparisonEvent sent");
                        storeAction(comparisonEvent, newActionTime-previousActionTime);
                    }
                    initTimes();
                    screenshoting = false;
                    Recorder.this.sendNotification(new ScreenshotNotification("Screenshot done", Recorder.this,
                            screenSeq));
                }catch(Exception e){
                    e.printStackTrace();
                }
            }

        }.start();
    }
    
    private void storeAction(AWTEvent e, long timeDiff){
        
        try{
            if(e instanceof MouseEvent){
                handleMouseEvent((MouseEvent) e, timeDiff);
            }else if(e instanceof ComponentEvent){
                handleComponentEvent((ComponentEvent) e, timeDiff);
            }else{
                LOGGER.finest("event is " + e);
                eventsOutputStream.writeObject(new RecordableEvent(e, timeDiff));
            }
        }catch(IOException ioe){
            ioe.printStackTrace();
        }
    }
    
    private void handleMouseEvent(MouseEvent me, long timeDiff) throws IOException{
        //the mouseEvent position refers to source component coordinates so we need to transform it to
        //screen coordinates (see Robot class)
        try{
            MouseEvent nme = new MouseEvent((Component)me.getSource(),
                    me.getID(),
                    me.getWhen(),
                    me.getModifiers(),
                    me.getX()+ ((Component)me.getSource()).getLocationOnScreen().x,
                    me.getY()+ ((Component)me.getSource()).getLocationOnScreen().y,
                    me.getClickCount(),
                    me.isPopupTrigger(),
                    me.getButton())
                    ;
            eventsOutputStream.writeObject(new RecordableEvent(nme, timeDiff));
        }catch(IllegalComponentStateException icse){
            LOGGER.warning("avoiding storing this event because of : " + icse.getMessage()
            + "\nEvent was: " + me);
        }
    }
    
    private void handleComponentEvent(ComponentEvent ce, long timeDiff) throws IOException{
        LOGGER.finest("Handling component event " + ce);
        if(ce.getComponent() instanceof Window){
            WindowMoveEvent fme = new WindowMoveEvent(WindowMoveEvent.buildKey((Window)ce.getComponent()),
                    ce.getComponent().getX(), ce.getComponent().getY());
            eventsOutputStream.writeObject(new RecordableEvent(fme, timeDiff));
        }else{
            eventsOutputStream.writeObject(new RecordableEvent(ce, timeDiff));
        }
    }
    
    public void remoteControlReady(){
        LOGGER.warning("Closing Remote control");
        this.clientWindow.toFront();
    }
    
    public boolean isRecording(){
        return this.recording;
    }
    
    public void setClientWindowVisible(boolean visible){
        this.clientWindow.setVisible(visible);
    }
    
    public void exit(){
        System.exit(0);
    }
    
}
