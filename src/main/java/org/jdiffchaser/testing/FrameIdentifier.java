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

import org.jdiffchaser.gui.BalloonManager;
import java.awt.AWTEvent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import org.jdiffchaser.gui.FrameFacilities;
import org.jdiffchaser.gui.FrameNotFoundException;

public class FrameIdentifier {
    
    private static final Logger LOGGER = Logger.getLogger(FrameIdentifier.class.getName());    
    
    private static JWindow balloon      = null;
    private static final JLabel CONTENT = new JLabel();
    
    static{
        CONTENT.setIconTextGap(10);
        CONTENT.setBorder(new EmptyBorder(0, 8, 0, 8));
        CONTENT.setPreferredSize(new Dimension(500,150));
        CONTENT.setIcon(new InfoIcon());
        CONTENT.setFont(new Font("Arial",Font.PLAIN, 12));        
    }
    
    private static final int ICON_SIZE = 48;
    private static class InfoIcon implements Icon {
        public int getIconHeight() {
            return ICON_SIZE;
        }
        public int getIconWidth() {
            return ICON_SIZE;
        }
        public void paintIcon(Component c, Graphics graphics, int x, int y) {
            Font font = UIManager.getFont("TextField.font");
            Graphics2D g = (Graphics2D)graphics.create(x, y, getIconWidth(), getIconHeight());
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setFont(font.deriveFont(Font.BOLD, getIconWidth()*3/4));
            g.setColor(Color.green.darker());
            final int SW = Math.max(getIconWidth()/10, 4);
            g.setStroke(new BasicStroke(SW));
            g.drawArc(SW/2, SW/2, getIconWidth()-SW-1, getIconHeight()-SW-1, 0, 360);
            Rectangle2D bounds = 
                font.getStringBounds("i", g.getFontRenderContext());
            g.drawString("i", Math.round((getIconWidth() - bounds.getWidth())/2 - getIconWidth()/12), 
                         SW/2 + Math.round((getIconHeight()-bounds.getHeight())/2 - bounds.getY() + getIconHeight()/8));
            g.dispose();
        }
    }
      
    public static void addBalloon() throws FrameNotFoundException{
        //here to fix some weird behaviour implying SharedOwnerFrame from SwingUtilities that can
        //shift from 1 all frame default naming (frame0 becomes frame1... strange)
        FrameFacilities.findClientWindow("dummy");

        Toolkit.getDefaultToolkit().addAWTEventListener(
        new AWTEventListener() {
            public void eventDispatched(AWTEvent e) {
                if(e instanceof MouseEvent){
                    MouseEvent me = (MouseEvent)e;  
                    int x = me.getX()+ ((Component)me.getSource()).getLocationOnScreen().x;
                    int y = me.getY()+ ((Component)me.getSource()).getLocationOnScreen().y;
                    if(balloon==null){
                        balloon = BalloonManager.getBalloonWindow(null , CONTENT, 
                                                                  x, y, 
                                                                  Boolean.getBoolean("useDropShadow"));
                        balloon.setAlwaysOnTop(true);
                    }
                    Window w = SwingUtilities.windowForComponent((Component)me.getSource());
                    if(w!=null){
                        balloon.setVisible(true);
                        CONTENT.setText("<html><center>This window getName() method returns : <br><b>'" + w.getName() + "</b>'"
                                        + "<br>Try to use this name in the jDiffChaser Recorder and Player"
                                        + "<br><b>If it doesn't work, try another frame name among: </b><br>" + FrameFacilities.getDisplayedFramesString()
                                        + "</center></html>");
                    }else{
                        balloon.setVisible(false);
                    }
                    BalloonManager.setWindowLocation(null, balloon, x, y);
                    
                    LOGGER.info(FrameFacilities.getDisplayedFramesString());
                }
            }
        }, AWTEvent.MOUSE_MOTION_EVENT_MASK);

    }
    
    public static void main(String[] args){
        try{

            if(args.length<1){
                System.out.println("Usage: FrameIdentifier [MainClassName] [ [App Arg1] [App Arg2] [...]]");
                System.exit(0);
            }
            
            System.setProperty("sun.java2d.noddraw", "true"); 
            
            Class mainClass = Class.forName(args[0]);
            String[] mainArgs = new String[args.length - 1];
            System.arraycopy(args, 1, mainArgs, 0, args.length - 1);
            Method mainMethod = mainClass.getMethod("main", new Class[]{String[].class});
            System.out.println("Invoking main with " + Arrays.asList(mainArgs));
            mainMethod.invoke(null, new Object[]{mainArgs});
            
            addBalloon();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
