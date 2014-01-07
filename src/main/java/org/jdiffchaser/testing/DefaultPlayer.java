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

package org.jdiffchaser.testing;

import org.jdiffchaser.gui.FrameNotFoundException;
import org.jdiffchaser.scenarihandling.Player;
import org.jdiffchaser.scenarihandling.PlayerListener;
import org.jdiffchaser.scenarihandling.RemoteControlFrame;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;



public class DefaultPlayer {
    
    private static final Logger LOGGER = Logger.getLogger(DefaultPlayer.class.getName());    
    
    private static final int TIME_TO_WAIT = 2000;
        
    static{
        String os = System.getProperty("os.name").toLowerCase();
        System.out.println("os.name is " + os);
        if(os.indexOf("win")>=0){
            System.out.println("Loading Windows native library");
            System.loadLibrary("RCtrlFrameOnTop");     
            System.out.println("Native lib loaded");
        }
    }
  

    public static class PlayerStateListener implements PlayerListener{
        
        private final Object lock = new Object();
        
        private String screenshotsDirectory;
        private boolean lastScenario = false;

        public Object getPlayerLock(){
            return lock;
        }

        public void setLastScenario(boolean bool){
            this.lastScenario = bool;
        }

        public PlayerStateListener(String scrDir){
            this.screenshotsDirectory = scrDir;
        }
        
        public void playerStarted(){
            LOGGER.info("Player Started");
        }

        public void playerAborted(Throwable t){
            LOGGER.log(Level.WARNING, "Player Aborted!!!", t);        
        }

        public void playerTerminated(){
            LOGGER.info("Scenario terminated");
            if(this.lastScenario){
                LOGGER.info("Player successfully terminated");
                JFrame dummyF = new JFrame("dummyF"); //dummy frame to make the OptionPane "OnTop"
                dummyF.setVisible(true);
                try{
                    dummyF.setAlwaysOnTop(true);
                }catch(NoSuchMethodError error){
                    RemoteControlFrame.setWindowAlwaysOnTop("dummyF", true);
                }
                JOptionPane.showMessageDialog(dummyF, "End of (all) scenario(s) reached :)"
                                                    + "\nYou can now exit the application and\ncheck the screenshot(s) \n"
                                                    + "into " + screenshotsDirectory + " directory."
                                              , "Player stopped",
                                              JOptionPane.INFORMATION_MESSAGE);
                System.exit(0);
            }else{
                synchronized(lock){
                    lock.notifyAll();
                }
            }
        }
        
    }
    
    private static PlayerListener stateListener;
    
    public static void main(String[] args){
        
        if(args.length<3){
            System.out.println("Usage: DefaultPlayer [MainClassName] [Frame Object Name] [screenshotDirectory] [ [App Arg1] [App Arg2] [...]]");
            System.exit(0);
        }          
        
       try{           
           File screenshotsDir = new File(args[2]);
           if(!screenshotsDir.exists()){
               screenshotsDir.mkdirs();
           }
           
           stateListener = new PlayerStateListener(args[2]);
           
           final DefaultScenarioPlayer pl = new DefaultScenarioPlayer(args[0], 
                                                                      args[1], 
                                                                      args[2]);
           
           pl.launch(args);
           boolean ready = waitForPlayerToBeReady(pl);
           
           pl.setRealTimeMode(true);
           
           JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
           chooser.setDialogTitle("Choose the scenario to play...");
           chooser.setMultiSelectionEnabled(true);
           chooser.showOpenDialog(pl.getClientWindow());

           File[] files = chooser.getSelectedFiles();
           System.out.println(" files : " + Arrays.asList(files));
           
           pl.addExitKeysSequence();

           pl.addStateListener(stateListener);

           for(int i=0; i<files.length; i++){
               Object playerLock = ((PlayerStateListener)stateListener).getPlayerLock();
               synchronized(playerLock){
                   File file = files[i];
                   LOGGER.info(" file : " + file);
                   byte[] fileBytes = new byte[(int)file.length()];
                   FileInputStream in = new FileInputStream(file);
                   in.read(fileBytes);
                   in.close();

                   if(ready){
                       pl.startScenario(fileBytes, true);
                       ((PlayerStateListener)stateListener).setLastScenario(i==(files.length-1));
                       if(i!=(files.length-1)){
                           playerLock.wait();
                       }
                   }else{
                       LOGGER.warning("Unable to play because we're unable to reach a ready application to test");
                       System.exit(1);
                   }
               }
           }

       }catch(Exception e){
           e.printStackTrace();
       }
    }
   
    public static boolean waitForPlayerToBeReady(Player localPlayer) throws FrameNotFoundException{
        long totalWaitingTime = 0;
        while(!localPlayer.isReadyToBeTested()){
            totalWaitingTime += TIME_TO_WAIT;
            if(totalWaitingTime > 30000){
                return false;
            }
            try{
                LOGGER.info("Waiting for app to be ready to test: " + localPlayer.getHost() + ":" + localPlayer.getJmxPort());
                Thread.sleep(TIME_TO_WAIT);
            }catch(Exception e){
                e.printStackTrace();
            }

        }
        LOGGER.info("App is ready to test : " + localPlayer.getHost() + ":" + localPlayer.getJmxPort());
        return true;
    }    
    

    
}
