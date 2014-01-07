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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.io.FilenameFilter;
import org.jdiffchaser.imgprocessing.ImageComparator;
import org.jdiffchaser.scenarihandling.events.RecordableEvent;
import org.jdiffchaser.scenarihandling.events.StartEvent;
import org.jdiffchaser.utils.ImageUtilities;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.management.InstanceNotFoundException;
import org.jdiffchaser.gui.FrameNotFoundException;
import org.jdiffchaser.imgprocessing.IgnoredBgPattern;
import org.jdiffchaser.scenarihandling.events.ComparisonEvent;


public class TestSet {
    
    private static final Logger LOGGER = Logger.getLogger(TestSet.class.getName());
    
    private static final GraphicsEnvironment LOCENV = GraphicsEnvironment.getLocalGraphicsEnvironment();
    private static final GraphicsConfiguration GCONF = LOCENV.getDefaultScreenDevice().getDefaultConfiguration();    
    
    //to avoid creating cache files during InputStream image reading
    //(the cache creation seems to have some problems depending the logins, platforms...)
    static{
        ImageIO.setUseCache(false); 
    }
    
    private PlayerHandle        remotePlayer1Handle;
    private PlayerHandle        remotePlayer2Handle;
    private ScenariiDirectories scenariDirectories;
    private String              zonesToIgnoreDirectory;
    private String              resultDirectory;
    private long                timeOutReady;
    private static final int    TIME_TO_WAIT = 1000;
    
    public static final String IMG_EXTENSION = "png";
    public static final String DIFF_IMG_NAME = "diffs";
    
    
    public TestSet(PlayerHandle         remotePlayer1Handle,
                   PlayerHandle         remotePlayer2Handle,
                   ScenariiDirectories  scenariDirectories,
                   String               zonesToIgnoreDirectory,
                   String               resultDirectory,
                   long                 timeOutReady) {
        
        this.remotePlayer1Handle    = remotePlayer1Handle;
        this.remotePlayer2Handle    = remotePlayer2Handle;
        this.scenariDirectories     = scenariDirectories;
        this.zonesToIgnoreDirectory = zonesToIgnoreDirectory;
        this.resultDirectory        = resultDirectory;
        this.timeOutReady           = timeOutReady;
    }

    public void dispose(){
        this.remotePlayer1Handle.close();
        if(this.remotePlayer2Handle!=null){
            this.remotePlayer2Handle.close();
        }
    }
    
    private byte[] getBytesArrayFromFile(File scenario) throws IOException, FileNotFoundException{
        byte[] fileBytes = new byte[(int)scenario.length()];
        FileInputStream in = null;
        try{
            in =new FileInputStream(scenario);
            in.read(fileBytes);
        }finally{
            in.close();
        }
        return fileBytes;
    }
    
    private StartEvent getStartEventFromScenario(File scenario) throws IOException, 
                                                                       FileNotFoundException,
                                                                       ClassNotFoundException{
        byte[] fileBytes = new byte[(int)scenario.length()];
        FileInputStream in = null;
        ObjectInputStream inobj = null;
        StartEvent startEvent = null;
        try{
            in = new FileInputStream(scenario);
            inobj = new ObjectInputStream(in);
            Object obj = inobj.readObject();
            startEvent = (StartEvent) ((RecordableEvent) obj).getEvent();
        }finally{
            in.close();
            inobj.close();
        }
        return startEvent;
    }
    
    private boolean waitForPlayerToBeReady(PlayerHandle remotePlayer) throws FrameNotFoundException{
        long totalWaitingTime = 0;
        while(!remotePlayer.getProxy().isReadyToBeTested()){
            totalWaitingTime += TIME_TO_WAIT;
            if(totalWaitingTime > timeOutReady){
                return false;
            }
            try{
                LOGGER.info("Waiting for " + remotePlayer.getIP() + ":" + remotePlayer.getPort() + " to be ready");
                Thread.sleep(TIME_TO_WAIT);
            }catch(Exception e){
                e.printStackTrace();
            }

        }
        return true;
    }

    private File[] buildScenariiList(String setupDir, String mainDir, String teardownDir){
        List files = new ArrayList();
        
        addAllFromDir(SCENARIOS_FILTER, setupDir, files);
        addAllFromDir(SCENARIOS_FILTER, mainDir, files);
        addAllFromDir(SCENARIOS_FILTER, teardownDir, files);
        
        return  (File[]) files.toArray(new File[0]);
    }
    
