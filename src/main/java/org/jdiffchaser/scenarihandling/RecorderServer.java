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

import org.jdiffchaser.utils.JMXUtils;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;


public class RecorderServer {
    
    private static final Logger LOGGER = Logger.getLogger(RecorderServer.class.getName());
    
    private ObjectName mbeanName;
    
    private Recorder recorder;
    
    /**
     * Connector server
     */
    private JMXConnectorServer connectorServer;
    
    /**
     * MBeanServer in which the player contained by this PlayerServer
     * will be registered
     */
    private MBeanServer mbs = null;
    
   
    public RecorderServer(Recorder recorder, String host, int jmxPort){
        this.recorder = recorder;
        this.registerMBean(host, jmxPort);
    }
    
    /**
     * Unregisters objects in JMX space
     */
    public void close(){
        
        try{            
            
            connectorServer.stop();
            
            mbs.unregisterMBean(mbeanName);
            
        }catch (Exception e) {  
            LOGGER.log(Level.WARNING, "Unable to close PlayerServer", e);
        }
    }
                   
    /**
     * Registers the MBean in the MBeanServer and
     * creates the JMXConnectorServer used by remote JMXConnectors
     */
    private void registerMBean(String host, int jmxPort){
        try {                        
            // Instantiate the MBean server            
            mbs = MBeanServerFactory.createMBeanServer();
            System.out.println("MBeanServer = " + mbs);            
            
            mbeanName = new ObjectName("RecorderMBean:host=" + host + ",port=" + jmxPort);
            
            System.out.println("Registering Service MBean (name=" + mbeanName + ")");            
            // Register 'object'
            mbs.registerMBean(this.recorder, mbeanName);
                        
            JMXServiceURL url = new JMXServiceURL("jmxmp", host, jmxPort);
            System.out.println("JMX service url is: '" + url + '\'');
            
            // Connector server
            connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, JMXUtils.JMX_PROPERTIES, mbs);
                        
            // Start the connector server
            System.out.println("Start the connector server");
            
            connectorServer.start();           
            System.out.println("Connector server successfully started, waiting for incoming connections");
            
            
        } catch (Exception e) {
            
            System.out.println("Error in registering MBean, object with keyname ' and mbean is '" + mbeanName + '\'');
            e.printStackTrace();
            System.exit(1);
        }
    }

    
}
