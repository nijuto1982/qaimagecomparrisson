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

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.jdiffchaser.utils.JMXUtils;
import java.io.IOException;
import java.util.logging.Logger;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;


public class PlayerHandle {
    
    private static final Logger LOGGER = Logger.getLogger(PlayerHandle.class.getName());
    private static final String LOCALHOST = "localhost";
    
    private PlayerMBean proxy;
    
    private ObjectName objectName;
    
    private JMXServiceURL jmxServiceURL;
    
    private JMXConnector jmxc = null;    
    
    private String  ip;
    private int     jmxPort;
    
    /** Creates a new instance of PlayerHandle */
    public PlayerHandle(String ip, int jmxPort) {
        if(LOCALHOST.equals(ip.toLowerCase())){
            try{
                this.ip = InetAddress.getLocalHost().getHostAddress();
            }catch(UnknownHostException uhe){
                LOGGER.severe("Unable to find localhost address");
                uhe.printStackTrace();
                System.exit(1);
            }
        }else{
            this.ip = ip;
        }
        this.jmxPort = jmxPort;
    }
    
    public String getIP(){
        return this.ip;
    }
    
    public int getPort(){
        return this.jmxPort;
    }
    
    public PlayerMBean getProxy(){
        return proxy;
    }
    
    public void close(){
        proxy = null;
        try{
            if(this.jmxc!=null){
                JMXConnector jmxc2 = this.jmxc;
                this.jmxc = null;
                jmxc2.close(); 
            }
        }catch(Throwable th){
            LOGGER.warning("Unable to close JMX Connector : " + th.getMessage());
        }      
    }    
    
    public boolean contact() throws IOException, 
                                    InstanceNotFoundException, 
                                    ClassNotFoundException {
        
        boolean success = false;
        
        close();
        
        try{
            objectName = new ObjectName("PlayerMBean:host=" + this.ip + ",port=" + this.jmxPort);

            jmxServiceURL = new JMXServiceURL("jmxmp", ip, jmxPort);
    
            LOGGER.fine("Trying to create a JMX Connection for " + jmxServiceURL);
            jmxc = JMXConnectorFactory.newJMXConnector(jmxServiceURL, JMXUtils.JMX_PROPERTIES);
            
            jmxc.connect();            
                        
            MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();         
            proxy = (PlayerMBean)MBeanServerInvocationHandler.newProxyInstance( mbsc,
                                                                                objectName,
                                                                                PlayerMBean.class,
                                                                                true);
            LOGGER.fine("Proxy instance (objectName=" + objectName+ ")");
                             
            LOGGER.fine("JMX Connection OK for " + jmxServiceURL);
            success = true;
        }catch(IOException ioe){
            LOGGER.warning("jmxContact failed : " + ioe.getMessage());
            // Something is wrong with the proxy (no error thrown at creation)
            close();
            throw ioe;            
        }catch(IllegalStateException ise){
            LOGGER.warning(ise.getMessage());
            close();   
            success = false;
        }catch(OutOfMemoryError oom){
            LOGGER.warning("Out of memory in JVM, exiting: " + oom.getMessage());
            System.exit(-1);
        }catch(java.lang.reflect.UndeclaredThrowableException ue){
            Throwable cause = ue.getCause();
            if( cause!=null && cause instanceof java.io.InvalidClassException ){
                LOGGER.warning("(Problem communicating with jmxServiceURL=" + jmxServiceURL 
                               +"', objectName='"+objectName+"' :: "+cause.getMessage());
            }else{
                Throwable error = cause;
                if( error==null ) {
                    error = ue;
                }
                LOGGER.warning("Error contacting proxy : jmxServiceURL='" + jmxServiceURL 
                               +"', objectName='"+objectName + '\'' + error.getMessage());
            }
            close();
            success = false;
        }catch(Throwable th){
            LOGGER.warning("Inconsistent proxy : jmxServiceURL='" + jmxServiceURL 
                           +"', objectName='"+objectName + '\'' + th.getMessage() 
                           + '\n' + "Cause : " + th.getCause());
            close();
            success = false;
        }
        return success;
    }

}