    private FilenameFilter SCENARIOS_FILTER = new FilenameFilter() {
        public boolean accept(File dir, String name) {
            return name.endsWith(".sc");
        }
    };
    
     private FilenameFilter LAYERS_FILTER = new FilenameFilter() {
        public boolean accept(File dir, String name) {
            return name.endsWith(".ign");
        }
    };   
    
    private void addAllFromDir(FilenameFilter filter, String dir, List list){
        if(dir!=null){
            File dirFile = new File(dir);
            if(dirFile!=null && dirFile.listFiles(filter)!=null){
                list.addAll(Arrays.asList(dirFile.listFiles(filter)));
            }else{
                LOGGER.warning("Unable to load files from dir " + dirFile);
            }
        }        
    }
    
    private boolean tryToContact(int times, PlayerHandle handle) throws IOException,
                                                                        InstanceNotFoundException,
                                                                        ClassNotFoundException,
                                                                        InterruptedException{
        int occurences = times;
        boolean contactEstablished = false;
        while(occurences>0 && !contactEstablished){
            contactEstablished = handle.contact();
            if(contactEstablished){
                break;
            }else{
                occurences--;
                Thread.sleep(1000);
            }
        }
        return contactEstablished;
    }
    
    public TestSetResult play(boolean parallelMode) throws StartException{
        String scenarioFileName = null;
        TestSetResult result = null;
        try{
            
            File[] scenariFiles = buildScenariiList(this.scenariDirectories.getSetupDirectory(), 
                                                    this.scenariDirectories.getScenariiDirectory(), 
                                                    this.scenariDirectories.getTearDownDirectory());
            result = new TestSetResult(scenariFiles.length);
            
            for(int i=0; i<scenariFiles.length; i++){
                
                scenarioFileName = scenariFiles[i].getName();
                
                if(scenarioFileName.startsWith(".")){ //avoid invisible files like .svn ones
                    continue;
                }
                
                byte[] scenario = getBytesArrayFromFile(scenariFiles[i]);
                StartEvent scenarioStartEvent = getStartEventFromScenario(scenariFiles[i]);
                       
                tryToContact(10, this.remotePlayer1Handle);
                result.setFirstHostComment(this.remotePlayer1Handle.getIP() + ":"
                                           + this.remotePlayer1Handle.getPort() + " running " 
                                           + this.remotePlayer1Handle.getProxy().getVersion());
                if(this.remotePlayer2Handle!=null){
                    tryToContact(10, this.remotePlayer2Handle);
                    result.setSecondHostComment(this.remotePlayer2Handle.getIP() + ":" 
                                               + this.remotePlayer2Handle.getPort() + " running " 
                                               + this.remotePlayer2Handle.getProxy().getVersion());
                }
                
                boolean player1FullInput = true;
                boolean player2FullInput = true;                 
                if(scenarioStartEvent.isInputScenario()){
                    player1FullInput = true;
                    player2FullInput = false;                    
                }
                
                boolean player1Ready = waitForPlayerToBeReady(remotePlayer1Handle);
                boolean player2Ready = false;

                if(remotePlayer2Handle!=null){
                    player2Ready = waitForPlayerToBeReady(remotePlayer2Handle);
                }
                
                if(parallelMode){

                    LOGGER.info("All tests hosts are ready, let's play the scenario...");

                    if(   (remotePlayer2Handle!=null && player1Ready && player2Ready) 
                        || (remotePlayer2Handle==null && player1Ready) ){

                        this.remotePlayer1Handle.getProxy().startScenario(scenario, player1FullInput);
                        if(this.remotePlayer2Handle!=null){
                            this.remotePlayer2Handle.getProxy().startScenario(scenario, player2FullInput);
                        }

                        BufferedImage image1 = ImageUtilities.readImageFromBytes(
                                                                this.remotePlayer1Handle.getProxy().waitForScenarioEnd());
                        BufferedImage image2 = null;
                        if(this.remotePlayer2Handle!=null){
                            image2 = ImageUtilities.readImageFromBytes(
                                                                this.remotePlayer2Handle.getProxy().waitForScenarioEnd());
                        }
                        if(this.remotePlayer2Handle!=null){
                            reportComparison(scenarioStartEvent, scenarioFileName, 
                                             image1, 
                                             getComparisonLabel(remotePlayer1Handle), 
                                             image2, 
                                             getComparisonLabel(remotePlayer2Handle), 
                                             result);
                        }
                    }else{
                        LOGGER.warning("One of the test actors isn't ready, aborting test...");
                    }
                }else{ //sequential mode
                    BufferedImage image1 = playSingle(remotePlayer1Handle, scenario, player1FullInput);
                    String label1 = getComparisonLabel(remotePlayer1Handle);
                    //TestSet.closeHandle(remotePlayer1Handle, "First version", 2000);
                    BufferedImage image2 = playSingle(remotePlayer2Handle, scenario, player2FullInput);
                    String label2 = getComparisonLabel(remotePlayer2Handle);
                    //TestSet.closeHandle(remotePlayer2Handle, "Second version", 2000);
                    reportComparison(scenarioStartEvent, scenarioFileName, image1, label1,
                                                                           image2, label2, result);
                }
            }
        }catch(Exception e){
            throw new StartException("Unable to play the scenario '" + scenarioFileName + "'", e);
        }
        return result;
    }
    
