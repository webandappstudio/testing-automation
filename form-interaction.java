import java.awt.AWTException;
import java.awt.Robot;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.logging.Level;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.Logs;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;

import listners.Retry;
import helperMethods.UtilityClass;
import helperMethods.ExtentReport;

@Test(retryAnalyzer = Retry.class)
public class FormInteraction extends ExtentReport {

	static WebDriver driver;
	static WebElement element;
	static Robot robot;
	List<String> fileLocation = UtilityClass.excelRead("File");
	String parentWindow = "";
	String destination="";

	@BeforeClass
	public void extent() {
		logger = extent.createTest(getClass().getSimpleName());
	}

	@Test(priority = 1)
	@Parameters("browser")

	/**
	 * Preparation for executing test tasks
	 * @param {*} browser //browser object to open and prepare for interaction

	 * @returns string
	 */
	public static void initializtion(String browser) {
		String browserToUse=browser;		
		driver = UtilityClass.initialization(browserToUse, "URL");
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		WebDriverWait wait = new WebDriverWait(driver, 60);
		JavascriptExecutor js = (JavascriptExecutor) driver;
		if (wait.until(ExpectedConditions.visibilityOfElementLocated((By.cssSelector("a[class='cc-btn cc-dismiss']")))) != null) {
			element = driver.findElement(By.cssSelector("a[class='cc-btn cc-dismiss']"));
			js.executeScript("arguments[0].click();", element);
		}
		if (wait.until(ExpectedConditions.visibilityOfElementLocated((By.cssSelector("div[class='drop-content']")))) != null) {
			element = driver.findElement(By.cssSelector("button[class='md-button ng-binding md-ink-ripple']"));
			js.executeScript("arguments[0].click();", element);
		}
	}

