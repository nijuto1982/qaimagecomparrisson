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

package org.jdiffchaser.publish;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import org.jdiffchaser.scenarihandling.TestSet;
import org.jdiffchaser.utils.ImageUtilities;
import java.awt.image.RenderedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;


public class HtmlReporter {
    
    private static final Logger LOGGER = Logger.getLogger(HtmlReporter.class.getName());
    
    private static final SimpleDateFormat SDF = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
    
    private static final ImageIcon JDIFFCHASER_LOGO = 
                         new ImageIcon(HtmlReporter.class.getResource("jdiffchaserlogo_xs.png"));    
    private static BufferedImage JDIFFCHASER_BUFF_LOGO;
    static{
        JDIFFCHASER_BUFF_LOGO = new BufferedImage(JDIFFCHASER_LOGO.getIconWidth(),
                                                  JDIFFCHASER_LOGO.getIconHeight(),
                                                  BufferedImage.TYPE_INT_ARGB);
        JDIFFCHASER_BUFF_LOGO.getGraphics().drawImage(JDIFFCHASER_LOGO.getImage(), 0, 0, null);
    }
    
    private String failedDir;
    private int totalTestCount;
    private int totalTestPlayed;
    private int totalTestSucceded;
    private String firstHostComment;
    private String secondHostComment;
    
    public HtmlReporter(String failedDir, 
                        String firstHostComment,
                        String secondHostComment,
                        int totalTestCount, int totalTestPlayed, int totalTestSucceded) {
        
        this.firstHostComment  = firstHostComment;
        this.secondHostComment = secondHostComment;
        this.failedDir         = failedDir;
        this.totalTestCount    = totalTestCount;
        this.totalTestPlayed   = totalTestPlayed;
        this.totalTestSucceded = totalTestSucceded;
    }

