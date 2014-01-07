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

import org.jdiffchaser.scenarihandling.ScenariiDirectories;
import org.jdiffchaser.conf.ArgType;
import org.jdiffchaser.conf.GuiTest;
import org.jdiffchaser.scenarihandling.PlayerHandle;
import org.jdiffchaser.scenarihandling.TestSet;
import org.jdiffchaser.scenarihandling.TestSetResult;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RemoteTestLauncher {
    
    private static final Logger LOGGER = Logger.getLogger(RemoteTestLauncher.class.getName());        
    
    private static final int  NB_RETRY    = 6;
    private static final long RETRY_PAUSE = 5000;
        
    public static TestSetResult launch( AppVersion oldAppVersion,
                                        AppVersion newAppVersion,
                                        GuiTest guiTest,
                                        ScenariiDirectories scenariiDirectories,
                                        String failedDir,
                                        boolean parallelMode,
                                        boolean restartPlayers) {
        
                
        TestSetResult result = null;
        ArgType[] args = guiTest.getArg();
        
        boolean realtime = true;
        if(guiTest.hasRealtimeMode()){
            realtime = guiTest.getRealtimeMode();
        }
        
        try{
            PlayerHandle playerHandle1 = new PlayerHandle(oldAppVersion.getHost(), oldAppVersion.getPort());
        
            PlayerHandle playerHandle2 = null;
            if(newAppVersion!=null){
                playerHandle2 = new PlayerHandle(newAppVersion.getHost(), newAppVersion.getPort());
            }
            
            tryToContact(playerHandle1);
            playerHandle1.getProxy().setRealTimeMode(realtime);
            
            if(playerHandle2!=null){
                tryToContact(playerHandle2);
                playerHandle2.getProxy().setRealTimeMode(realtime);
            }
            
            Map argsMap1 = new Hashtable();
            updateArgsMap(argsMap1, oldAppVersion.getArgs());
            updateArgsMap(argsMap1, args);
            
            Map argsMap2 = new Hashtable();
            updateArgsMap(argsMap2, newAppVersion.getArgs());            
            updateArgsMap(argsMap2, args);
            
            String[] specArgs1 = createArgsArray(argsMap1);
            String[] specArgs2 = createArgsArray(argsMap2);
            
            LOGGER.fine("Launching FIRST remote application with args : " + Arrays.asList(specArgs1));
            playerHandle1.getProxy().launch(specArgs1);
                                
            if(playerHandle2!=null){    
                LOGGER.fine("Launching SECOND remote application with args : " + Arrays.asList(specArgs2));
                
                playerHandle2.getProxy().launch(specArgs2);
            }
                                                         
            TestSet testSet = new TestSet(playerHandle1,
                                          playerHandle2,
                                          scenariiDirectories,
                                          guiTest.getCompLayersDirectory(),
                                          failedDir,
                                          30000);
            
            result = testSet.play(parallelMode);
            
            long timeBeforeExit = 5000;
            TestSet.closeHandle(playerHandle1, "First system", timeBeforeExit, restartPlayers);
                
            if(playerHandle2!=null){
                TestSet.closeHandle(playerHandle2, "Second system", timeBeforeExit, restartPlayers);
            }
            
            try{
                Thread.sleep(4*timeBeforeExit);
            }catch(InterruptedException ie){
                //ignore
            }

            LOGGER.fine("End of TestSet");
        }catch(Throwable t){
            t.printStackTrace();
        }

        return result;
    }
    
    private static void createRemoteArgs(String[] remoteArgs, String[] specArgs, String[] commonArgs){
        System.arraycopy(specArgs, 0, remoteArgs, 0, specArgs.length);
        System.arraycopy(commonArgs, 0, remoteArgs, specArgs.length, commonArgs.length);    
    }
    
    private static void updateArgsMap(Map argsMap, ArgType[] args){
        for(int i=0; i<args.length; i++){
            ArgType arg = args[i];
            if(argsMap.containsKey(arg.getName())){
                //if arg already exist, we append the new one
                String existingArg = (String)argsMap.get(arg.getName());
                argsMap.put(arg.getName(), existingArg + arg.getContent());
            }else{
                argsMap.put(arg.getName(), arg.getContent());
            }
        }
    }
    
    private static String[] createArgsArray(Map argsMap){
        List argsArr = new ArrayList();
        for(Iterator ite = argsMap.values().iterator(); ite.hasNext(); ){
            String argLine = (String) ite.next();
            StringTokenizer st = new StringTokenizer(argLine);
            while(st.hasMoreTokens()){
                argsArr.add(st.nextToken());
            }
        }
        return (String[]) argsArr.toArray(new String[argsArr.size()]);
    }
    
    private static void tryToContact(PlayerHandle playerHandle){
        boolean connected   = false;
        int connectionTries = NB_RETRY; 
        while(connectionTries>0 && !connected){
            try{
                connected = playerHandle.contact();
                if(!connected){
                    LOGGER.warning("PlayerHandle still not connected, nb of leaving retries: " + connectionTries);
                    connectionTries--;
                    Thread.sleep(RETRY_PAUSE);
                }
            }catch(Exception e){
                LOGGER.log(Level.WARNING, 
                           "Unable to contact playerHandle, nb of leaving retries: " + connectionTries,
                           e);
                connectionTries--;
                try{
                    Thread.sleep(RETRY_PAUSE);
                }catch(Exception exc){
                    //
                }
            }
        }        
    }
    
}
