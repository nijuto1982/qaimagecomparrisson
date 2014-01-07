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
import org.jdiffchaser.publish.HtmlReporter;
import org.jdiffchaser.publish.ReportException;
import org.jdiffchaser.scenarihandling.TestSetResult;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.jdiffchaser.conf.GuiTest;
import org.jdiffchaser.conf.TestConfiguration;
import org.jdiffchaser.conf.TestHost;
import org.jdiffchaser.conf.TestSuite;

public class Controller {
    
    private static final Logger LOGGER = Logger.getLogger(Controller.class.getName());    
    
    public Controller() {
    }
       
    private boolean deleteDirContent(File dir){
        boolean r = true;
        if( dir.exists() ){
            File[] files = dir.listFiles();
            for(int i=0;i<files.length && r;i++){
                if( files[i].isDirectory() ){
                    r = deleteDirContent(files[i]);
                }
                if( !files[i].delete() ){
                    LOGGER.warning("Unable to delete : " + files[i]);
                    r = false;
                }
            }
        }
        return r;
    }
    
    public void launchFromFile(String filePath) throws FileNotFoundException,
                                                       IOException,
                                                       InterruptedException,
                                                       MarshalException,
                                                       ValidationException,
                                                       ReportException{
  
        FileReader fileReader = new FileReader(filePath);
        TestConfiguration config = (TestConfiguration)Unmarshaller.unmarshal(
                                                        org.jdiffchaser.conf.TestConfiguration.class,
                                                        fileReader);
        fileReader.close();
        
        File failedDir = new File(config.getFailedBaseDirectory());
        deleteDirContent(failedDir);
        
        TestHost firstHost  = config.getFirstHost();
        TestHost secondHost = config.getSecondHost();
        
        int totalTestCount       = 0;
        int totalTestPlayed      = 0;
        int totalTestSucceded    = 0;
        String firstHostComment  = null;
        String secondHostComment = null;
        
        for(int k=0; k<config.getTestSuiteCount(); k++){
            TestSuite testSuite = config.getTestSuite(k);
            for(int i=0; i<testSuite.getGuiTestCount(); i++){
                
                GuiTest regressionTest = testSuite.getGuiTest(i);

                if(testSuite.getCompLayersDirectory()!=null
                   && regressionTest.getCompLayersDirectory()==null){
                   regressionTest.setCompLayersDirectory(testSuite.getCompLayersDirectory());
                }
                
                if(testSuite.hasRealtimeMode()
                   && !regressionTest.hasRealtimeMode()){
                   regressionTest.setRealtimeMode(testSuite.getRealtimeMode());
                }
                
                LOGGER.info("***********************************************************************");
                LOGGER.info("**** launching regression tests for " + regressionTest.getScenariiDirectory());
                LOGGER.info("***********************************************************************");
                String scenariiDirectory = config.getScenariiBaseDirectory() 
                                           + regressionTest.getScenariiDirectory() + File.separator;
                LOGGER.info("*** Scenarii dir is: " + scenariiDirectory);
                
                String setupDirectory = testSuite.getSetupScenariiDirectory()!=null?
                                        config.getScenariiBaseDirectory() + testSuite.getSetupScenariiDirectory():null;

                String teardownDirectory = testSuite.getTeardownScenariiDirectory()!=null?
                                        config.getScenariiBaseDirectory() + testSuite.getTeardownScenariiDirectory():null;
                                
                ScenariiDirectories scenariiDirectories = new ScenariiDirectories(config.getScenariiBaseDirectory(),
                                                                                  setupDirectory, 
                                                                                  teardownDirectory, 
                                                                                  scenariiDirectory);
                
                boolean noMoreTestToDo = (k==(config.getTestSuiteCount()-1)) && (i==(testSuite.getGuiTestCount()-1));
                
                AppVersion oldAppVersion = new AppVersion(firstHost.getIp(), firstHost.getPort(), firstHost.getArg());
                AppVersion newAppVersion = null;
                if(secondHost.getIp()!=null){ //only for debugging prupose (test remote launching)
                    newAppVersion = new AppVersion(secondHost.getIp(), secondHost.getPort(), secondHost.getArg());
                }else{
                    LOGGER.warning("Second host not specified: will only launch and play scenario"
                                   + " on the first one without doing any image comparison");
                }
                
                TestSetResult result = RemoteTestLauncher.launch(oldAppVersion,
                                                                 newAppVersion,
                                                                 regressionTest,
                                                                 scenariiDirectories,
                                                                 config.getFailedBaseDirectory(),
                                                                 testSuite.hasParallelMode()?testSuite.getParallelMode():false,
                                                                 noMoreTestToDo?false:true);
                
                
                totalTestCount    += result.getTotalTestCount();
                totalTestPlayed   += result.getTotalTestPlayed();
                totalTestSucceded += result.getTotalTestSucceded();
                
                //tbd: choose a better way to store the hosts comments
                if(firstHostComment==null){
                    firstHostComment  = result.getFirstHostComment();
                }
                if(secondHostComment==null){
                    secondHostComment = result.getSecondHostComment();
                }
            }
        }
    
        HtmlReporter htmlReporter = new HtmlReporter(config.getFailedBaseDirectory(),
                                                     firstHostComment,
                                                     secondHostComment,
                                                     totalTestCount,
                                                     totalTestPlayed,
                                                     totalTestSucceded);
        htmlReporter.createReport(config.getReportTitle());
        
    }
    
    public static void main(String [] args){
        try{
            
            if(args.length!=1){
                System.out.println("Usage: Controller <fileName>");
                return;
            }
            Controller clientLauncher = new Controller();
            clientLauncher.launchFromFile(args[0]);
                    
        }catch(Throwable t){
            t.printStackTrace();
        }
        System.exit(0);
    }
    
}