    private BufferedImage applyIgnoredLayer(ComparisonEvent compEvent, BufferedImage image){
        BufferedImage buffImg = IgnoredBgPattern.getDisplayedImageWithBg(image);

        Graphics2D g2d = (Graphics2D) buffImg.getGraphics();
        List ignoreZones = compEvent.getIgnoredZones();
        for(Iterator ite = ignoreZones.iterator(); ite.hasNext();){
            Rectangle ign = (Rectangle) ite.next();
            g2d.setBackground(new Color(0,0,0,0));
            g2d.clearRect( ign.x, ign.y, ign.width, ign.height);
            LOGGER.fine("Clearing layer rectangle (" + ign.x + ", " + ign.y + ", " 
                        + ign.width + ", " + ign.height + ")");
        }
        g2d.dispose();    
        return IgnoredBgPattern.getDisplayedImageWithBg(buffImg);
    }
    
    private BufferedImage applyIgnoredLayers(List layersFiles, BufferedImage image){
        BufferedImage alteredImg = image;
        ObjectInputStream ois = null;
        for(Iterator ite=layersFiles.iterator(); ite.hasNext();){
            File file = (File) ite.next();
            LOGGER.info("Will apply supplementary zones to ignore from file: " + file);
            try{
                ois = new ObjectInputStream(new FileInputStream(file));
                ComparisonEvent comparisonEvent = (ComparisonEvent) ois.readObject();
                alteredImg = applyIgnoredLayer(comparisonEvent, alteredImg);
            }catch(IOException ioe){
                LOGGER.log(Level.SEVERE, "Unable to load comparison event file " + file.getName(), ioe);
            }catch(ClassNotFoundException cnfe){
                LOGGER.log(Level.SEVERE, "Unable to load comparison event file " + file.getName(), cnfe);
            }finally{
                try{
                    if(ois !=null ){
                        ois.close();
                    }
                }catch(IOException ioe){
                    LOGGER.log(Level.SEVERE, "Unable to close layer file " + file.getName(), ioe);
                }
            }
        }
        return alteredImg;
    }
    
    private void reportComparison(StartEvent scenarioStartEvent, 
                                  String scenarioFileName,
                                  BufferedImage image1, 
                                  String label1,
                                  BufferedImage image2,
                                  String label2,
                                  TestSetResult result) throws PlayerException{
        //launch regression test comparison between the two clients
        String scenarioName = scenarioStartEvent.getScenarioName();
        //todo: apply common zones to ignore files to both images before comparing
        //here
        List layersFiles = new ArrayList();
        addAllFromDir(LAYERS_FILTER, 
                      this.scenariDirectories.getScenariiBaseDirectory() 
                      + File.separator +  this.zonesToIgnoreDirectory, 
                      layersFiles);
        LOGGER.info("Will apply supplementary zones to ignore from following files list: " + layersFiles);
        BufferedImage image1Bis = applyIgnoredLayers(layersFiles, image1);
        BufferedImage image2Bis = applyIgnoredLayers(layersFiles, image2);
        
        boolean ok = compareTwoScreenshots(scenarioFileName, scenarioName, image1Bis, label1,
                                                                           image2Bis, label2);
        LOGGER.info("COMPARISON IS " + ok);
        if(ok){
            result.incrementTestSucceded();
        }
        result.incrementTestPlayed();
    }
    
