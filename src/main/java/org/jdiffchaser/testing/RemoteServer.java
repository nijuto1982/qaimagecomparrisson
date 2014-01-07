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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.jdiffchaser.scenarihandling.Player;
import org.jdiffchaser.scenarihandling.PlayerException;
import org.jdiffchaser.scenarihandling.PlayerServer;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import javax.swing.JFrame;

public abstract class RemoteServer {

    protected String[] args;

    public abstract Player getPlayer(String fullClassName, String windowObjectName, String hostName,
            int jmxPort, String screenshotsDirectory) throws PlayerException;
    private Thread restartShutdownHook;

    public RemoteServer(final String[] args) {

        this.args = args;

        restartShutdownHook = new Thread() {

            public void run() {
                System.out.println("Exiting");
                if (Player.shouldRestart()) {
                    
                    System.out.println(" Restarting JMX server to play forthcoming tests");

                    String classpath = System.getProperty("java.class.path");
                    String command = null;
                    List excludedProperties = Arrays.asList(new String[]{"awt",
                                                                         "file",
                                                                         "java.awt",
                                                                         "java.class",
                                                                         "java.endorsed",
                                                                         "java.ext",
                                                                         "java.home",
                                                                         "java.io",
                                                                         "java.runtime",
                                                                         "java.specification",
                                                                         "java.vendor",
                                                                         "java.version",
                                                                         "java.vm", 
                                                                         "line",
                                                                         "os",
                                                                         "path",
                                                                         "sun",
                                                                         "user",
                                                                         "apple",
                                                                         "gopherProxySet",
                                                                         "mrj"
                                                                    });
                    
                    List commandStr = new ArrayList();
                    commandStr.add(System.getProperty("java.home") + File.separatorChar + "bin" + File.separator + "java");
                    commandStr.add("-Xmx" + ((Runtime.getRuntime().maxMemory() / 1024) / 1024) + "M");
                    
                    Properties props = getProperties(excludedProperties);
                    for (Enumeration enume = props.keys(); enume.hasMoreElements();) {
                        String key = (String) enume.nextElement();
                        commandStr.add(getJVMArg(key, props.getProperty(key)));                          
                    }
                    
                    commandStr.add("-classpath");
                    commandStr.add(classpath);
                    commandStr.add(RemoteServer.this.getClass().getName());
                    
                    for (int i = 0; i < args.length; i++) {
                        commandStr.add(args[i]);
                    }                    
                    
                    ProcessBuilder pb = new ProcessBuilder(commandStr);
                    
                    System.out.println("command used to restart: " + commandStr);
                    Process p = null;
                    try {
                        p = pb.start();
                        //ONLY for debug purpose: let user see the children logs
                        //don't let this jvm arg to true as it locks all children process to their parent
                        if(Boolean.getBoolean("debug.restart")){
                            redirectErrorStream(p);
                        }
                        //
                        System.out.println("Process created");
                        
                    } catch (Throwable e) {
                        System.out.println("Unable to restart JMX Server");
                        e.printStackTrace();
                    }
                } else {
                    System.out.println(" No restart requested ");
                }
            }
        };
    }
    

    private void redirectErrorStream(Process p) throws IOException{
        InputStream stderr = p.getErrorStream();
        InputStreamReader isr = new InputStreamReader(stderr);
        BufferedReader br = new BufferedReader(isr);
        String line = null;
        System.out.println("<Child process error stream>");
        while ( (line = br.readLine()) != null)
            System.out.println(line);
        System.out.println("</Child process error stream>");        
    }

    private boolean startsWith(String key, List prefixes){
        boolean found = false;
        for(Iterator ite=prefixes.iterator(); ite.hasNext();){
            String prefix = (String) ite.next();
            found = key.startsWith(prefix);
            if(found){
                break;
            }
        }
        return found;
    }

    private Properties getProperties(List excludedKeys){
        Properties properties = (Properties) System.getProperties().clone();
        for(Enumeration elements = properties.keys(); elements.hasMoreElements();){
            String key = (String) elements.nextElement();
            if(excludedKeys!=null && startsWith(key, excludedKeys)){
                properties.remove(key);
            }
        }
        return properties;
    }    
    
    private String getJVMArg(String key, String prop){
        StringBuffer sbuf = new StringBuffer();
        sbuf.append("-D").append(key).append("=");
        sbuf.append(prop);
        return sbuf.toString();
    }
    
    public void launch() {
        try {
            
            JFrame testFrame = new JFrame("dummy");
            Runtime.getRuntime().addShutdownHook(restartShutdownHook);
            testFrame.setVisible(false);
            String windowObjectName = null;
            String screenshotsPath = null;
            String fullClassName = null;
            int port = -1;
            if (args.length < 4) {
                System.out.println("Usage: " + this.getClass().getName() + " <port> <fullClassName to test> <Frame Object Name> <screenshotsPath> [<app arg1> <app arg2> <...>]");
                System.exit(1);
            } else {
                port = Integer.parseInt(args[0]);
                fullClassName = args[1];
                windowObjectName = args[2];
                screenshotsPath = args[3];
            }

            final PlayerServer pls = new PlayerServer(getPlayer(fullClassName,
                    windowObjectName,
                    InetAddress.getLocalHost().getHostAddress(),
                    port,
                    screenshotsPath));
                        
            Runtime.getRuntime().addShutdownHook(new Thread(){
            
                public void run(){
                    pls.close();
                }
                
            });
            
            
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
