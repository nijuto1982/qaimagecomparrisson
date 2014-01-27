/**
 * 
 */
package de.asideas.qaimagecomparrisson.tklosek.screenshotmaker;

import java.awt.Image;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.jdiffchaser.imgprocessing.Screenshot;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

/**
 * @author tklosek
 * 
 */
public abstract class AScreenshot implements IScreenshot {

	protected String path;
	protected String filename;
	protected File actualScreenshotFile;
	protected String url;

	/**
	 * @param path
	 * @param filename
	 * @param url
	 */
	public AScreenshot(String path, String filename, String url) {
		this.path = path;
		this.filename = filename;
		this.url = url;
		saveScreenshot();
	}

	public void saveScreenshot() {

		WebDriver driver = getWebDriver();
		driver.manage().window().maximize();
		driver.get(url);

		int contentHeight = 0;
		int windowHeight = 0;

		contentHeight = driver.findElement(By.id("content")).getSize().height;
		windowHeight = driver.manage().window().getSize().height;

		int windowHeigtQuarter = windowHeight / 4;
		for (int second = 0;; second += windowHeigtQuarter) {
			if (second > contentHeight) {
				System.out.println(second);
				break;
			}
			((JavascriptExecutor) driver).executeScript("window.scrollBy(0," + windowHeigtQuarter + ")", "");
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
		actualScreenshotFile = new File(path + "/" + filename);

		driver.close();
		driver.quit();

		try {
			FileUtils.copyFile(scrFile, actualScreenshotFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public File getScreenshotAsFile() {
		return actualScreenshotFile;
	}

}
