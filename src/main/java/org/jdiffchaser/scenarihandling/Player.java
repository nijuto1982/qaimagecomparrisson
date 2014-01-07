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
import org.jdiffchaser.scenarihandling.events.ComparisonEvent;
import org.jdiffchaser.scenarihandling.events.WindowMoveEvent;
import org.jdiffchaser.scenarihandling.events.RecordableEvent;
import org.jdiffchaser.imgprocessing.IgnoredBgPattern;
import org.jdiffchaser.imgprocessing.Screenshot;
import org.jdiffchaser.imgprocessing.ScreenshotException;
import org.jdiffchaser.scenarihandling.events.InputModeEvent;
import org.jdiffchaser.scenarihandling.events.StartEvent;
import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import org.jdiffchaser.gui.FrameFacilities;
import org.jdiffchaser.gui.FrameNotFoundException;
import org.jdiffchaser.utils.ImageUtilities;


public abstract class Player implements PlayerMBean{
    
    private static final Logger LOGGER = Logger.getLogger(Player.class.getName());
    
    private List                stateListeners = new Vector();
    
    protected String            windowObjectName;
    protected Frame             clientWindow;
    private Robot               robot;
    private ObjectInputStream   in;
    private String              screenshotsDirectory;
    
    public  static final String IMG_FORMAT = "png";
    private String              scenarioName; 
    private boolean             fullInput;
    private String              hostName = "<unknown hostname>";
    private int                 jmxPort  = -1;    
    private Throwable           playException;
    
    private byte[]              lastScreenshotBytes;
    private boolean             realtimeMode = true;
    
    /**
     * is the fullInput mode currently on
     */
    private boolean             currentActionsPlayed;
    
    /**
     * The class that is loaded using reflection
     * This class contains the main method to be launch by the player
     */
    protected Class             mainClass;
    
    private Object              playLock = new Object();
        
