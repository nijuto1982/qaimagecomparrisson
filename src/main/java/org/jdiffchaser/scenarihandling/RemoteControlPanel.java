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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jdiffchaser.imgprocessing.ScreenshotException;
import org.jdiffchaser.scenarihandling.notifications.ControlNotification;
import org.jdiffchaser.scenarihandling.notifications.ScreenshotNotification;
import java.awt.Graphics;
import java.awt.event.MouseMotionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.swing.ImageIcon;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.jdiffchaser.gui.ExternalWaitWindow;
import org.jdiffchaser.utils.ChronoPanel;

public class RemoteControlPanel extends javax.swing.JPanel implements NotificationListener{
    
    private static final Logger LOGGER = Logger.getLogger(RemoteControlPanel.class.getName());
    
    public static interface TranslucencyListener{
        public void translucencyChanged(float alpha);
    }
    
    private RecorderMBean recorder;
    
    private static final String IDLE_LABEL      = "Idle...";
    private static final String RECORDING_LABEL = "RECORDING...";
    private static final String IN_RC_LABEL     = "Not recording RC actions...";
    
    private static final ImageIcon IMG_RECORD = new javax.swing.ImageIcon(
                RemoteControlPanel.class.getResource("/org/jdiffchaser/scenarihandling/record.png"));

    private static final ImageIcon IMG_STOP = new javax.swing.ImageIcon(
                RemoteControlPanel.class.getResource("/org/jdiffchaser/scenarihandling/stop.png"));
    
    private ChronoPanel chrono = new ChronoPanel();
    
    private JFormattedTextField secondsTextField;
    
    private Timer scDelayUpdateTimer; //for texfield update
    private Timer scDelayTriggerTimer; //for capture triggering
    private int   captureDelay;
        
    private TranslucencyListener translucencyListener;
    
    /** Creates new form BeanForm */
    public RemoteControlPanel() {
        initComponents();
        this.chronoCont.add(chrono);
        initTimers();
        initTranslucencySlider();
    }
    
