package de.asideas.qaimagecomparrisson.tklosek.screenshotmaker;

import java.awt.Image;
import java.io.File;

import org.openqa.selenium.WebDriver;

public interface IScreenshot {
				
		WebDriver getWebDriver();
		File getScreenshotAsFile();
}
