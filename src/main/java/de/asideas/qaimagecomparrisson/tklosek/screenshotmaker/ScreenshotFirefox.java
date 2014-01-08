package de.asideas.qaimagecomparrisson.tklosek.screenshotmaker;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

/**
 * @author Tobias Klosek
 *
 */
public class ScreenshotFirefox extends AScreenshot {

	public ScreenshotFirefox(String path, String filename, String url) {		
		super(path, filename, url);
	}

	@Override
	public WebDriver getWebDriver() {
		return new FirefoxDriver();
	}

}
