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


public class PlayerServer {
    
    private static final Logger LOGGER = Logger.getLogger(PlayerServer.class.getName());
    
    /**
     * The service/position managed by this server
     */
    protected Player player = null;
    
    /**
     * MBeanName of the service
     */
    private ObjectName mbeanName;
    
    /**
     * Connector server
     */
    private JMXConnectorServer connectorServer;
    
    /**
     * MBeanServer in which the player contained by this PlayerServer
     * will be registered
     */
    private MBeanServer mbs = null;
    
   
    public PlayerServer(Player player) throws PlayerException{
        if(player.getHost()==null || player.getJmxPort()<0){
            throw new IllegalArgumentException("Player argument must have a hostname and a jmxport set!");
        }
        this.player = player;
        this.registerMBean();
    }
    
    /**
     * Unregisters objects in JMX space
     */
    public void close(){
        
        try{            

            player.stop();
            
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
    private void registerMBean(){
        try {                        
            // Instantiate the MBean server            
            mbs = MBeanServerFactory.createMBeanServer();
            LOGGER.fine("MBeanServer = " + mbs);            
            
            mbeanName = new ObjectName("PlayerMBean:host=" + player.getHost() + ",port=" + player.getJmxPort());
            
            LOGGER.fine("Registering Service MBean (name=" + mbeanName + ")");            
            // Register 'service'
            mbs.registerMBean(player, mbeanName);
                        
            JMXServiceURL url = new JMXServiceURL("jmxmp", player.getHost(), player.getJmxPort());
            LOGGER.fine("JMX service url is: '" + url + '\'');
            
            // Connector server
            connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, JMXUtils.JMX_PROPERTIES, mbs);
                        
            // Start the connector server
            LOGGER.info("Start the connector server");
            
            connectorServer.start();           
            LOGGER.info("Connector server successfully started, waiting for incoming connections");
            
            
        } catch (Exception e) {
            
            LOGGER.log(Level.SEVERE, "Error in registering MBean, player is '" 
                                     + player + "' object with keyname ' and mbean is '" + mbeanName + '\'',
                       e);
            System.exit(1);
        }
    }

    
}
