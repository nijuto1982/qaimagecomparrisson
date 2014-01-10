/**
 * 
 */
package de.asideas.qaimagecomparrisson.tklosek.main;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JFileChooser;

import org.jdiffchaser.imgprocessing.ImageComparator;
import org.jdiffchaser.imgprocessing.Screenshot;
import org.jdiffchaser.utils.ImageUtilities;

import de.asideas.qaimagecomparrisson.tklosek.screenshotmaker.IScreenshot;
import de.asideas.qaimagecomparrisson.tklosek.screenshotmaker.ScreenshotFirefox;

/**
 * @author Tobias Klosek
 * 
 */
public class Main {

	private static final String SCREENSHOTPATH = "screenshots";
	private static final String URL = "http://www.welt.de/";
	private static final String REFERENCESCREENSHOTFF = "refScreenshFF.png";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Date dNow = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("yyyyMMdd-HHmmss");
		String currentDateTimeformatted = ft.format(dNow);
		System.out.println("Current Date: " + currentDateTimeformatted);
		// TODO Auto-generated method stub

		// IScreenshot screenshotChrome = new ScreenshotChrome(SCREENSHOTPATH,
		// "testInstsantziertChrome", URL); //works
		boolean takeReferenceScreenshot = true;
		
		if (takeReferenceScreenshot) {
			new ScreenshotFirefox(SCREENSHOTPATH, REFERENCESCREENSHOTFF, URL);
		}

		final IScreenshot currentScreenshotFF = new ScreenshotFirefox(SCREENSHOTPATH, currentDateTimeformatted + "_currentScreenshotFF.png", URL);
		processImageComparrison(currentScreenshotFF);

	}

	/**
	 * @param currentScreenshot
	 * @param referenceScreenshotFile
	 * @throws Exception
	 * @throws IOException
	 */
	private static void processImageComparrison(final IScreenshot currentScreenshot) {
		File referenceScreenshotFile = new File(SCREENSHOTPATH + "/" + REFERENCESCREENSHOTFF);
		File currentScreenshotAsFile = currentScreenshot.getScreenshotAsFile();
		String currentScreenshotFilename = currentScreenshotAsFile.getName();
		String referenceScreenshotFilename = referenceScreenshotFile.getName();

		try {

			Image referenceScreenshotImage = Screenshot.loadImageFromFile(referenceScreenshotFile);
			Image currentScreenshotImage = Screenshot.loadImageFromFile(currentScreenshotAsFile);

			ImageComparator imageComparator = ImageComparator.getInstance(0.6, referenceScreenshotImage,
					"Reference Screenshot: " + referenceScreenshotFilename, currentScreenshotImage, "compare with: " + currentScreenshotFilename);

			imageComparator.compareImages(false);// try with false to avoid
													// animations...

			String absolutePathFilename = referenceScreenshotFile.getAbsolutePath();
			String absolutePath = absolutePathFilename.substring(0, absolutePathFilename.lastIndexOf(File.separator));

			System.out.println(absolutePath);

			JFileChooser fileChooser = new JFileChooser(System.getProperty(absolutePath));

			// set the filechooser directory path same like
			// referenceScreenshotFile
			fileChooser.setCurrentDirectory(referenceScreenshotFile);
			// fileChooser.setCurrentDirectory(new
			// File(System.getProperty("user.dir")));

			// ImageUtilities.storeImage(imageComparator.getLastResultImage(),
			// new File("diffs_" +
			// currentScreenshotFilename).getAbsolutePath());

			fileChooser.setSelectedFile(new File("diffs_" + currentScreenshotFilename));
			if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
				ImageUtilities.storeImage(imageComparator.getLastResultImage(), fileChooser.getSelectedFile().getPath());
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.exit(0);
	}
}