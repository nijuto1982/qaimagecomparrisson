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

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.lang.reflect.Method;
import java.util.logging.Logger;
import org.jdiffchaser.scenarihandling.Player;
import org.jdiffchaser.scenarihandling.PlayerException;

public class DefaultScenarioPlayer extends Player {

    private static final Logger LOGGER = Logger.getLogger(DefaultScenarioPlayer.class.getName());    
    
    private final String DEFAULT_EXIT_KEY = KeyEvent.getKeyText(KeyEvent.VK_Q);
    private final String DEFAULT_EXIT_KEYMODIFIER = KeyEvent.getKeyModifiersText(KeyEvent.ALT_MASK);
    private String exitKeyCodeText;
    private String exitKeyCodeModifiersText;

    public DefaultScenarioPlayer(String fullClassName, String windowObjectName, String screenshotsDirectory)
            throws PlayerException {
        super(fullClassName, windowObjectName, screenshotsDirectory);
        exitKeyCodeText = System.getProperty("exit.key", DEFAULT_EXIT_KEY);
        exitKeyCodeModifiersText = System.getProperty("exit.key.modifier", DEFAULT_EXIT_KEYMODIFIER);
    }

    public DefaultScenarioPlayer(String fullClassName, String windowObjectName, String hostName,
            int jmxPort, String screenshotsDirectory) throws PlayerException {
        super(fullClassName, windowObjectName, hostName, jmxPort, screenshotsDirectory);
    }

    /**
     * args array containing the application parameters
     * use of reflection to be able to test any version of your app, no matter
     * the package the version is in.
     */
    public boolean launch(String[] args) {

        LOGGER.fine("Launching application");
        LOGGER.finer("Classpath is" + System.getProperty("java.class.path"));
        LOGGER.finer("Args, before truncate, are [" + arrayToString(args) + "]");
        try {
            String[] mainArgs = new String[0];
            if (args.length > 0) {
                mainArgs = new String[args.length - 3];
                System.arraycopy(args, 3, mainArgs, 0, args.length - 3);
                LOGGER.finer("MainArgs, after truncate, are [" + arrayToString(mainArgs) + "]");
            }

            Method mainMethod = mainClass.getMethod("main", new Class[]{String[].class});
            LOGGER.finer("main method parameters types are [" + arrayToString(mainMethod.getParameterTypes()) + "]");
            mainMethod.invoke(null, new Object[]{mainArgs});
            Thread.sleep(1000); //needed to make the getFrame method successful in ALL cases... weird thing
            //this.clientWindow = FrameFacilities.findClientWindowWithName(this.windowObjectName);
            LOGGER.fine("Application launched");
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
        return true;
    }

    private static String arrayToString(Object[] array) {
        StringBuffer sbuf = new StringBuffer();
        for (int i = 0; i < array.length; i++) {
            sbuf.append("[").append(array[i]).append("]");
            if (i < array.length - 1) {
                sbuf.append(",");
            }
        }
        return sbuf.toString();
    }

    public boolean exit(final long timeBeforeExit, final boolean withRestart) {
        new Thread() {

            public void run() {
                Player.setWithRestart(withRestart);
                try {
                    Thread.sleep(timeBeforeExit);
                } catch (InterruptedException ie) {
                    //
                    }
                System.exit(0);
            }
        }.start();
        return true;
    }

    /**
     * In a dedicated player, override this method to have a correct version number.
     * If using this player, create a static getVersion method in the main class you're testing
     */
    public String getVersion() {
        Method getVersionMethod = null;
        try {
            getVersionMethod = mainClass.getMethod("getVersion", null);
        } catch (Throwable t) {
        }
        if (getVersionMethod != null) {
            try {
                return (String) getVersionMethod.invoke(null, null);
            } catch (Throwable t) {
                return "<Using DefaultPlayer: unknown version>";
            }
        } else {
            return "<Using DefaultPlayer: unknown version>";
        }
    }

    public void addExitKeysSequence() {
        Toolkit.getDefaultToolkit().addAWTEventListener(
                new AWTEventListener() {

                    public void eventDispatched(AWTEvent e) {
                        KeyEvent ke = (KeyEvent) e;
                        String currentKeyCodeText = KeyEvent.getKeyText(ke.getKeyCode());
                        String currentKeyCodeModifiers = KeyEvent.getKeyModifiersText(ke.getModifiers());
                        if (currentKeyCodeText.equals(exitKeyCodeText) && currentKeyCodeModifiers.equals(exitKeyCodeModifiersText)) {
                            System.out.println("Aborting scenario playing: user requested an exit.");
                            System.exit(0);
                        }
                    }
                }, AWTEvent.KEY_EVENT_MASK);
    }
}