    private StringBuffer getReportCss() throws IOException{
        StringBuffer sbuf = new StringBuffer();
        InputStream inputS = HtmlReporter.class.getResourceAsStream("report.css");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputS));
        String line = reader.readLine();
        while(line!=null){
            sbuf.append(line).append('\n');
            line = reader.readLine();
        }
        reader.close();
        return sbuf;
    }

    public void createReport(String title) throws ReportException, IOException, InterruptedException{
        StringBuffer sbuf = new StringBuffer();
        sbuf.append("<HTML>");
        sbuf.append("<HEAD>");
        sbuf.append("<META HTTP-EQUIV=\"CONTENT-TYPE\" CONTENT=\"text/html; charset=windows-1252\">");
        sbuf.append("<TITLE>");
        sbuf.append(title).append("GUI Comparisons");
        sbuf.append("</TITLE>");
        sbuf.append("<STYLE TYPE=\"text/css\">");
        sbuf.append(getReportCss());
        sbuf.append("</STYLE>");
        sbuf.append("</HEAD>");
        sbuf.append("<BODY>");
        sbuf.append("<TABLE border=\"0\" >");
        sbuf.append("   <TR>");
        sbuf.append("       <TD>");
        sbuf.append(createTitleTable(title));
        File failedDirFile = new File(this.failedDir);
        LOGGER.finer("failedDir is " + this.failedDir);
        File[] appsDirs = failedDirFile.listFiles();
        LOGGER.finer("app dirs are " + Arrays.asList(appsDirs));
        boolean success = (appsDirs.length==0);
        sbuf.append(createReportMessageTable(success));
        sbuf.append("       </TD>");
        sbuf.append("   </TR>");
        if(!success){
            sbuf.append(createScenariosLines(appsDirs));
        }

        sbuf.append("</TABLE>");
        sbuf.append("</BODY>");
        sbuf.append("</HTML>");
        try{
            FileWriter fw = new FileWriter(failedDir+'/'+"index.html");
            fw.write(sbuf.toString());
            fw.close();
        }catch(IOException ioe){
            throw new ReportException("Unable to create HTML report file", ioe);
        }
        storeReportLayoutImgs();
    }

    private void storeReportLayoutImgs() throws IOException, InterruptedException{
        String[] imageFiles = new String[]{"cellBg.png", "cellBody.png", "cellBottom.png", "cellTop.png",
                                           "greenSignBg.png", "redSignBg.png", "pageBg.png", "suiteSeparator.png",
                                           "testSeparator.png", "titleBg.png"};
        for(int i=0; i<imageFiles.length; i++){
            BufferedImage image = ImageUtilities.readImage(HtmlReporter.class.getResourceAsStream("img/"+imageFiles[i]));
            String imgPath = failedDir + File.separator + "img/"+imageFiles[i];
            ImageUtilities.storeImage(image, imgPath);
        }
    }

    private StringBuffer createScenariosLines(File[] appsDirs) throws ReportException, IOException, InterruptedException{
        StringBuffer sbuf = new StringBuffer();
        sbuf.append("<TR>");
        sbuf.append("   <TD>");
        sbuf.append("       <TABLE class=\"cell-table\" width=680 cellspacing=0 cellpadding=0>");
        
        for(int i=0; i<appsDirs.length; i++){
            File appDir = appsDirs[i];
            if(!appDir.isDirectory()){ //if index.html already exists, bypass
                continue;
            }
            sbuf.append("       <TR class=\"celltop\" height=49>");
    	    sbuf.append("           <TD COLSPAN=3>").append(appDir.getName()).append("&nbsp;&nbsp;</TD>");
	        sbuf.append("       </TR>");

            File[] scenariiDirs = appDir.listFiles();
            LOGGER.finer("app dir is " + appDir);
            LOGGER.finer("Scenariis dirs are " + Arrays.asList(scenariiDirs));
            for(int j=0; j<scenariiDirs.length; j++){

                File scenarioDir = scenariiDirs[j];

                File scenarioThumbnailsDir = new File(scenarioDir.getPath() + File.separator + "thumbnails");
                scenarioThumbnailsDir.mkdir();

                sbuf.append("   <TR class=\"cellbody\">");
    	        sbuf.append("       <TD width=380 >");

                sbuf.append(scenarioDir.getName().substring(0, scenarioDir.getName().indexOf('(')));
                sbuf.append("<BR>");
                sbuf.append(scenarioDir.getName().substring(scenarioDir.getName().indexOf('(')));

                sbuf.append("       </TD>");
    	        sbuf.append("       <TD width=80>");

                File[] images = scenarioDir.listFiles();
                LOGGER.finer("scenario dir is " + scenarioDir.getPath());
                LOGGER.finer("Images are " + Arrays.asList(images));
                File diffImg = findFile(images, TestSet.DIFF_IMG_NAME + '.' + TestSet.IMG_EXTENSION);

                List imagesWithoutDiff = getListWithout(images, diffImg.getName());

                String image1Base  = ((File)imagesWithoutDiff.get(0)).getName();
                String image2Base  = ((File)imagesWithoutDiff.get(1)).getName();
                String diffImgBase = diffImg.getName();

                String image1Th      = image1Base.substring(0, image1Base.lastIndexOf('.'))
                                                                                    + "Th." + TestSet.IMG_EXTENSION;
                String image2Th      = image2Base.substring(0, image2Base.lastIndexOf('.'))
                                                                                    + "Th." + TestSet.IMG_EXTENSION;
                String diffImgNameTh = diffImgBase.substring(0, diffImgBase.lastIndexOf('.'))
                                                                                    + "Th." + TestSet.IMG_EXTENSION;

                storeScaledImageFile(ImageUtilities.readImage((File)imagesWithoutDiff.get(0)),
                                     image1Th, scenarioThumbnailsDir, 80, 48);

                storeScaledImageFile(ImageUtilities.readImage((File)imagesWithoutDiff.get(1)),
                                     image2Th, scenarioThumbnailsDir, 80, 48);

                storeScaledImageFile(ImageUtilities.readImage(diffImg),
                                     diffImgNameTh, scenarioThumbnailsDir, 160, 104);

                String bigImagesDir  = appDir.getName() + '/' + scenarioDir.getName() + '/';
                String thumbnailsDir = bigImagesDir + scenarioThumbnailsDir.getName() + '/';

                String linkAname = String.valueOf(i) + String.valueOf(j);

                createMagnifyHtmlFile(failedDir+'/'+bigImagesDir, scenarioDir.getName(), null, image1Base, image2Base, linkAname);
                createMagnifyHtmlFile(failedDir+'/'+bigImagesDir, scenarioDir.getName(), image1Base, image2Base, diffImgBase, linkAname);
                createMagnifyHtmlFile(failedDir+'/'+bigImagesDir, scenarioDir.getName(), image2Base, diffImgBase, null, linkAname);

                sbuf.append("<a name=\"").append(linkAname).append("\"");
                sbuf.append("href=\"").append(bigImagesDir).append(toHtmlFilename(image1Base));
                sbuf.append("\"><img src=\"").append(thumbnailsDir).append(image1Th);
                sbuf.append("\" border=\"0\" width=\"80\" alt=\"image1\"></a>");

                sbuf.append("       </TD>");
                sbuf.append("       <TD rowspan=2 width=160>");

                sbuf.append("<a href=\"").append(bigImagesDir).append(toHtmlFilename(diffImgBase));
                sbuf.append("\"><img src=\"").append(thumbnailsDir).append(diffImgNameTh);
                sbuf.append("\" border=\"0\" width=\"160\" alt=\"image of diffs\"></a>");

                sbuf.append("       </TD>");
                sbuf.append("   </TR>");
                sbuf.append("   <TR class=\"cellbody\">");
                sbuf.append("       <TD width=380 >&nbsp;</TD>");
                sbuf.append("       <TD width=80>");

                sbuf.append("<a href=\"").append(bigImagesDir).append(toHtmlFilename(image2Base));
                sbuf.append("\"><img src=\"").append(thumbnailsDir).append(image2Th);
                sbuf.append("\" border=\"0\" width=\"80\" alt=\"image2\"></a>");

                sbuf.append("       </TD>");
                sbuf.append("   </TR>");

                if(j<scenariiDirs.length-1){
                    sbuf.append("   <TR class=\"cellbody\" height=40>");
                    sbuf.append("       <TD colspan=3><img src=\"img/testSeparator.png\"></TD>");
                    sbuf.append("   </TR>");
                }else{
                    sbuf.append("   <TR class=\"cellbottom\" height=36>");
                    sbuf.append("       <TD COLSPAN=3 width=\"100%\">&nbsp;</TD>");
                    sbuf.append("   </TR>");
                }

            }

            if(i<appsDirs.length-1){
                sbuf.append("   <TR class=\"suiteSeparator\" height=40>");
                sbuf.append("       <TD colspan=3><img src=\"img/suiteSeparator.png\"></TD>");
                sbuf.append("   </TR>");
            }
        }
        sbuf.append("       </TABLE>");
        sbuf.append("   </TD>");
        sbuf.append("</TR>");
        return sbuf;
    }

    private StringBuffer createTitleTable(String title){
        StringBuffer sbuf = new StringBuffer();
        sbuf.append("<TABLE border=\"0\" class=\"title-table\">");
        sbuf.append("   <TR>");
        sbuf.append("       <TD COLSPAN=3>&nbsp;</TD>");
        sbuf.append("   </TR>");
        sbuf.append("   <TR>");
        sbuf.append("       <TD width=15>&nbsp;</TD>");
        sbuf.append("       <TD class=\"test-session-title-cell\">");
        sbuf.append("           <H1>").append(title).append("<br>GUI Comparisons</H1>");
        sbuf.append("           <H2>").append(SDF.format(new Date())).append("</H2>");
        sbuf.append("       <TD>&nbsp;</TD>");
        sbuf.append("   </TR>");
        sbuf.append("   <TR>");
        sbuf.append("       <TD width=15>&nbsp;</TD>");
        sbuf.append("       <TD COLSPAN=2 class=\"test-session-versions\">");
        sbuf.append("           Client #1 is ").append(this.firstHostComment).append("<br>");
        sbuf.append("           Client #2 is ").append(this.secondHostComment);
        sbuf.append("       </TD>");
        sbuf.append("   </TR>");
        sbuf.append("</TABLE>");
        return sbuf;
    }

    private StringBuffer createReportMessageTable(boolean success){
        StringBuffer sbuf = new StringBuffer();
        sbuf.append("<TABLE border=\"0\" class=\"sign-table ").append(success?"green":"red").append("\">");
        sbuf.append("   <TR>");
        sbuf.append("       <TD width=120>&nbsp;</TD>");
        if(success){
            sbuf.append("   <TD><h2>Congratulations, no differences detected!</h2></TD>");
        }else{
            int failures = this.totalTestPlayed - this.totalTestSucceded;
            sbuf.append("   <TD><h2>- Expected to play ").append(this.totalTestCount).append(" tests -<br>");
            sbuf.append("       <h2>").append(failures).append(" different screen").append(failures>1?"s":"").append(" detected among ");
            sbuf.append(this.totalTestPlayed).append(" test").append(this.totalTestPlayed>1?"s":"").append(" played</h2>");
        }
        sbuf.append("       </TD>");
        sbuf.append("       <TD width=120>&nbsp;</TD>");
        sbuf.append("   </TR>");
        sbuf.append("   <TR>");
        sbuf.append("       <TD COLSPAN=2>&nbsp;</TD>");
        sbuf.append("   </TR>");
        sbuf.append("</TABLE>");
        return sbuf;
    }

        
    private void createMagnifyHtmlFile(String toDir,
                                       String scenarioName,
                                       String previousImgFilename, 
                                       String currentImgFilename,
                                       String nextImgFilename,
                                       String backLinkAname) throws IOException{
        FileWriter fw = new FileWriter(toDir+toHtmlFilename(currentImgFilename));
        fw.write(createMagnifyHtmlContent(scenarioName, previousImgFilename, currentImgFilename, nextImgFilename, backLinkAname));
        fw.close();
    }
    
    public static String createMagnifyHtmlContent(String scenarioName,
                                                  String previousImgFilename, 
                                                  String currentImgFilename,
                                                  String nextImgFilename,
                                                  String backLinkAname){
    String toRet =  
        "<html>"
        + "\n<title>"
        + "\n" + scenarioName + ": " + currentImgFilename
        + "\n</title>"
        + "\n<body>"
        + "\n<center><a href=\"../../index.html#" + backLinkAname + "\">back to report</a></center>"
        + "\n<center>" + scenarioName + ": " + currentImgFilename + "</center>"             
        + "\n<table border=\"0\" width=\"100%\">"
        + "\n<tr>"
        + "\n<td><center>"
        + (previousImgFilename!=null? "<a href=\"" + toHtmlFilename(previousImgFilename) + "\">Previous</a>" : "")
        + "&nbsp;&nbsp;"
        + (nextImgFilename!=null?"<a href=\"" + toHtmlFilename(nextImgFilename) + "\">Next</a>" : "")
        + "</center></td>"
        + "\n</tr>"
        + "\n<tr>"
        + "\n<td><center><img src=\"" + currentImgFilename + "\" border=\"0\"></center></td>"
        + "\n</tr>"
        + "\n<tr>"
        + "\n<td><center>"
        + (previousImgFilename!=null? "<a href=\"" + toHtmlFilename(previousImgFilename) + "\">Previous</a>": "")
        + "&nbsp;&nbsp;"
        + (nextImgFilename!=null?"<a href=\"" + toHtmlFilename(nextImgFilename) + "\">Next</a>" : "")
        + "</center></td>"
        + "\n</tr>"
        + "\n</table>"
        + "\n</body>"
        + "\n</html>";
    
        return toRet;
    }
    
    private static String toHtmlFilename(String imageName){
        return imageName.substring(0, imageName.indexOf('.')) + ".html";
    }
    
    private void storeScaledImageFile(RenderedImage srcImg, String filename, 
                                      File toDir, int width, int height) throws IOException{
        String imgPath = toDir.getPath() + File.separator + filename;
        ImageUtilities.storeImage(ImageUtilities.getScaledInstance(srcImg, width, height, true), imgPath); 
    }
    
    private static List getListWithout(File[] files, String name){
        List list = new ArrayList();
        for(int i=0; i<files.length; i++){
            if(!name.equals(files[i].getName())){
                list.add(files[i]);
            }
        }
        return list;
    }
    
    private static File findFile(File[] files, String name){
        File fileToReturn = null;
        for(int i=0; i<files.length; i++){
            if(name.equals(files[i].getName())){
                fileToReturn = files[i];
                break;
            }
        }
        return fileToReturn;
    }
    
}
