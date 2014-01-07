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

import org.jdiffchaser.scenarihandling.Player;
import org.jdiffchaser.scenarihandling.PlayerException;

public class DefaultRemoteServer extends RemoteServer{
    
    public Player getPlayer(String fullClassName, String windowObjectName, String hostName, 
                            int jmxPort, String screenshotsDirectory) throws PlayerException{
        return new DefaultScenarioPlayer(fullClassName, windowObjectName, hostName, 
                                         jmxPort, screenshotsDirectory);
    }
    
    public DefaultRemoteServer(String[] args) {
        super(args);
    }
    
    public static void main(String[] args){
        new DefaultRemoteServer(args).launch();
    }
    
}
