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

import com.sun.jna.examples.WindowUtils;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

/**
 * A draggable window to control recorder
 */
public class RemoteControlFrame extends JFrame{
    
    private static final Logger LOGGER = Logger.getLogger(RemoteControlFrame.class.getName());
    
    public static native void setWindowAlwaysOnTop(String titre, boolean b);
        
    private RecorderMBean recorder;
    private int mx, my, x, y;
    private static final int MARGIN = 20;

    private RemoteControlPanel remoteControlPanel;
    
    public RemoteControlFrame(int width, int height, int jmxPort) {
        
        remoteControlPanel = new RemoteControlPanel();
        this.setName("jDiffChaser RemoteControl");
        RecorderHandle recorderHandle = null;
        try{       
            recorderHandle = new RecorderHandle(InetAddress.getLocalHost().getHostAddress(), 
                                                jmxPort);
        }catch(Exception e){
            e.printStackTrace();
            System.exit(1);
        }
                
        boolean succeed = false;
        while(!succeed){
            try{
                Thread.sleep(500);
                recorderHandle.contact(remoteControlPanel);
                succeed=true;
            }catch(Exception e){
                LOGGER.log(Level.WARNING, "retrying to connect...", e);
            }                
        }        

        this.recorder = recorderHandle.getProxy();
        this.remoteControlPanel.setRecorder(recorder);
        
        this.remoteControlPanel.setTranslucencyListener(new RemoteControlPanel.TranslucencyListener(){
           public void translucencyChanged(float alpha){
               makeItTranslucent(alpha);
           }
        });
        
        this.remoteControlPanel.addMouseMotionListener(new MouseMotionAdapter(){
            
            public void mouseMoved(MouseEvent m){
                RemoteControlFrame.this.recorder.setControlling(true);                   
                mx = m.getX();
                my = m.getY();                    
            }
            
            public void mouseDragged(MouseEvent m){
                int x = m.getX()-mx;
                int y = m.getY()-my;
                setLocation(getX()+x, getY()+y);
                mx = m.getX()-x;
                my = m.getY()-y;
            }            
        });    
        
        this.getContentPane().add(remoteControlPanel);
        setUndecorated(true);
        setTitle(String.valueOf(this.hashCode()));
        setSize(width,height);
        makeItTranslucent(1.0f);
        try{
            setAlwaysOnTop(true);
            setTitle("Remote Control");
        }catch(NoSuchMethodError error){
            LOGGER.info("Using native method to keep window always on top");
            loadNativeOnTopLib();
            setWindowAlwaysOnTop(getTitle(),true);        
        }
        recorderHandle.getProxy().remoteControlReady();
    }
    
    private void loadNativeOnTopLib(){
        String os = System.getProperty("os.name").toLowerCase();
        System.out.println("os.name is " + os);
        if(os.indexOf("win")>=0){
            System.out.println("Loading Windows native library");
            System.loadLibrary("RCtrlFrameOnTop");     
            System.out.println("Native lib loaded");
        }        
    }
    
    private void makeItTranslucent(float alpha){
        try {
            WindowUtils.setWindowAlpha(this, alpha);
            this.setVisible(true);
            this.remoteControlPanel.updateUI();
            
        }catch(UnsatisfiedLinkError e) {
            e.printStackTrace();
            String msg = e.getMessage() 
                + "\nError loading the JNA library after looking in "
                + System.getProperty("jna.library.path");
            JTextArea area = new JTextArea(msg);
            area.setOpaque(false);
            area.setFont(UIManager.getFont("Label.font"));
            area.setEditable(false);
            area.setColumns(80);
            area.setRows(8);
            area.setWrapStyleWord(true);
            area.setLineWrap(true);
            JOptionPane.showMessageDialog(this, new JScrollPane(area), 
                                          "Library Load Error: "
                                          + System.getProperty("os.name")
                                          + "/" + System.getProperty("os.arch"),
                                          JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }        
    }
    
    public static void main(String[] args){
        try{
            System.setProperty("sun.java2d.noddraw", "true");
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(Exception e){}
        
        try{
            RemoteControlFrame rcFrame = new RemoteControlFrame(510, 205, 
                                                                Integer.parseInt(args[0]));
        }catch(Exception e){
            e.printStackTrace();
            System.exit(1);
        }
    }
    
}
