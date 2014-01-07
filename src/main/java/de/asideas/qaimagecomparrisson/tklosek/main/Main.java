/**
 * 
 */
package de.asideas.qaimagecomparrisson.tklosek.main;

import java.awt.Image;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.JFileChooser;

import org.jdiffchaser.imgprocessing.ImageComparator;
import org.jdiffchaser.imgprocessing.Screenshot;
import org.jdiffchaser.utils.ImageUtilities;

import de.asideas.qaimagecomparrisson.tklosek.screenshotmaker.IScreenshot;
import de.asideas.qaimagecomparrisson.tklosek.screenshotmaker.ScreenshotChrome;
import de.asideas.qaimagecomparrisson.tklosek.screenshotmaker.ScreenshotFirefox;

/**
 * @author tklosek
 * 
 */
public class Main {

	private static final String SCREENSHOTPATH = "screenshots";
	private static final String URL = "http://www.welt.de/";
	private static final String REFERENCESCREENSHOTFF = "referenceScreenshotFF";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		// IScreenshot screenshotChrome = new ScreenshotChrome(SCREENSHOTPATH,
		// "testInstsantziertChrome", URL); //works

		compareScreenshots().run();

	}

	/**
	 * @param currentScreenshotFF
	 * @return
	 */
	private static Runnable compareScreenshots() {
		final IScreenshot currentScreenshotFF = new ScreenshotFirefox(SCREENSHOTPATH, "currentScreenshotFF", URL);
		Date dNow = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");

		System.out.println("Current Date: " + ft.format(dNow));
		try {
			File referenceScreenshotFile = new File(SCREENSHOTPATH + "/" + REFERENCESCREENSHOTFF + ".png");

			Image referenceSreenshot = Screenshot.loadImageFromFile(referenceScreenshotFile);
			ImageComparator imageComparator = ImageComparator.getInstance(0.6, referenceSreenshot, "Reference screenshot",
					currentScreenshotFF.getScreensotAsImage(), "Current Screenshot");
			imageComparator.compareImages(true);

			String absolutePath = referenceScreenshotFile.getAbsolutePath();

			JFileChooser fileChooser = new JFileChooser(System.getProperty(absolutePath));
			fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
			fileChooser.setSelectedFile(new File(ft.format(dNow) + "differences.png"));
			// fileChooser.setCurrentDirectory(new
			// File(System.getProperty("user.dir")));
			// fileChooser.setSelectedFile(new File("diff.png"));
			if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
				ImageUtilities.storeImage(imageComparator.getLastResultImage(), fileChooser.getSelectedFile().getPath());
			}
			System.exit(0);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				int nrOfProcessors = Runtime.getRuntime().availableProcessors();
				ExecutorService eservice = Executors.newFixedThreadPool(nrOfProcessors);

				eservice.submit(compareScreenshots());
				System.out.println();
			}
		};
		return runnable;
	}
}