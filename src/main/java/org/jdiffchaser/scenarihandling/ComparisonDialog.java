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

import org.jdiffchaser.scenarihandling.events.ComparisonEvent;
import org.jdiffchaser.imgprocessing.Screenshot;
import org.jdiffchaser.imgprocessing.ScreenshotException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

public class ComparisonDialog extends javax.swing.JDialog {

    private static final int EMPTY_WIDTH  = 800;
    private static final int EMPTY_HEIGHT = 600;

    private Component eventSource;
    private ComparisonEvent comparisonEvent;
    
    private static ComparisonDialog instance;
    
    private boolean fullScreen;
            
    private ScreenshotPanel screenshotPanel;
    private JScrollPane     scrollPane;

    private String lastOpenedPath;
    
    /** Creates new form BeanForm */
    private ComparisonDialog(Component eventSource) {
        initComponents();
        this.setModal(true);
        this.eventSource = eventSource;
        this.scrollPane = new JScrollPane();
        this.getContentPane().add(scrollPane, BorderLayout.CENTER);
        this.scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        this.scrollPane.getHorizontalScrollBar().setUnitIncrement(20);
        this.scrollPane.getHorizontalScrollBar().setBlockIncrement(60);
        this.scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        this.scrollPane.getVerticalScrollBar().setBlockIncrement(60);
    }

