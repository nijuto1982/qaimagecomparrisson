package de.asideas.qaimagecomparrisson.tklosek.screenshotmaker;

import java.io.File;

import org.openqa.selenium.WebDriver;

/**
 * @author Tobias Klosek
 *
 */
public interface IScreenshot {
				
		WebDriver getWebDriver();
		File getScreenshotAsFile();
}
