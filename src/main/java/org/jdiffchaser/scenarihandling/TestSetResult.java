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


public class TestSetResult {

    /**
     * The tests we should have played (no unexpected exit)
     */
    private int totalTestCount;

    /**
     * The tests we've REALLY played
     */
    private int totalTestPlayed;
    
    /**
     * The tests that have succeded
     */
    private int totalTestSucceded;
    
    /**
     * The comment (may be the version number) of the first host
     */
    private String firstHostComment;
    
    /**
     * The comment (may be the version number) of the second host
     */
    private String secondHostComment;

    public TestSetResult(int totalTestCount) {
        this.totalTestCount = totalTestCount;
    }
    
    public void incrementTestPlayed(){
        this.totalTestPlayed++;
    }
    
    public int getTotalTestPlayed(){
        return this.totalTestPlayed;
    }
    
    public void incrementTestSucceded(){
        this.totalTestSucceded++;
    }

    public int getTotalTestSucceded(){
        return this.totalTestSucceded;
    }
    
    public int getTotalTestCount(){
        return this.totalTestCount;
    }    
    
    public void setFirstHostComment(String comment){
        this.firstHostComment = comment;
    }

    public void setSecondHostComment(String comment){
        this.secondHostComment = comment;
    }
    
    public String getFirstHostComment(){
        return this.firstHostComment;
    }

    public String getSecondHostComment(){
        return this.secondHostComment;
    }
}
