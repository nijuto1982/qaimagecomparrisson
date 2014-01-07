/*
 * SketchBSample.java
 *
 * Created on 10. novembre 2006, 16:32
 */

package org.jdiffchaser.samples.sketchbook.version2;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JOptionPane;

public class SketchBSample extends javax.swing.JFrame {

    private static final Stroke PEN_STROKE = new BasicStroke(10.0f, 
                                                             BasicStroke.CAP_ROUND,
                                                             BasicStroke.JOIN_ROUND);
    
    private class PaintAdapter implements MouseMotionListener, MouseListener{
        
        private int x, y = 0;
        private PaintPanel paintPanel;
        
        public PaintAdapter(PaintPanel paintPanel){
            super();
            this.paintPanel = paintPanel;
            this.paintPanel.addMouseListener(this);
            this.paintPanel.addMouseMotionListener(this);
        }
        
        public void mouseMoved(MouseEvent e) {
        }
        
        public void mouseDragged(MouseEvent e) {
            Graphics2D g2d = (Graphics2D) this.paintPanel.getBufferGraphics();
            g2d.setColor(Color.BLUE);
            g2d.setStroke(PEN_STROKE);
            g2d.drawLine(x, y , e.getX(), e.getY());
            x = e.getX();
            y = e.getY();
            this.paintPanel.repaint();
        }
        public void mousePressed(MouseEvent e) {
            drawBluePoint(this.paintPanel.getBufferGraphics(), e.getX(), e.getY());
            x = e.getX();
            y = e.getY();
            this.paintPanel.repaint();
        }
        
        public void mouseReleased(MouseEvent e) {
            drawBluePoint(this.paintPanel.getBufferGraphics(), e.getX(), e.getY());
            this.paintPanel.repaint();
        }

        public void mouseClicked(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        private void drawBluePoint(Graphics g, int x, int y){
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(Color.BLUE);
            g2d.setStroke(PEN_STROKE);
            g2d.fillOval(x, y, 11, 11);

        }
    }
    
    /** Creates new form LoginExample */
    public SketchBSample() {
        initComponents();
        this.setName("SketchBSample new frame");
        setSize(800, 600);
        new PaintAdapter((PaintPanel)this.paintPanel);
        System.out.println("Note that this frame.getName() returns '" + this.getName() + '\'');
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        paintPanel = new PaintPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem3 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Paint example");

        paintPanel.setBackground(new java.awt.Color(255, 255, 255));
        getContentPane().add(paintPanel, java.awt.BorderLayout.CENTER);

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.SOUTH);

        jMenu1.setText("File");

        jMenuItem1.setText("New");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuItem2.setText("Exit");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem2);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Help");

        jMenuItem3.setText("About...");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem3);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        JOptionPane.showConfirmDialog(this, "This is SketchBook " + getVersion() + " about box", 
                                      "About...", JOptionPane.OK_OPTION);
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        System.exit(0);
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        ((PaintPanel)this.paintPanel).erase();
        this.paintPanel.repaint();
    }//GEN-LAST:event_jMenuItem1ActionPerformed
       
    public static String getVersion(){
        return " v 2.0 ";
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new SketchBSample().setVisible(true);
            }
        });
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JPanel paintPanel;
    // End of variables declaration//GEN-END:variables
    
}
