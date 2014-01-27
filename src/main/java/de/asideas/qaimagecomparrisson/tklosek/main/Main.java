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

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
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

	private static String screenshotpath;
	private static String refsreenshff;
	private static String url;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		setupConfig();

		String currentDateTimeformatted = setupDateFormat();

		// TODO Auto-generated method stub
		// IScreenshot screenshotChrome = new ScreenshotChrome(SCREENSHOTPATH,
		// "testInstsantziertChrome", url); //works

		boolean takeReferenceScreenshot = false;

		if (takeReferenceScreenshot) {
			new ScreenshotFirefox(screenshotpath, refsreenshff, url);
		}

		final IScreenshot currentScreenshotFF = new ScreenshotFirefox(screenshotpath, currentDateTimeformatted + "_currentScreenshotFF.png", url);
		processImageComparrison(currentScreenshotFF);

	}

	/**
	 * @return
	 */
	private static String setupDateFormat() {
		Date dNow = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("yyyyMMdd-HHmmss");
		String currentDateTimeformatted = ft.format(dNow);
		System.out.println("Current Date: " + currentDateTimeformatted);
		return currentDateTimeformatted;
	}

	/**
	 * 
	 */
	private static void setupConfig() {
		CompositeConfiguration config = new CompositeConfiguration();
		config.addConfiguration(new SystemConfiguration());
		try {
			config.addConfiguration(new PropertiesConfiguration("application.properties"));
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		screenshotpath = config.getString("screenshotpath");
		refsreenshff = config.getString("refsreensh.ff");
		url = config.getString("url.politik");

		System.out.println(screenshotpath + ", " + refsreenshff + ", " + url);

	}

	/**
	 * @param currentScreenshot
	 * @param referenceScreenshotFile
	 * @throws Exception
	 * @throws IOException
	 */
	private static void processImageComparrison(final IScreenshot currentScreenshot) {
		File referenceScreenshotFile = new File(screenshotpath + "/" + refsreenshff);
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