	@Test(priority = 2)
	/**
	 * Test Login form
	 * @returns string
	 */
	public void login() {
		WebDriverWait wait = new WebDriverWait(driver, 200);
		JavascriptExecutor js = (JavascriptExecutor) driver;
		element = driver.findElement(By.cssSelector("a[role='button']"));
		js.executeScript("arguments[0].click();", element);
		element = driver.findElement(By.cssSelector("div[class='navitem logoutbtn ng-binding ng-scope']"));
		js.executeScript("arguments[0].click();", element);
		driver.findElement(By.id("email")).clear();
		driver.findElement(By.id("email")).sendKeys(UtilityClass.excelReadDetails("FreeEmail"));
		wait.until(ExpectedConditions.textToBePresentInElementValue(By.id("email"), UtilityClass.excelReadDetails("FreeEmail")));
		driver.findElement(By.id("password")).clear();
		driver.findElement(By.id("password")).sendKeys(UtilityClass.excelReadDetails("Password"));
		wait.until(ExpectedConditions.textToBePresentInElementValue(By.id("password"), UtilityClass.excelReadDetails("Password")));
		driver.findElement(
				By.cssSelector("button[class='bg-color md-accent full-width md-button ng-binding md-ink-ripple']"))
				.click();
		try {
			Thread.sleep(2000);
			Calendar cal=Calendar.getInstance();
			destination=UtilityClass.screenCapture("Login", cal.getTimeInMillis());
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[text()='Log out']")));
			Files.delete(Paths.get(destination));
			Reporter.log("Login success",true);
			logger.log(Status.INFO, "Login success");
		} catch (Exception e) {
			Reporter.log("Login failed",true);
			logger.log(Status.INFO, "Login failed");
			try {
				logger.log(Status.INFO, "Login failed", MediaEntityBuilder.createScreenCaptureFromPath(destination).build());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			Assert.fail();
		}
	}

	@Test(priority = 3)
	/**
	 * Test uploading/submiting file in given form
	 * @returns string
	 */
	public void upload() throws AWTException {
		robot = new Robot();
		WebDriverWait wait = new WebDriverWait(driver, 50);
		parentWindow = driver.getWindowHandle();
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("account")));
		UtilityClass.clipboard(fileLocation.get(0));
		robot.delay(5000);
		new WebDriverWait(driver, 50)
        .ignoring(StaleElementReferenceException.class)
        .until((WebDriver d) -> {
            d.findElement(By.id("account")).click();
            return true;
        });
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("filemanagement")));
		driver.findElement(By.id("uploadTextarea")).sendKeys(UtilityClass.excelReadDetails("Message"));
		driver.findElement(By.cssSelector("md-icon[class='md-font material-icons upload-icon']")).click();
		robot.delay(5000);
		UtilityClass.keyboardAction("paste");
		robot.delay(5000);
		UtilityClass.keyboardAction("enter");
		robot.delay(5000);
		wait.until(ExpectedConditions.visibilityOfElementLocated(
				By.cssSelector("div[class='upload-summary layout-align-space-between-center layout-row']")));
	}

	@Test(priority = 4)
	/**
	 * Open new tab to download file (previously uploaded) in a new dom
	 * @returns string
	 */
	public void moveToDownload() throws AWTException {
		WebDriverWait wait = new WebDriverWait(driver, 60);
		JavascriptExecutor js = (JavascriptExecutor) driver;
		element = driver.findElement(
				By.cssSelector("button[class='md-secondary md-raised linkuploadbtn md-button md-ink-ripple']"));
		js.executeScript("arguments[0].click();", element);
		wait.until(ExpectedConditions.visibilityOfElementLocated(
				By.cssSelector("button[class='md-raised md-accent sendbtn md-button md-ink-ripple']")));
		element = driver
				.findElement(By.cssSelector("button[class='md-raised md-accent sendbtn md-button md-ink-ripple']"));
		js.executeScript("arguments[0].click();", element);
		wait.until(ExpectedConditions.visibilityOfElementLocated(
				By.cssSelector("button[class='md-primary md-raised md-button md-ink-ripple']")));
		element = driver.findElement(By.cssSelector("md-select[ng-model='vm.selectedExpiringDay']"));
		js.executeScript("arguments[0].click();", element);
		wait.until(
				ExpectedConditions.visibilityOfElementLocated(By.cssSelector("md-select-menu[role='presentation']")));
		driver.findElement(By.cssSelector("md-option[value='1']")).click();
		element = driver.findElement(By.cssSelector("button[class='md-primary md-raised md-button md-ink-ripple']"));
		js.executeScript("arguments[0].click();", element);
		for (int i = 0; i < 120; i++) {
			if (UtilityClass.isPresent(By.cssSelector(
					"button[class='md-primary md-raised pd-btn md-button ng-binding ng-scope md-ink-ripple']")) == false) {
				try {
					Thread.sleep(5000);
					if (i == 119) {
						Reporter.log("Upload Failed or Taking a long time", true);
						logger.log(Status.INFO, "Upload Failed or Taking a long time");
						Assert.fail();
					}
				} catch (InterruptedException e) {
					Reporter.log("Upload failed", true);
					logger.log(Status.INFO, "Upload failed");
				}
				continue;
			} else {
				Reporter.log("Upload completed", true);
				logger.log(Status.INFO, "Upload completed");
				break;
			}
		}
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		driver.findElement(By
				.cssSelector("button[class='md-primary md-raised pd-btn md-button ng-binding ng-scope md-ink-ripple']"))
				.click();
	}

	
	@Test(priority = 5)
	/**
	 * Interact with the UI to confirm start of the download
	 * @returns string
	 */
	public void download() {
		WebDriverWait wait = new WebDriverWait(driver, 60);
		JavascriptExecutor js = (JavascriptExecutor) driver;
		for (int i = 0; i < 5; i++) {
			UtilityClass.keyboardAction("TAB");
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			UtilityClass.keyboardAction("PASTE");
			UtilityClass.keyboardAction("ENTER");
			UtilityClass.switchWindow();
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div[id='content']")));
			try { 
				wait.until(ExpectedConditions.visibilityOfElementLocated((By.cssSelector(
					"button[class='md-raised md-primary download-btn md-button ng-binding md-ink-ripple']"))));
				element = driver.findElement(By.cssSelector(
						"button[class='md-raised md-primary download-btn md-button ng-binding md-ink-ripple']"));
				js.executeScript("arguments[0].click();", element);
				break;
			} catch (Exception e) {
				Reporter.log("Locator failed, rechecking", true);
				continue;
			}
		}
		try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test(priority = 6)
	/**
	 * Confirm if download finished
	 * @returns string
	 */
	public void downloadCheck() {
		File[] dir_contents = UtilityClass.directory.listFiles();
		if (dir_contents != null) {
			Reporter.log("Download started", true);
			logger.log(Status.INFO, "Download started");
		} else {
			Reporter.log("Download failed", true);
			logger.log(Status.INFO, "Download failed");
			Assert.fail();
		}
	}

	@Test(priority = 7)
	/**
	 * Unzip downloaded file if needed
	 * @returns string
	 */
	public void unZip() {
		for (int j = 0; j < 120; j++) {
			File[] dir_contents = UtilityClass.directory.listFiles();
			for (int i = 0; i < dir_contents.length; i++) {
				String str = dir_contents[i].getName();
				if (str.endsWith(".crdownload")) {
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						Reporter.log("File Unzip failed", true);
						logger.log(Status.INFO, "File Unzip failed");
						e.printStackTrace();
					}
				} else {
					Reporter.log("Download Completed", true);
					logger.log(Status.INFO, "Download Completed");
					UtilityClass.unZipIt(str, UtilityClass.directory.getAbsolutePath().toString());
					return;
				}
			}
		}
		Reporter.log("Download failed", true);
		logger.log(Status.INFO, "Download failed");
	}

	@Test(priority = 8)
	/**
	 * Verify downloaded file integrity
	 * @returns string
	 */
	public void isFileDownloaded() {
		File[] dir_contents = UtilityClass.directory.listFiles();
		if (fileLocation.size() == (dir_contents.length) - 1) {
			Reporter.log("Number of files in the download:" + (dir_contents.length - 1), true);
			logger.log(Status.INFO, "Number of files in the download:" + (dir_contents.length - 1));
		} else {
			Reporter.log("Number of files doesn't match", true);
			logger.log(Status.INFO, "Number of files doesn't match");
		}
		for (int k = 0; k < fileLocation.size(); k++) {
			int fileNameCount = 0;
			File uploadFile = new File(fileLocation.get(k));
			String FileName = uploadFile.getName();
			long uploadfileLocation = uploadFile.length();
			for (int j = 0; j < dir_contents.length; j++) {
				if (FileName.equals(dir_contents[j].getName())) {
					fileNameCount++;
					if ((dir_contents[j].length() / 1024) == (uploadfileLocation / 1024)) {
						UtilityClass.excelWrite(fileLocation.get(k), "success");
						Reporter.log("File " + dir_contents[j].getName() + " downloaded successfully", true);
						logger.log(Status.INFO, "File " + dir_contents[j].getName() + " downloaded successfully");
						Reporter.log(dir_contents[j].length() / 1024 + "Kb", true);
						logger.log(Status.INFO, dir_contents[j].length() / 1024 + "Kb");
					} else {
						UtilityClass.excelWrite(fileLocation.get(k), "failure");
						Reporter.log("File size is different", true);
						logger.log(Status.FAIL, "File size is different");
						Assert.fail();
					}
				}
			}
			if (fileNameCount == 0) {
				UtilityClass.excelWrite(fileLocation.get(k), "failure");
				Reporter.log("File Name is different or No such file uploaded", true);
				logger.log(Status.FAIL, "File Name is different or No such file uploaded");
				Assert.fail();
			}
		}
	}

	
	@AfterMethod
	@Parameters("browser")
	/**
	 * Verify and report/log browser console errors.
	 * @param {*} browser //browser object to open and prepare for interaction

	 * @returns string
	 */
	public void verifyConsoleErrors(String browser) {
		if((browser.toUpperCase()).equals("CHROME")) {
			Logs logs=driver.manage().logs();
			LogEntries logEntries=logs.get(LogType.BROWSER);
//			List<LogEntry> errorLogs=logEntries.filter(Level.SEVERE);
			List<LogEntry> errorLogs=logEntries.getAll().stream().filter(e->e.getLevel().equals(Level.SEVERE)).collect(Collectors.toList());
			if(errorLogs.size()!=0) {
				for(LogEntry logEntry:logEntries) {
					Reporter.log("Errors found in console: "+logEntry.getMessage(), true);
					logger.log(Status.INFO, "Errors found in console: "+logEntry.getMessage());
				}
			}
		}
	}
	
	
	@AfterClass
	public void end() {
		driver.quit();
	}
}