    private BufferedImage playSingle(PlayerHandle remotePlayerHandle, byte[] scenario, boolean fullInput) 
      throws IOException, InterruptedException, FrameNotFoundException{
        waitForPlayerToBeReady(remotePlayerHandle);
        remotePlayerHandle.getProxy().startScenario(scenario, fullInput);
        BufferedImage image = ImageUtilities.readImageFromBytes(remotePlayerHandle.getProxy().waitForScenarioEnd());
        return image;
    }

    
    private String getScenarioName(byte[] scenario) throws IOException, ClassNotFoundException{
        ByteArrayInputStream bin = null;
        ObjectInputStream oin    = null;
        String scenarioName = null;
        try{
            bin = new ByteArrayInputStream(scenario);
            oin = new ObjectInputStream(bin);
            RecordableEvent readEvent = (RecordableEvent)oin.readObject();
            while(readEvent!=null && !(readEvent.getEvent() instanceof StartEvent)){
                readEvent = (RecordableEvent)oin.readObject();
            }
            if(readEvent.getEvent() instanceof StartEvent){
                scenarioName = ((StartEvent)readEvent.getEvent()).getScenarioName();
            }
        }finally{
            bin.close();
            oin.close();
        }
        return scenarioName;
    }
    
    private String getComparisonLabel(PlayerHandle playerHandle){
        return "From" + playerHandle.getIP() + "(" + playerHandle.getProxy().getVersion() + ")";
    }
    
    private boolean compareTwoScreenshots(String scenarioFileName, String scenarioName, 
                                          BufferedImage image1, String label1,
                                          BufferedImage image2, String label2) throws PlayerException{
        boolean matchToReturn = false;
        double ratio = 1.0;
        String ratioProp = System.getProperty("animation.ratio");
        if(ratioProp!=null){
            ratio = Double.parseDouble(ratioProp);
            LOGGER.info("Animation will be played with " + ratio + " ratio...");
        }
        
        ImageComparator imgComp = ImageComparator.getInstance(ratio,
                                                              image1,
                                                              label1,
                                                              image2,
                                                              label2);
        try{
            
            imgComp.compareImages(!Boolean.getBoolean("skip.animation"));
            Thread.sleep(2000); //to let the user see the result dialog

            matchToReturn = imgComp.getLastComparisonMatched();
            if(!matchToReturn){
                String scDirPath  = this.resultDirectory + '/' 
                                    + getLastElementFromPath(this.scenariDirectories.getScenariiDirectory()) 
                                    + scenarioFileName + "(" + scenarioName + ")";
                removeIfItExists(scDirPath);
                String file1Path  = scDirPath + '/' 
                                    + remotePlayer1Handle.getIP().replace('.', '_') + "_1." + IMG_EXTENSION;
                String file2Path  = scDirPath + '/' 
                                    + remotePlayer2Handle.getIP().replace('.', '_') + "_2." + IMG_EXTENSION;
                String resultPath = scDirPath + '/' 
                                    + DIFF_IMG_NAME + '.' + IMG_EXTENSION;
                storeMismatchResult(image1, file1Path,
                                    image2, file2Path,
                                    imgComp.getLastResultImage(), resultPath);
            }
            imgComp.clean();
        }catch(Exception e){
            throw new PlayerException("Unable to compare the two screenshots ' " + remotePlayer1Handle.getIP() 
                                      + "' and '" + remotePlayer2Handle.getIP() + "' for '" + scenarioName + '\'' , e);
        }
        return matchToReturn;
    }
    
    private void removeIfItExists(String dir){
        File file = new File(dir);
        if(file.exists()){
            file.delete();
        }
    }

    private void storeMismatchResult(BufferedImage image1,   String image1Path,
                                     BufferedImage image2,   String image2Path,
                                     BufferedImage diffsImg, String resultPath)  throws IOException{
        
        ImageUtilities.storeImage(image1, image1Path);
        ImageUtilities.storeImage(image2, image2Path);
        ImageUtilities.storeImage(diffsImg, resultPath);
        
    }
    
    /**
     * @returns 'accarfa' for '../testdata/scenarii/accarfa'
     */
    private static String getLastElementFromPath(String path){
        String toRet = path.replace('\\', '/');
        if(toRet.lastIndexOf('/')==toRet.length()-1){
            toRet = path.substring(0, toRet.length()-1);
        }
        toRet = toRet.substring(toRet.lastIndexOf('/')+1);
        LOGGER.fine("returning last element '" + toRet + "' for path " + path);
        return toRet + '/';
    }
    
    public static void closeHandle(PlayerHandle handle, String systemName, long timeBeforeExit, boolean withRestart){
        try{
            if(handle.getProxy()!=null){
                handle.getProxy().exit(timeBeforeExit, withRestart);
                handle.close();
                LOGGER.info(systemName + " will exit");
            }
        }catch(Throwable t){
            //we need to catch throwables because the remote exit can cause a JMX Disconnection
            LOGGER.info(systemName + " will exit");
        }        
    }    
      
}