    private void initTranslucencySlider(){
        this.translucencySlider.getModel().addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if(translucencyListener!=null){
                    float alphaValue = (float)RemoteControlPanel.this.translucencySlider.getModel().getValue()/100.0f;
                    RemoteControlPanel.this.translucencyListener.translucencyChanged(alphaValue);
                }else{
                    LOGGER.warning("No translucency listener set");
                }
            }
        });
    }
    
    public void setTranslucencyListener(TranslucencyListener translucencyListener){
        this.translucencyListener = translucencyListener;
    }
    
    private static String formatDelay(int delay){
        String prefix = "";
        if(delay<100){
            prefix += "0";
        }
        if(delay<10){
            prefix += "0";
        }
        return prefix + String.valueOf(delay);
    }
    
    private void initCaptureDelayUpdateTimer(){
        ActionListener captureDelayListener = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                int delay = Integer.parseInt(tfCaptureDelay.getText());
                if(delay>0){
                    delay--;
                    tfCaptureDelay.setText(String.valueOf(delay));
                }else{
                    tfCaptureDelay.setText(String.valueOf(RemoteControlPanel.this.captureDelay));
                    RemoteControlPanel.this.scDelayUpdateTimer.stop();
                }
            }
        };
        this.scDelayUpdateTimer = new Timer(1000, captureDelayListener);        
    }
    
    private void initCaptureDelayTriggeringTimer(){
        ActionListener captureDelayTriggeringListener = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                SwingUtilities.windowForComponent(RemoteControlPanel.this).setVisible(false);
                RemoteControlPanel.this.chrono.pause();
            }
        };
        this.scDelayTriggerTimer = new Timer(0, captureDelayTriggeringListener);        
        this.scDelayTriggerTimer.setRepeats(false);
    }    
    
    private void initTimers(){
        initCaptureDelayUpdateTimer();
        initCaptureDelayTriggeringTimer();        
    }
    
    private boolean validTimerValue(String text){
        try{
            Integer.parseInt(text);
        }catch(Exception e){
            JOptionPane.showMessageDialog(this, 
                                          "Invalid capture timer value\nCheck it is a number>=0", 
                                          "Oups...NaN?",
                                          JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }
    
    public void setRecorder(RecorderMBean recorder){
        this.recorder = recorder;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        lbTitle = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        panelButtons = new javax.swing.JPanel();
        btRecord = new javax.swing.JButton();
        btComparisonApp = new javax.swing.JButton();
        btComparisonFull = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        lbScChrono = new javax.swing.JLabel();
        tfCaptureDelay = new javax.swing.JTextField();
        cbBothInput = new javax.swing.JCheckBox();
        jPanel4 = new javax.swing.JPanel();
        btExit = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        panelScName = new javax.swing.JPanel();
        lbScName = new javax.swing.JLabel();
        tfScName = new javax.swing.JTextField();
        jPanel5 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        chronoCont = new javax.swing.JPanel();
        panelControls = new javax.swing.JPanel();
        panelButtonsStatus = new javax.swing.JPanel();
        panelStatus = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        lbRecordedData = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        lbStatusTitle = new javax.swing.JLabel();
        lbStatus = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        lbTranslucency = new javax.swing.JLabel();
        translucencySlider = new javax.swing.JSlider();

        setLayout(new java.awt.BorderLayout());

        setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        lbTitle.setBackground(new java.awt.Color(255, 255, 204));
        lbTitle.setFont(new java.awt.Font("Arial", 1, 18));
        lbTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbTitle.setText("Recorder Remote Control");
        lbTitle.setOpaque(true);
        add(lbTitle, java.awt.BorderLayout.NORTH);

        jPanel6.setLayout(new java.awt.BorderLayout());

        panelButtons.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        btRecord.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdiffchaser/scenarihandling/record.png")));
        btRecord.setToolTipText("Record / Stop");
        btRecord.setPreferredSize(new java.awt.Dimension(44, 44));
        btRecord.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btRecordActionPerformed(evt);
            }
        });

        panelButtons.add(btRecord);

        btComparisonApp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdiffchaser/scenarihandling/captureapp.png")));
        btComparisonApp.setToolTipText("Tested frame capture");
        btComparisonApp.setEnabled(false);
        btComparisonApp.setPreferredSize(new java.awt.Dimension(44, 44));
        btComparisonApp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btComparisonAppActionPerformed(evt);
            }
        });

        panelButtons.add(btComparisonApp);

        btComparisonFull.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdiffchaser/scenarihandling/capturefull.png")));
        btComparisonFull.setToolTipText("Fullscreen capture");
        btComparisonFull.setEnabled(false);
        btComparisonFull.setPreferredSize(new java.awt.Dimension(44, 44));
        btComparisonFull.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btComparisonFullActionPerformed(evt);
            }
        });

        panelButtons.add(btComparisonFull);

        jPanel7.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        lbScChrono.setText("Capture delay (s):");
        jPanel7.add(lbScChrono);

        tfCaptureDelay.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tfCaptureDelay.setText("0");
        tfCaptureDelay.setMaximumSize(new java.awt.Dimension(54, 22));
        tfCaptureDelay.setMinimumSize(new java.awt.Dimension(54, 22));
        tfCaptureDelay.setPreferredSize(new java.awt.Dimension(54, 22));
        jPanel7.add(tfCaptureDelay);

        panelButtons.add(jPanel7);

        cbBothInput.setSelected(true);
        cbBothInput.setText("Next actions must be played on both tested hosts");
        cbBothInput.setEnabled(false);
        cbBothInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbBothInputActionPerformed(evt);
            }
        });

        panelButtons.add(cbBothInput);

        jPanel6.add(panelButtons, java.awt.BorderLayout.CENTER);

        jPanel4.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        btExit.setText("Exit");
        btExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btExitActionPerformed(evt);
            }
        });

        jPanel4.add(btExit);

        jPanel6.add(jPanel4, java.awt.BorderLayout.EAST);

        add(jPanel6, java.awt.BorderLayout.CENTER);

        jPanel2.setLayout(new java.awt.BorderLayout());

        panelScName.setLayout(new java.awt.BorderLayout());

        lbScName.setText("  Scenario title (.sc extension will be added): ");
        panelScName.add(lbScName, java.awt.BorderLayout.WEST);

        tfScName.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tfScName.setText("defaultScenario");
        panelScName.add(tfScName, java.awt.BorderLayout.CENTER);

        panelScName.add(jPanel5, java.awt.BorderLayout.SOUTH);

        chronoCont.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        chronoCont.setMinimumSize(new java.awt.Dimension(100, 24));
        chronoCont.setPreferredSize(new java.awt.Dimension(180, 24));
        jPanel3.add(chronoCont);

        panelScName.add(jPanel3, java.awt.BorderLayout.NORTH);

        jPanel2.add(panelScName, java.awt.BorderLayout.NORTH);

        panelControls.setLayout(new java.awt.GridLayout(0, 1));

        panelButtonsStatus.setLayout(new java.awt.BorderLayout());

        panelStatus.setLayout(new java.awt.BorderLayout());

        panelStatus.add(jSeparator1, java.awt.BorderLayout.NORTH);

        lbRecordedData.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        lbRecordedData.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lbRecordedData.setText("----");
        lbRecordedData.setMinimumSize(new java.awt.Dimension(54, 13));
        lbRecordedData.setPreferredSize(new java.awt.Dimension(54, 13));
        panelStatus.add(lbRecordedData, java.awt.BorderLayout.EAST);

        lbStatusTitle.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        lbStatusTitle.setText("Status: ");
        jPanel1.add(lbStatusTitle);

        lbStatus.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        lbStatus.setText("paused...");
        jPanel1.add(lbStatus);

        jPanel8.add(jPanel1);

        panelStatus.add(jPanel8, java.awt.BorderLayout.WEST);

        jPanel9.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        lbTranslucency.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        lbTranslucency.setText("Translucency:");
        jPanel9.add(lbTranslucency);

        translucencySlider.setMinimum(60);
        translucencySlider.setValue(100);
        translucencySlider.setPreferredSize(new java.awt.Dimension(100, 24));
        jPanel9.add(translucencySlider);

        panelStatus.add(jPanel9, java.awt.BorderLayout.CENTER);

        panelButtonsStatus.add(panelStatus, java.awt.BorderLayout.SOUTH);

        panelControls.add(panelButtonsStatus);

        jPanel2.add(panelControls, java.awt.BorderLayout.SOUTH);

        add(jPanel2, java.awt.BorderLayout.SOUTH);

    }// </editor-fold>//GEN-END:initComponents

    private void btComparisonFullActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btComparisonFullActionPerformed
        
        if(!validTimerValue(tfCaptureDelay.getText())){
            return;
        }
        
        //launches a comparison event for a Full Screen Capture
        this.captureDelay = Integer.parseInt(tfCaptureDelay.getText());
        int delayInMilli = RemoteControlPanel.this.captureDelay * 1000;
        this.scDelayTriggerTimer.setInitialDelay(delayInMilli);
        this.scDelayTriggerTimer.start();
        this.scDelayUpdateTimer.start();
        try{
            recorder.handleComparison(true, 
                                      delayInMilli);
        }catch(ScreenshotException se){
            LOGGER.log(Level.SEVERE, "Unable to take Screenshot!...", se);
        }                
    }//GEN-LAST:event_btComparisonFullActionPerformed

    private void btExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btExitActionPerformed
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                    btExit.setText("Exiting...");
                    btExit.setEnabled(false);
                    recorder.setClientWindowVisible(false);                    
                    try{
                        Thread.sleep(500);
                        new ExternalWaitWindow().start("Exiting...");
                    }catch(Throwable t){
                        LOGGER.log(Level.WARNING, "Unable to display wait window", t);
                    }                    
                    new Thread(){
                        public void run(){
                            try{
                                recorder.exit();
                            }catch(Exception e){
                                //ignore this
                            }
                            System.exit(0);
                        }
                    }.start();
            }
        });
    }//GEN-LAST:event_btExitActionPerformed

    private void cbBothInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbBothInputActionPerformed
        recorder.setSingleInput(!cbBothInput.isSelected());
    }//GEN-LAST:event_cbBothInputActionPerformed

    private void btComparisonAppActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btComparisonAppActionPerformed
        
        if(!validTimerValue(tfCaptureDelay.getText())){
            return;
        }
        
        //launches a comparison event for an Application Capture
        this.captureDelay = Integer.parseInt(tfCaptureDelay.getText());
        int delayInMilli = RemoteControlPanel.this.captureDelay * 1000;
        this.scDelayTriggerTimer.setInitialDelay(delayInMilli);
        this.scDelayTriggerTimer.start();
        this.scDelayUpdateTimer.start();
        try{
            recorder.handleComparison(false, 
                                      delayInMilli);
        }catch(ScreenshotException se){
            LOGGER.log(Level.SEVERE, "Unable to take Screenshot!...", se);
        }                        
    }//GEN-LAST:event_btComparisonAppActionPerformed

    private void btRecordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btRecordActionPerformed
        //toggles between record mode and pause
        boolean recording = recorder.handleScenarioStart(this.tfScName.getText()); 
        if(recording){
            this.btRecord.setIcon(IMG_STOP);
            this.lbStatus.setText(RECORDING_LABEL);
            this.chrono.reset();
            this.chrono.start();
        }else{
            this.btRecord.setIcon(IMG_RECORD);
            this.lbStatus.setText(IDLE_LABEL);
            this.chrono.pause();
        }
        this.btRecord.validate();
        
        this.btComparisonApp.setEnabled(recording);        
        this.btComparisonFull.setEnabled(recording);        
        this.cbBothInput.setEnabled(recording);        
        this.tfScName.setEnabled(!recording);
        this.btExit.setEnabled(!recording);
    }//GEN-LAST:event_btRecordActionPerformed
    
    public void paint(Graphics g){
        super.paint(g);
    }
    
    public void setStatusLabel(String statusText){
        this.lbStatus.setText(statusText);
    }
    
    public void resetStatusLabel(){
        this.lbStatus.setText(recorder.isRecording()?RECORDING_LABEL:IDLE_LABEL);
    }    

    public void handleNotification(Notification notification, Object o){
        if(notification instanceof ControlNotification){
            ControlNotification contNotif = (ControlNotification) notification;
            if(contNotif.getType().equals(Boolean.TRUE.toString())){
                this.lbStatus.setText(IN_RC_LABEL);
            }else{
                resetStatusLabel();
            }            
        }else if(notification instanceof ScreenshotNotification){
            SwingUtilities.windowForComponent(this).setVisible(true);
            this.chrono.start();
        }
    }
    
    public void addMouseMotionListener(MouseMotionListener mouseMotionListener){
        super.addMouseMotionListener(mouseMotionListener);
        this.cbBothInput.addMouseMotionListener(mouseMotionListener);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btComparisonApp;
    private javax.swing.JButton btComparisonFull;
    private javax.swing.JButton btExit;
    private javax.swing.JButton btRecord;
    private javax.swing.JCheckBox cbBothInput;
    private javax.swing.JPanel chronoCont;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lbRecordedData;
    private javax.swing.JLabel lbScChrono;
    private javax.swing.JLabel lbScName;
    private javax.swing.JLabel lbStatus;
    private javax.swing.JLabel lbStatusTitle;
    private javax.swing.JLabel lbTitle;
    private javax.swing.JLabel lbTranslucency;
    private javax.swing.JPanel panelButtons;
    private javax.swing.JPanel panelButtonsStatus;
    private javax.swing.JPanel panelControls;
    private javax.swing.JPanel panelScName;
    private javax.swing.JPanel panelStatus;
    private javax.swing.JTextField tfCaptureDelay;
    private javax.swing.JTextField tfScName;
    private javax.swing.JSlider translucencySlider;
    // End of variables declaration//GEN-END:variables
    
}
