package de.asideas.qaimagecomparrisson.tklosek.screenshotmaker;

import java.io.File;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

/**
 * @author Tobias Klosek
 *
 */
public class ScreenshotChrome extends AScreenshot {


	public ScreenshotChrome(String path, String filename, String url) {
		super(path, filename, url);
	}

	@Override
	public WebDriver getWebDriver() {
		File fileChromeDriver = new File("driver/chromedriver");
		System.setProperty("webdriver.chrome.driver", fileChromeDriver.getAbsolutePath());
		return new ChromeDriver();
	}

}
