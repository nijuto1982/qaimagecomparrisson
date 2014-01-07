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

package org.jdiffchaser.scenarihandling.events;

import java.awt.AWTEvent;
import java.awt.Event;


public class StartEvent extends AWTEvent{
    
    private String  scenarioName;
    
    /**
     *  if inputScenario is true, this means that for this scenario, some action won't be played by one of the clients 
     *  (among the two of the testSet) because the second client can't do the same input at the same time 
     *  due to system limitations (the system we are testing).
     */
    private boolean inputScenario;
    
    public StartEvent(Event event) {
        super(event);
    }
    
    public StartEvent(Object source, int id, String scenarioName, boolean inputScenario) {
        super(source, id);
        this.scenarioName = scenarioName;
        this.inputScenario = inputScenario;
    }
    
    public String getScenarioName(){
        return scenarioName;
    }
    
    public boolean isInputScenario(){
        return inputScenario;
    }
    
    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append("\nscenarioName = ").append(this.scenarioName).append("\n");
        sb.append("inputScenario = ").append(this.inputScenario);
        return sb.toString();
    }
}
