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

public class ScenariiDirectories {
    
    private String scenariiBaseDirectory;
    private String setupDirectory;
    private String tearDownDirectory;
    private String scenariiDirectory;
    private String ignoreLayersDirectory;
    
    
    public ScenariiDirectories(String scenariiBaseDirectory, String setupDirectory, 
                               String tearDownDirectory, String scenariiDirectory){
        this.scenariiBaseDirectory = scenariiBaseDirectory;
        this.setupDirectory = setupDirectory;
        this.tearDownDirectory = tearDownDirectory;
        this.scenariiDirectory = scenariiDirectory;
    }

    public String getScenariiDirectory() {
        return scenariiDirectory;
    }

    public String getSetupDirectory() {
        return setupDirectory;
    }

    public String getTearDownDirectory() {
        return tearDownDirectory;
    }

    public void setIgnoreLayersDirectory(String ignoreLayersDirectory) {
        this.ignoreLayersDirectory = ignoreLayersDirectory;
    }
    
    public String getIgnoreLayersDirectory() {
        return ignoreLayersDirectory;
    }
    
    public String getScenariiBaseDirectory(){
        return this.scenariiBaseDirectory;
    }
    
}
