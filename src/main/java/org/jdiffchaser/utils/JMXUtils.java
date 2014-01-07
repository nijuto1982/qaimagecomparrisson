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

package org.jdiffchaser.utils;

import com.sun.jmx.remote.generic.DefaultConfig;
import com.sun.jmx.remote.util.EnvHelp;
import java.util.Hashtable;
import java.util.Map;
import javax.management.remote.JMXConnectorServerFactory;

public class JMXUtils {

    public static final Map JMX_PROPERTIES = new Hashtable(){
        {
            String socketTimeout = "" + (60*60*1000);
            put(JMXConnectorServerFactory.PROTOCOL_PROVIDER_PACKAGES,"com.sun.jmx.remote.protocol");
            // timeout in milliseconds for a client to wait for its state to become connected.
            put(DefaultConfig.TIMEOUT_FOR_CONNECTED_STATE,  "" + 2000);
            // timeout in milliseconds for a client request to wait for its response.
            put(DefaultConfig.REQUEST_WAITING_TIME,  socketTimeout);
            // timeout to keep a server side connection after answering last client request.
            put(EnvHelp.SERVER_CONNECTION_TIMEOUT,  "" + (2*60*1000) );
            // timeout for a client to fetch notifications from its server.
            put(EnvHelp.FETCH_TIMEOUT,  socketTimeout);

            put("jmx.socket.timeout", socketTimeout);
        }
    };


    /** Creates a new instance of JMXUtils */
    private JMXUtils() {
    }
    
}