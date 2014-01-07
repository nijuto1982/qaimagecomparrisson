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

import org.jdiffchaser.scenarihandling.Recorder;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.logging.Logger;
import org.jdiffchaser.gui.FrameFacilities;

public class DefaultRecorder {

    private static final Logger LOGGER = Logger.getLogger(DefaultRecorder.class.getName());    
    
    private DefaultRecorder() {
    }

    /**
     * args[0] must be the fully qualified class name to test (e.g.: ch.toto.inch.tools.jms.JMSSender)
     * args[1] must be the jmxPort used by Recorder Remote Control
     *
     * BEWARE: This main method may seem weird because of the usage of reflection. This because
     * we want to be able to launch any recorder on any version of your software, no matter if packages have changed
     *
     */
    public static void main(String[] args){
        try{

            if(args.length<4){
                System.out.println("Usage: DefaultRecorder [MainClassName] [Frame Object Name] [OutputDirectory] [FreeJmxPort] [ [App Arg1] [App Arg2] [...]]");
                System.exit(0);
            }
            
            Class mainClass = Class.forName(args[0]);
            String[] mainArgs = new String[args.length - 4];
            System.arraycopy(args, 4, mainArgs, 0, args.length - 4);
            Method mainMethod = mainClass.getMethod("main", new Class[]{String[].class});
            System.out.println("Invoking main with " + Arrays.asList(mainArgs));
            mainMethod.invoke(null, new Object[]{mainArgs});

            Recorder recorder = new Recorder(args[1],
                                             args[2],
                                             InetAddress.getLocalHost().getHostAddress(),
                                             Integer.parseInt(args[3]));
            LOGGER.info("Displayed frames are: " + FrameFacilities.getDisplayedFramesString());           
        }catch(Exception e){
            e.printStackTrace();

        }
    }
    

}