    private BufferedImage createEmptyBlackImage(int x, int y){
        BufferedImage bgImage = new BufferedImage(x,
                                                  y,
                                                  BufferedImage.TYPE_INT_ARGB);
        Graphics g = bgImage.getGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, x, y);
        return bgImage;
    }

    private void loadComparisonEvent(ComparisonEvent compEvent) {
        Point lowerRighter = compEvent.getLowerRighter();
        if(this.screenshotPanel!=null){
            Dimension panelSize = this.screenshotPanel.getSize();
            if( (panelSize.width < lowerRighter.x)
                || (panelSize.height < lowerRighter.y) ){
                BufferedImage bgImage = createEmptyBlackImage(lowerRighter.x, lowerRighter.y);
                this.addImageToDialog(bgImage);
                JOptionPane.showMessageDialog(this,
                                              "Background has been created because old one had different size",
                                              "Information",
                                              JOptionPane.INFORMATION_MESSAGE);
            }
        }else{
            BufferedImage bgImage = createEmptyBlackImage(lowerRighter.x, lowerRighter.y);
            this.addImageToDialog(bgImage);
            this.pack();
        }
        this.screenshotPanel.setIgnoredZones(compEvent.getIgnoredZones());
    }

    public static ComparisonEvent show(Component eventSource) throws ScreenshotException{
        synchronized(ComparisonDialog.class){
            if(instance==null){
                instance = new ComparisonDialog(eventSource);
            }else{
                instance.eventSource = eventSource;
            }
        }
        instance.okCancelButtonsPanel.setVisible(false);
        instance.jFileMenu.setVisible(true);
        instance.setTitle(".sc/.ign editor");
        instance.setSize(EMPTY_WIDTH, EMPTY_HEIGHT);
        instance.setVisible(true);
        instance.setResizable(true);
        return instance.comparisonEvent;
    }
    
    public static ComparisonEvent show(Component eventSource, boolean fullScreen) throws ScreenshotException{
        synchronized(ComparisonDialog.class){
            if(instance==null){
                instance = new ComparisonDialog(eventSource);
            }else{
                instance.eventSource = eventSource;
            }
        }
        instance.okCancelButtonsPanel.setVisible(true);
        instance.jFileMenu.setVisible(false);
        instance.setTitle("Screenshot that will be used for comparison");
        BufferedImage img = instance.takeScreenshotFromSource(eventSource, fullScreen);
        instance.addImageToDialog(img);
        instance.pack();
        instance.setVisible(true);
        instance.setResizable(true);
        return instance.comparisonEvent;
    }
    
    private BufferedImage takeScreenshotFromSource(Component eventSource, boolean fullScreen) throws ScreenshotException{
        
        this.fullScreen = fullScreen;
        BufferedImage imgBuf = Screenshot.createScreenCapture(eventSource, fullScreen);

        return imgBuf;
    }

    private void setToolsEnabled(boolean enabled){
        this.recToolButton.setEnabled(enabled);
        this.ellipseToolButton.setEnabled(enabled);
        this.eraserToolButton.setEnabled(enabled);
    }

    private void addImageToDialog(BufferedImage imgBuf){
        if(this.screenshotPanel!=null){
            this.scrollPane.getViewport().remove(this.screenshotPanel);
        }
        this.screenshotPanel = new ScreenshotPanel(imgBuf, true);
        this.screenshotPanel.setPreferredSize(new Dimension(imgBuf.getWidth(), imgBuf.getHeight()));
        this.scrollPane.getViewport().add(screenshotPanel);
        this.setToolsEnabled(true);
        this.pack();
        this.screenshotPanel.revalidate();
        this.screenshotPanel.repaint();
    }    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        toolsButtonGroup = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        recToolButton = new javax.swing.JToggleButton();
        ellipseToolButton = new javax.swing.JToggleButton();
        eraserToolButton = new javax.swing.JToggleButton();
        jPanel2 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        lbLoadedZonesTitle = new javax.swing.JLabel();
        lbLoadedZonesValue = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        lbLoadedImageTitle = new javax.swing.JLabel();
        lbLoadedImageValue = new javax.swing.JLabel();
        okCancelButtonsPanel = new javax.swing.JPanel();
        btCancel = new javax.swing.JButton();
        btOk = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jFileMenu = new javax.swing.JMenu();
        jFileImportPngMenuItem = new javax.swing.JMenuItem();
        jFileScenarioMenuItem = new javax.swing.JMenuItem();
        jSaveAsIgnMenuItem = new javax.swing.JMenuItem();
        jInjectIgnIntoScenarioMenuItem = new javax.swing.JMenuItem();
        jExitMenuItem = new javax.swing.JMenuItem();

        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel3.setLayout(new java.awt.BorderLayout());

        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        toolsButtonGroup.add(recToolButton);
        recToolButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdiffchaser/scenarihandling/rect-add.png"))); // NOI18N
        recToolButton.setSelected(true);
        recToolButton.setEnabled(false);
        recToolButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                recToolButtonActionPerformed(evt);
            }
        });
        jPanel4.add(recToolButton);

        toolsButtonGroup.add(ellipseToolButton);
        ellipseToolButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdiffchaser/scenarihandling/ellipse-add.png"))); // NOI18N
        ellipseToolButton.setEnabled(false);
        ellipseToolButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ellipseToolButtonActionPerformed(evt);
            }
        });
        jPanel4.add(ellipseToolButton);

        toolsButtonGroup.add(eraserToolButton);
        eraserToolButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdiffchaser/scenarihandling/edit-clear.png"))); // NOI18N
        eraserToolButton.setEnabled(false);
        eraserToolButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eraserToolButtonActionPerformed(evt);
            }
        });
        jPanel4.add(eraserToolButton);

        jPanel7.add(jPanel4);

        jPanel3.add(jPanel7, java.awt.BorderLayout.WEST);

        jPanel2.setLayout(new java.awt.GridLayout(2, 1));

        jPanel5.setLayout(new java.awt.BorderLayout());

        lbLoadedZonesTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbLoadedZonesTitle.setText("Loaded zones: ");
        jPanel5.add(lbLoadedZonesTitle, java.awt.BorderLayout.WEST);

        lbLoadedZonesValue.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lbLoadedZonesValue.setText("-");
        lbLoadedZonesValue.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jPanel5.add(lbLoadedZonesValue, java.awt.BorderLayout.CENTER);

        jPanel2.add(jPanel5);

        jPanel6.setLayout(new java.awt.BorderLayout());

        lbLoadedImageTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbLoadedImageTitle.setText("Loaded image: ");
        jPanel6.add(lbLoadedImageTitle, java.awt.BorderLayout.WEST);

        lbLoadedImageValue.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lbLoadedImageValue.setText("-");
        lbLoadedImageValue.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jPanel6.add(lbLoadedImageValue, java.awt.BorderLayout.CENTER);

        jPanel2.add(jPanel6);

        jPanel3.add(jPanel2, java.awt.BorderLayout.CENTER);

        jPanel1.add(jPanel3, java.awt.BorderLayout.CENTER);

        btCancel.setMnemonic('C');
        btCancel.setText("Cancel");
        btCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btCancelActionPerformed(evt);
            }
        });
        okCancelButtonsPanel.add(btCancel);

        btOk.setMnemonic('K');
        btOk.setText("Ok");
        btOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btOkActionPerformed(evt);
            }
        });
        okCancelButtonsPanel.add(btOk);

        jPanel1.add(okCancelButtonsPanel, java.awt.BorderLayout.EAST);

        getContentPane().add(jPanel1, java.awt.BorderLayout.NORTH);

        jFileMenu.setText("File");

        jFileImportPngMenuItem.setText("Load .png...");
        jFileImportPngMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFileImportPngMenuItemActionPerformed(evt);
            }
        });
        jFileMenu.add(jFileImportPngMenuItem);

        jFileScenarioMenuItem.setText("Load .sc or .ign...");
        jFileScenarioMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFileScenarioMenuItemActionPerformed(evt);
            }
        });
        jFileMenu.add(jFileScenarioMenuItem);

        jSaveAsIgnMenuItem.setText("Save as .ign...");
        jSaveAsIgnMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jSaveAsIgnMenuItemActionPerformed(evt);
            }
        });
        jFileMenu.add(jSaveAsIgnMenuItem);

        jInjectIgnIntoScenarioMenuItem.setText("Inject zones to scenario...");
        jInjectIgnIntoScenarioMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jInjectIgnIntoScenarioMenuItemActionPerformed(evt);
            }
        });
        jFileMenu.add(jInjectIgnIntoScenarioMenuItem);

        jExitMenuItem.setText("Exit");
        jExitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jExitMenuItemActionPerformed(evt);
            }
        });
        jFileMenu.add(jExitMenuItem);

        jMenuBar1.add(jFileMenu);

        setJMenuBar(jMenuBar1);
    }// </editor-fold>//GEN-END:initComponents

    private void btCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCancelActionPerformed
        if(this.screenshotPanel!=null){
            this.screenshotPanel.resetIgnoredAreas();
        }
        this.comparisonEvent = null;
        this.setVisible(false);
    }//GEN-LAST:event_btCancelActionPerformed

    private ComparisonEvent createComparisonEvent(){
        return new ComparisonEvent(this.eventSource,
                                   Integer.MIN_VALUE,
                                   this.screenshotPanel.getIgnoredZones(),
                                   this.fullScreen);   
    }

    private void btOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btOkActionPerformed

        this.comparisonEvent = createComparisonEvent();
        this.setVisible(false);
        
    }//GEN-LAST:event_btOkActionPerformed

    private void eraserToolButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eraserToolButtonActionPerformed
        this.screenshotPanel.setToolMode(IgnoredAreaMouseMotionAdapter.ERASE_MODE);
}//GEN-LAST:event_eraserToolButtonActionPerformed

    private void recToolButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recToolButtonActionPerformed
        this.screenshotPanel.setToolMode(IgnoredAreaMouseMotionAdapter.RECTANGLE_MODE);
    }//GEN-LAST:event_recToolButtonActionPerformed

    private void ellipseToolButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ellipseToolButtonActionPerformed
        this.screenshotPanel.setToolMode(IgnoredAreaMouseMotionAdapter.ELLIPSE_MODE);
    }//GEN-LAST:event_ellipseToolButtonActionPerformed

    private void jSaveAsIgnMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSaveAsIgnMenuItemActionPerformed
        saveToIgnFile();
}//GEN-LAST:event_jSaveAsIgnMenuItemActionPerformed

    private void jFileImportPngMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFileImportPngMenuItemActionPerformed
        try{
            chooseBackgroundImg();
        }catch(Exception e){
            e.printStackTrace(); //old school ;p
        }
    }//GEN-LAST:event_jFileImportPngMenuItemActionPerformed

    private void jFileScenarioMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFileScenarioMenuItemActionPerformed
        try{
            chooseScenarioOrIgnFile();
        }catch(Exception e){
            e.printStackTrace(); //old school ;p
        }
    }//GEN-LAST:event_jFileScenarioMenuItemActionPerformed

    private void jExitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jExitMenuItemActionPerformed
        System.exit(0);
    }//GEN-LAST:event_jExitMenuItemActionPerformed

    private void jInjectIgnIntoScenarioMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jInjectIgnIntoScenarioMenuItemActionPerformed
        injectToScenarioFile();
}//GEN-LAST:event_jInjectIgnIntoScenarioMenuItemActionPerformed

    private void injectToScenarioFile(){
        String dir = lastOpenedPath==null?System.getProperty("user.dir"):lastOpenedPath;
        JFileChooser fileChooser = new JFileChooser(dir);
        fileChooser.setDialogTitle("Choose the exported layer filename");
        fileChooser.setSelectedFile(new File("commonZonesToHide.ign"));
        int option = fileChooser.showSaveDialog(this);
        if(option == JFileChooser.APPROVE_OPTION
           && fileChooser.getSelectedFile()!=null){
            this.lastOpenedPath = fileChooser.getSelectedFile().getAbsolutePath();
            ScenarioEditor.putIgnoredZonesIntoScenario(this.screenshotPanel.getIgnoredZones(),
                                        fileChooser.getSelectedFile());

        }
    }


    private void saveToIgnFile(){
        String dir = lastOpenedPath==null?System.getProperty("user.dir"):lastOpenedPath;
        JFileChooser fileChooser = new JFileChooser(dir);
        fileChooser.setDialogTitle("Choose the exported layer filename");
        fileChooser.setSelectedFile(new File("commonZonesToHide.ign"));
        int option = fileChooser.showSaveDialog(this);
        if(option == JFileChooser.APPROVE_OPTION
           && fileChooser.getSelectedFile()!=null){
            try {
                saveComparisonLayerTo(createComparisonEvent(), fileChooser.getSelectedFile());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void chooseScenarioOrIgnFile() throws Exception{
        String dir = lastOpenedPath==null?System.getProperty("user.dir"):lastOpenedPath;
        JFileChooser fileChooser = new JFileChooser(dir);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setDialogTitle("Choose your .sc or .ign file");
        int option = fileChooser.showOpenDialog(this);
        final File file = fileChooser.getSelectedFile();
        if(option==JFileChooser.APPROVE_OPTION && file!=null){
            ComparisonEvent compEvent = null;
            if(file.getName().endsWith(".sc")
               || file.getName().endsWith(".sc.bak")){
                compEvent = ScenarioEditor.extractComparison(file);
            }else if(file.getName().endsWith(".ign")){
                compEvent = ComparisonEvent.loadFromFile(file);
            }else{
                System.out.println("No file selected");
                return;
            }

            SwingUtilities.invokeLater(new Runnable(){
                public void run() {
                    ComparisonDialog.this.lastOpenedPath = file.getAbsolutePath();
                    ComparisonDialog.this.lbLoadedZonesValue.setText(file.getAbsolutePath());
                    ComparisonDialog.this.lbLoadedZonesValue.setToolTipText(file.getAbsolutePath());
                }
            });
            loadComparisonEvent(compEvent);
        }
    }


    private void chooseBackgroundImg() throws IOException{
        String dir = lastOpenedPath==null?System.getProperty("user.dir"):lastOpenedPath;
        JFileChooser fileChooser = new JFileChooser(dir);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setDialogTitle("Choose your reference image");
        int option = fileChooser.showOpenDialog(this);
        final File imageFile = fileChooser.getSelectedFile();
        if(option==JFileChooser.APPROVE_OPTION && imageFile!=null){
            SwingUtilities.invokeLater(new Runnable(){
                public void run() {
                    ComparisonDialog.this.lastOpenedPath = imageFile.getAbsolutePath();
                    ComparisonDialog.this.lbLoadedImageValue.setText(imageFile.getAbsolutePath());
                    ComparisonDialog.this.lbLoadedImageValue.setToolTipText(imageFile.getAbsolutePath());
                }
            });

            this.addImageToDialog(ImageIO.read(imageFile));
        }
    }

    public static void main(String args[]){
        try{
            JFrame  frame = new JFrame("Comparison Layer Editor");
            ComparisonEvent event = ComparisonDialog.show(frame);
            if(event!=null){
                System.out.println("Comparison Layer is : \n" + event);

            }
            System.exit(0);
        }catch(Throwable t){
            t.printStackTrace();
        }
    }
    
    private static void saveComparisonLayerTo(ComparisonEvent event, File file) throws IOException{
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
        out.writeObject(event);
        out.close();
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btCancel;
    private javax.swing.JButton btOk;
    private javax.swing.JToggleButton ellipseToolButton;
    private javax.swing.JToggleButton eraserToolButton;
    private javax.swing.JMenuItem jExitMenuItem;
    private javax.swing.JMenuItem jFileImportPngMenuItem;
    private javax.swing.JMenu jFileMenu;
    private javax.swing.JMenuItem jFileScenarioMenuItem;
    private javax.swing.JMenuItem jInjectIgnIntoScenarioMenuItem;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JMenuItem jSaveAsIgnMenuItem;
    private javax.swing.JLabel lbLoadedImageTitle;
    private javax.swing.JLabel lbLoadedImageValue;
    private javax.swing.JLabel lbLoadedZonesTitle;
    private javax.swing.JLabel lbLoadedZonesValue;
    private javax.swing.JPanel okCancelButtonsPanel;
    private javax.swing.JToggleButton recToolButton;
    private javax.swing.ButtonGroup toolsButtonGroup;
    // End of variables declaration//GEN-END:variables
    
}
