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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import javax.swing.JButton;
import javax.swing.JFrame;


public class ChronoPanel extends javax.swing.JPanel {

    private long elapsedTime = 0;
    private int  seconds     = 0;    
    private int  minutes     = 0;
    private int  hours       = 0;
    
    private boolean running  = false;
    
    private Timer timer;
    private ActionListener updateChronoTask;
    
    private static final Color RUNNING_FG_COLOR = Color.BLACK;
    private static final Color PAUSING_FG_COLOR = Color.GRAY;
    
    public ChronoPanel() {
        initComponents();
        updateChronoTask = new ActionListener(){
            public void actionPerformed(ActionEvent e){
                incrementChrono();
                updateChronoValues();
            }
        };
        timer = new Timer(1000, updateChronoTask);
        setLabelsFgColor(PAUSING_FG_COLOR);
    }
    
    private void setLabelsFgColor(Color color){
        this.lbSeconds.setForeground(color);
        this.lbPointsMs.setForeground(color);
        this.lbMinutes.setForeground(color);
        this.lbPointsHm.setForeground(color);
        this.lbHours.setForeground(color);
    }
    
    public void start(){
        setLabelsFgColor(RUNNING_FG_COLOR);
        timer.start();
        running = true;
    }
    
    public void pause(){
        setLabelsFgColor(PAUSING_FG_COLOR);
        timer.stop();
        running = false;
    }
    
    private void incrementChrono(){
        elapsedTime++;
        seconds++;
        if(seconds >= 60){
            seconds = 0;
            minutes++;
        }
        if(minutes >= 60){
            minutes = 0;
            hours++;
        }
    }
    
    private String padNumber(int num){
        StringBuffer sbuf = new StringBuffer();
        if(num<10){
            sbuf.append('0');
        }
        sbuf.append(num);
        return sbuf.toString();
    }
    
    private void updateChronoValues(){
        lbHours.setText(padNumber(hours));
        lbMinutes.setText(padNumber(minutes));
        lbSeconds.setText(padNumber(seconds));       
    }
    
    public void reset(){
        pause();
        elapsedTime = 0;
        seconds     = 0;
        minutes     = 0;
        hours       = 0;
        updateChronoValues();
    }
    
    public long getElapsedTime(){
        return elapsedTime;
    }
    
    public boolean isRunning(){
        return running;
    }
    
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        lbHours = new javax.swing.JLabel();
        lbPointsHm = new javax.swing.JLabel();
        lbMinutes = new javax.swing.JLabel();
        lbPointsMs = new javax.swing.JLabel();
        lbSeconds = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        lbHours.setFont(new java.awt.Font("Tahoma", 0, 18));
        lbHours.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbHours.setText("00");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        add(lbHours, gridBagConstraints);

        lbPointsHm.setFont(new java.awt.Font("Tahoma", 0, 18));
        lbPointsHm.setText(":");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(lbPointsHm, gridBagConstraints);

        lbMinutes.setFont(new java.awt.Font("Tahoma", 0, 18));
        lbMinutes.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbMinutes.setText("00");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        add(lbMinutes, gridBagConstraints);

        lbPointsMs.setFont(new java.awt.Font("Tahoma", 0, 18));
        lbPointsMs.setText(":");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(lbPointsMs, gridBagConstraints);

        lbSeconds.setFont(new java.awt.Font("Tahoma", 0, 18));
        lbSeconds.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbSeconds.setText("00");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        add(lbSeconds, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents
    
    
    public static void main(String args[]){
        final ChronoPanel chrono = new ChronoPanel();
        JFrame frame = new JFrame("Chrono test");
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(chrono, BorderLayout.CENTER);
        final JButton startbutton = new JButton("Start");
        startbutton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(chrono.isRunning()){
                    startbutton.setText("Start");
                    chrono.pause();                    
                }else{
                    startbutton.setText("Pause");
                    chrono.start();
                }
            }
        });
        JButton resetbutton = new JButton("Reset");
        resetbutton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                chrono.reset();
            }
        });   
        frame.getContentPane().add(startbutton, BorderLayout.WEST);
        frame.getContentPane().add(resetbutton, BorderLayout.EAST);
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(400,400,300,80);
        frame.setVisible(true);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lbHours;
    private javax.swing.JLabel lbMinutes;
    private javax.swing.JLabel lbPointsHm;
    private javax.swing.JLabel lbPointsMs;
    private javax.swing.JLabel lbSeconds;
    // End of variables declaration//GEN-END:variables
    
}