    /**
     * Do the player should restart itself after exiting in order to play incoming scenarios
     */
    private static boolean      WITH_RESTART;
    /**
     * The fake transparent cursor: we use this one to make cursor invisible during a screenshot
     */
    private static final Cursor HIDDEN_CURSOR =
            Toolkit.getDefaultToolkit().createCustomCursor(
                    Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(16, 16, new int[16 * 16], 0, 16)),
                    new Point(0, 0),
                    "HIDDEN_CURSOR");
    
    private static final Cursor DEFAULT_CURSOR = new Cursor(Cursor.DEFAULT_CURSOR);
    
    public Player(String fullClassName, String windowObjectName, String screenshotsDirectory) throws PlayerException{
    
        try{
            this.mainClass = Class.forName(fullClassName);
        }catch(ClassNotFoundException cnfe){
            throw new PlayerException("Unable to load the main class to test", cnfe);
        }
        
        if(!isValidClassToTest(this.mainClass)){
            throw new PlayerException("Unable to use the main class loaded, not valid,"
                                      + " check if it contains the mandatory methods:  main and getFrame");
        }
        this.windowObjectName = windowObjectName;

        try{
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice[] gd = ge.getScreenDevices();            
            this.robot = new Robot(gd[0]);
            this.robot.delay(50);
        }catch(AWTException awte){
            throw new PlayerException("Unable to create the Robot : " + awte.getMessage());
        }
        this.screenshotsDirectory = screenshotsDirectory;
    }

    public Player(String fullClassName, String windowObjectName, String hostName, 
                  int jmxPort, String screenshotsDirectory) throws PlayerException{
        this(fullClassName, windowObjectName, screenshotsDirectory);
        this.hostName = hostName;
        this.jmxPort  = jmxPort;
    }

    public void setInputScenario(byte[] scenario) throws PlayerException{
        try{
            this.in = new ObjectInputStream(new ByteArrayInputStream(scenario));
        }catch(Exception e){
            throw new PlayerException("Unable to load Robot scenario : " + e.getMessage());
        }        
    }
    
    private boolean isValidClassToTest(Class clazz){
//        Method[] methods = clazz.getDeclaredMethods();
//        return methodExists("main", methods) && methodExists("getFrame", methods);
          return true;
    }   
    
    private boolean methodExists(String methodName, Method[] methods){
        for(int i=0; i<methods.length; i++){
            Method method = methods[i];
            if(method.getName().equals(methodName)){
                return true;
            }
        }
        return false;
    }
    
    public void play() throws PlayerException{
        //three lines below to be sure to be able to take some screenshots
        this.clientWindow.toFront(); 
        try{Thread.sleep(250);}catch(Exception e){}
        this.clientWindow.setExtendedState(Frame.ICONIFIED);
        try{Thread.sleep(250);}catch(Exception e){}
        this.clientWindow.setExtendedState(Frame.NORMAL);
        try{Thread.sleep(250);}catch(Exception e){}
        this.clientWindow.toFront();
        notifyStartedListeners();        
        RecordableEvent recEvent = null;
        try{
            Object obj = this.in.readObject();
            while( obj!=null ){
                recEvent = (RecordableEvent) obj;
                handleEvent(recEvent);
                try{
                    obj = this.in.readObject();
                }catch(EOFException eofe){
                    obj = null;
                }
            }
        }catch(Exception e){
            notifyAbortedListeners(e);        
            throw new PlayerException("Unable to well play scenario : " + e.getMessage() 
                                      + ", recEvent was : " + recEvent.getEvent().toString(), e);
        }finally{
            this.clientWindow.setExtendedState(Frame.ICONIFIED);
            notifyTerminatedListeners();
        }
    }
    
    private void handleEvent(RecordableEvent recEvent) throws PlayerException{
        try{
            if(this.realtimeMode){
                Thread.sleep(recEvent.getTimeElapsedBeforeEvent());
            }
        }catch(InterruptedException ie){
            ie.printStackTrace();
        }
        if (recEvent.getEvent() instanceof StartEvent){
            LOGGER.info("start event recognized");
            scenarioName        = ((StartEvent)recEvent.getEvent()).getScenarioName();
        }else if ( (this.fullInput || this.currentActionsPlayed) && recEvent.getEvent() instanceof MouseEvent){
            handleMouseEvent((MouseEvent) recEvent.getEvent());
            
        }else if ( (this.fullInput || this.currentActionsPlayed) && recEvent.getEvent() instanceof KeyEvent){
            handleKeyEvent((KeyEvent) recEvent.getEvent());

        }else if (recEvent.getEvent() instanceof ComparisonEvent){
            handleComparisonEvent((ComparisonEvent) recEvent.getEvent());
            
        }else if (recEvent.getEvent() instanceof InputModeEvent){
            handleInputModeEvent((InputModeEvent) recEvent.getEvent());
        
        }else if (recEvent.getEvent() instanceof WindowMoveEvent){
            handleFrameMoveEvent((WindowMoveEvent) recEvent.getEvent());
        }else{
            LOGGER.warning("Event to reproduce not recognized, RecordableEvent is " + recEvent);
        }
    }
    
    private int getRobotButtonMask(int mouseButton){
        switch(mouseButton){
            case MouseEvent.BUTTON1 : return InputEvent.BUTTON1_MASK;
            case MouseEvent.BUTTON2 : return InputEvent.BUTTON2_MASK;
            case MouseEvent.BUTTON3 : return InputEvent.BUTTON3_MASK;
        }
        return -1;
    }
    
    private void handleKeyEvent(KeyEvent ke){
        if(ke.getKeyCode() == KeyEvent.VK_UNDEFINED){
            if(ke.getID() != KeyEvent.KEY_TYPED){
                LOGGER.warning("UNKNOWN Keycode used! Unable to Reproduce this KeyEvent: " + ke);
            }
        }else{
            if(ke.getID() == KeyEvent.KEY_PRESSED){
                robot.keyPress(ke.getKeyCode());
            }

            if(ke.getID() == KeyEvent.KEY_RELEASED){
                robot.keyRelease(ke.getKeyCode());
            }
        }
    }
    
    private void handleMouseEvent(MouseEvent me){
                
        if(me.getID() == MouseEvent.MOUSE_MOVED
           || me.getID() == MouseEvent.MOUSE_DRAGGED){
            robot.mouseMove(me.getX(), me.getY());
        }
        
        if(me.getID() == MouseEvent.MOUSE_PRESSED){
            robot.mousePress(getRobotButtonMask(me.getButton()));
        }
        
        if(me.getID() == MouseEvent.MOUSE_RELEASED){
            robot.mouseRelease(getRobotButtonMask(me.getButton()));
        }
        
    }
    
    private void handleComparisonEvent(ComparisonEvent comparisonEvent) throws PlayerException{
        LOGGER.info("WAIT... Screenshot about to be done");
        hideCursor();
        try{
            BufferedImage screenshot = Screenshot.createScreenCapture(this.clientWindow, 
                                                                      comparisonEvent.isFullScreen());
            drawIgnoredZones(screenshot, comparisonEvent);

            showFrameWithScreenshot(screenshot);        

            feedScreenshotBytes(screenshot);
            if(getScreenshotPath()!=null){
                writeToDisk(screenshot, IMG_FORMAT, getScreenshotPath());
            }
        }catch(ScreenshotException se){
            throw new PlayerException("Unable to take screenshot", se);
        }
        showCursor();
    }
    
    private void handleInputModeEvent(InputModeEvent inputModeEvent){
        this.currentActionsPlayed = !inputModeEvent.isOneSideInputOn();
    }
    
    private void handleFrameMoveEvent(WindowMoveEvent windowMoveEvent){
        LOGGER.finer("-------------------------------------------------------");
        LOGGER.finer("Searching for " + windowMoveEvent.getKey());
        Window window = findWindow(windowMoveEvent.getKey());
        if(window!=null)
        {
            LOGGER.finer("Moving " + windowMoveEvent.getKey() + " to " 
                        + windowMoveEvent.getX() + "," + windowMoveEvent.getY());
            window.setLocation(windowMoveEvent.getX(), windowMoveEvent.getY());
        }else{
            LOGGER.warning("Window " + windowMoveEvent.getKey() + " NOT FOUND!");
        }
    }
    
    /**
     * This method uses really BIG APPROXIMATION to find the concerned frame.
     * The window key is its size and its type (Dialog, Frame, Window) as well as its visible state.
     * This usually works well for 80% of our tests but should be improved in the future.
     */
    private Window findWindow(String key){
        Window returnedWindow = null;
        Frame[] frames = Frame.getFrames();
        for(int i=0; i<frames.length; i++){
            Frame frame = frames[i];
            LOGGER.finer("testing frame: " + WindowMoveEvent.buildKey(frame));
            if(key.equals(WindowMoveEvent.buildKey(frame)) && frame.isVisible()){
                returnedWindow = frame;
                break;
            }else{
                Window ownedWindow = findOwnedWindow(frame, key);
                if(ownedWindow!=null){
                    returnedWindow = ownedWindow;
                    break;
                }
            }
        }
        return returnedWindow;
    }
    
    private Window findOwnedWindow(Window parent, String key){
        Window returnedOwnedWindow = null;
        Window[] ownedWindows = parent.getOwnedWindows();
        for(int i=0; i<ownedWindows.length; i++){
            Window ownedWindow = ownedWindows[i];
            LOGGER.finer("Found ownedWindow " + WindowMoveEvent.buildKey(ownedWindow) + " for parent " + WindowMoveEvent.buildKey(parent));
            if(key.equals(WindowMoveEvent.buildKey(ownedWindow)) && ownedWindow.isVisible()){
                returnedOwnedWindow = ownedWindow;
                break;
            }else{
                returnedOwnedWindow = findOwnedWindow(ownedWindow, key);
            }
        }
        LOGGER.finer("Returning ownedWindow " + returnedOwnedWindow);
        return returnedOwnedWindow;
    }    
    
    public static byte[] toBytes(BufferedImage image, String type) throws PlayerException{
        byte[] pictureBytes = null;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try{
            ImageIO.write(image, type, stream);

            pictureBytes = stream.toByteArray();
            stream.flush();
        }catch(Exception ex){
            throw new PlayerException("Can not serialize image", ex);
        }finally{
            try{
                stream.close();
            }catch(Exception e){
                //ignore
            }
        }
        return pictureBytes;
    }  
    
    public void writeToDisk(BufferedImage image, String type, String filePath) throws PlayerException{
        FileOutputStream stream = null;
        try{
            File file = new File(filePath.substring(0, filePath.lastIndexOf('/')));
            file.mkdirs();
            stream = new FileOutputStream(filePath);
            ImageIO.write(image, type, stream);
            stream.flush();
        }catch(Exception ex){
            throw new PlayerException("Can not write image into file '" + filePath + "'", ex);
        }finally{
            try{
                if(stream!=null){
                    stream.close();
                }
            }catch(Exception e){
                //ignore
            }
        }
    }    
    
    private void feedScreenshotBytes(BufferedImage screenshot) throws PlayerException{
        lastScreenshotBytes = toBytes(screenshot, IMG_FORMAT);
    }
    
    
    private void showFrameWithScreenshot(BufferedImage screenshot){
        final JFrame screenshotFrame = new JFrame("Scenario Screenshot Done");
        screenshotFrame.getContentPane().setLayout(new BorderLayout());
        BufferedImage displayedImg = IgnoredBgPattern.getDisplayedImageWithBg(screenshot);
        ScreenshotPanel screenshotPanel = new ScreenshotPanel(displayedImg, false);
        screenshotFrame.getContentPane().add(screenshotPanel, BorderLayout.CENTER);
        screenshotFrame.pack();
        screenshotFrame.setVisible(true);

        try{
            for(int i=0; i<3; i++){
                Thread.sleep(1000);
                screenshotFrame.setTitle(screenshotFrame.getTitle()+".");
            }
        }catch(InterruptedException ie){
            //ignore this exception
        }
        screenshotFrame.dispose();

    }
    
    private void drawIgnoredZones(BufferedImage image, ComparisonEvent comparisonEvent){
        List ignoredZones = comparisonEvent.getIgnoredZones();
        Graphics g = image.getGraphics();
        if(ignoredZones!=null && ignoredZones.size()>0){
            BufferedImage patternImage = ImageUtilities.createEmptyTransparentImage(image.getWidth(), 
                                                                                    image.getHeight());
            for(int i=0; i<ignoredZones.size(); i++){
                Rectangle rect = (Rectangle) ignoredZones.get(i);
                Image subImage = patternImage.getSubimage(rect.x, rect.y, rect.width, rect.height);
                ((Graphics2D)g).setBackground(new Color(1.0f, 0.0f, 0.0f, 0.0f));
                g.clearRect(rect.x, rect.y, rect.width, rect.height);
            }
        }
    }
    
    public void closePlayer() throws PlayerException{
        try{
            this.in.close();
        }catch(Exception e){
            throw new PlayerException("Unable to close player : " + e.getMessage());
        }
    }
    
    private Thread playThread;
    
    public void startScenario(final byte[] scenario, boolean fullInput){
        this.fullInput = fullInput;
        playThread = new Thread(){
            public void run(){
                try{
                    Player.this.setInputScenario(scenario);
                    Player.this.play();
                }catch(Throwable t){
                    Player.this.playException = t;
                }finally{
                    Player.this.playThread = null;
                }
            }
        };
        playThread.start();
    }
    
    public byte[] waitForScenarioEnd(){
        Thread t = playThread;
        if(t!=null){
            try{    
                t.join();
            }catch(InterruptedException ie){
                //to ignore
            }
        }
        
        return lastScreenshotBytes;
    }
    
    public void stop(){
        
    }
    
    public String getHost(){
        return this.hostName;
    }
    
    public int getJmxPort(){
        return this.jmxPort;
    }
    
    public abstract boolean launch(String[] args);
    
    public abstract boolean exit(long timeBeforeExit, boolean withRestart);
    
    public abstract String getVersion();
    
    private String getScreenshotPath(){
        if(screenshotsDirectory!=null){
            return screenshotsDirectory + '/' + scenarioName + '.' + IMG_FORMAT;
        }else{
            return null;
        }
    }
    
    private void hideCursor(){
        if(this.clientWindow.getCursor() != HIDDEN_CURSOR){
            this.clientWindow.setCursor(HIDDEN_CURSOR);
        }
    }

    private void showCursor(){
        if(this.clientWindow.getCursor() != DEFAULT_CURSOR){
             this.clientWindow.setCursor(DEFAULT_CURSOR);
        }
    }    
    
    public boolean isReadyToBeTested() throws FrameNotFoundException{
        LOGGER.fine(hostName + ":" + jmxPort + " searching for window :" + this.windowObjectName);
        this.clientWindow = FrameFacilities.findClientWindow(this.windowObjectName);
        LOGGER.fine(hostName + ":" + jmxPort 
                    + " clientWindow is " + this.clientWindow 
                    + ((this.clientWindow!=null)?" and visible " + this.clientWindow.isVisible():""));
        return (this.clientWindow!=null && this.clientWindow.isVisible());
    }    
    

    public void addStateListener(PlayerListener playerListener){
        stateListeners.add(playerListener);
    }
    
    public void notifyStartedListeners(){
        for(int i=0; i<stateListeners.size(); i++){
            ((PlayerListener)stateListeners.get(i)).playerStarted();
        }
    }

    public void notifyAbortedListeners(Throwable t){
        for(int i=0; i<stateListeners.size(); i++){
            ((PlayerListener)stateListeners.get(i)).playerAborted(t);
        }
    }

    public void notifyTerminatedListeners(){
        for(int i=0; i<stateListeners.size(); i++){
            ((PlayerListener)stateListeners.get(i)).playerTerminated();
        }
    }
    
    public static void setWithRestart(boolean shouldRestart){
       WITH_RESTART = shouldRestart; 
    }
    
    public static boolean shouldRestart(){
        return WITH_RESTART;
    }
    
    public Window getClientWindow(){
        return this.clientWindow;
    }  
    
    public void setRealTimeMode(boolean realtime){
        this.realtimeMode = realtime;
        LOGGER.info("Realtime Mode set to " + this.realtimeMode);
    }

}
