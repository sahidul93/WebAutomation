package base;

import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.LogStatus;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.*;
import report.ExtentManager;
import report.ExtentTestManager;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CommonAPI {
    public static WebDriver driver;
    public static ExtentReports extent;

    //https:// + username + : + key + specific url for cloud
    public static String SAUCE_URL="https://peoplentech1234:(key)@ondemand.saucelabs.com.80/wd/hub";
    public static String BROWSERSTACK_URL="";

    public static WebDriver getLocalDriver(String browser, String platform) {
        //chrome popup
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("disable-infobars");

        if (platform.equalsIgnoreCase("mac") && browser.equalsIgnoreCase("chrome")) {
            System.setProperty("webdriver.chrome.driver", "../Generic/src/main/resources/chromedriver");
            driver = new ChromeDriver(chromeOptions);
        } else if (platform.equalsIgnoreCase("windows") && browser.equalsIgnoreCase("chrome")) {
            System.setProperty("webdriver.chrome.driver", "../Generic/src/main/resources/chromedriver.exe");
            driver = new ChromeDriver(chromeOptions);
        } else if (platform.equalsIgnoreCase("mac") && browser.equalsIgnoreCase("firefox")) {
            System.setProperty("webdriver.gecko.driver", "../Generic/src/main/resources/geckodriver");
            driver = new FirefoxDriver();
        } else if (platform.equalsIgnoreCase("windows") && browser.equalsIgnoreCase("firefox")) {
            System.setProperty("webdriver.gecko.driver", "../Generic/src/main/resources/geckodriver.exe");
            driver = new FirefoxDriver();
        }
        driver.manage().timeouts().pageLoadTimeout(20, TimeUnit.SECONDS);
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        driver.manage().window().maximize();

        return driver;
    }

    public static WebDriver getCloudDriver(String browser, String broswerVersion, String platform, String envName)
            throws MalformedURLException {
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        desiredCapabilities.setCapability("name", "Cloud Execution");
        desiredCapabilities.setCapability("browser", browser);
        desiredCapabilities.setCapability("browser_version", broswerVersion);
        desiredCapabilities.setCapability("os", platform);
        desiredCapabilities.setCapability("os_version", "Mojave");
        desiredCapabilities.setCapability("resolution", "1600x1200");

        if (envName.equalsIgnoreCase("saucelabs")){
            driver = new RemoteWebDriver(new URL(SAUCE_URL), desiredCapabilities);
        }else if (envName.equalsIgnoreCase("browserstack")) {
            driver = new RemoteWebDriver(new URL(BROWSERSTACK_URL), desiredCapabilities);
        }
        return driver;
    }

    @Parameters({"platform", "url", "browser", "cloud", "browserVersion", "envName"})
    @BeforeMethod
    public static WebDriver setupDriver(String platform, String url, String browser, boolean cloud,
                                        String broswerVersion, String envName) throws MalformedURLException {
        if (cloud == true) {
            driver = getCloudDriver(browser, broswerVersion, platform, envName);
        } else {
            driver = getLocalDriver(browser, platform);
        }
        driver.get(url);
        return driver;
    }

    //reporting starts
    @BeforeSuite
    public void extentSetup(ITestContext context) {
        ExtentManager.setOutputDirectory(context);
        extent = ExtentManager.getInstance();
    }

    @BeforeMethod
    public void startExtent(Method method) {
        String className = method.getDeclaringClass().getSimpleName();
        ExtentTestManager.startTest(method.getName());
        ExtentTestManager.getTest().assignCategory(className);
    }

    protected String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }

    @AfterMethod
    public void afterEachTestMethod(ITestResult result) {
        ExtentTestManager.getTest().getTest().setStartedTime(getTime(result.getStartMillis()));
        ExtentTestManager.getTest().getTest().setEndedTime(getTime(result.getEndMillis()));
        for (String group : result.getMethod().getGroups()) {
            ExtentTestManager.getTest().assignCategory(group);
        }

        if (result.getStatus() == 1) {
            ExtentTestManager.getTest().log(LogStatus.PASS, "Test Passed");
        } else if (result.getStatus() == 2) {
            ExtentTestManager.getTest().log(LogStatus.FAIL, getStackTrace(result.getThrowable()));
        } else if (result.getStatus() == 3) {
            ExtentTestManager.getTest().log(LogStatus.SKIP, "Test Skipped");
        }

        ExtentTestManager.endTest();
        extent.flush();
        if (result.getStatus() == ITestResult.FAILURE) {
            captureScreenshot(driver, result.getName());
        }
    }

    private Date getTime(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return calendar.getTime();
    }

    @AfterSuite
    public void generateReport() {
        extent.close();
    }
    //reporting finish

    public void sleepFor(int seconds) {
        try {
            Thread.sleep(seconds*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    @AfterMethod
    public void cleanUp(){
        driver.close();
        driver.quit();
    }

    public void clickOnElementByXpath(String locator) {
        driver.findElement(By.xpath(locator)).click();
    }

    public void clickOnElementById(String locator){
        driver.findElement(By.id(locator)).click();
    }

    public void sendKeysByXpath(String locator, String keys){
        driver.findElement(By.xpath(locator)).sendKeys(keys);
    }

    public void sendKeysById(String locator, String keys) {
        driver.findElement(By.id(locator)).sendKeys(keys);
    }

    public void sendKeysByClass(String locator, String keys) {
        driver.findElement(By.className(locator)).sendKeys(keys);
    }

    public void sendKeysByCSS(String locator, String keys) {
        driver.findElement(By.cssSelector(locator)).sendKeys(keys);
    }

    public void sendKeysByLinkText(String locator, String keys) {
        driver.findElement(By.linkText(locator)).sendKeys(keys);
    }

    public void sendKeysByPartialLinkText(String locator, String keys) {
        driver.findElement(By.partialLinkText(locator)).sendKeys(keys);
    }
    public void clickOnElementByClass(String locator) {
        driver.findElement(By.className(locator)).click();
    }

    public void clickOnElementByCSS(String locator) {
        driver.findElement(By.cssSelector(locator)).click();
    }

    public void clickOnElementByLinkText(String locator) {
        driver.findElement(By.linkText(locator)).click();
    }

    public void clickonElementByPartialLinkText(String locator) {
        driver.findElement(By.partialLinkText(locator)).click();
    }

    public String getValueByXpath(String locator) {
        return driver.findElement(By.xpath(locator)).getText();
    }

    public boolean isElementDisplayed(String locator) {
        return driver.findElement(By.xpath(locator)).isDisplayed();
    }

    public boolean isElementEnabled(String locator) {
        return driver.findElement(By.xpath(locator)).isEnabled();
    }

    public boolean isElementSelected(String locator) {
        return driver.findElement(By.xpath(locator)).isSelected();
    }

    public WebElement getElement(String locator) {
        WebElement element = driver.findElement(By.xpath(locator));
        return element;
    }

    public WebElement getElementByLinkText(String locator) {
        return driver.findElement(By.linkText(locator));
    }

    public static void captureScreenshot(WebDriver driver, String screenshotName) {
        DateFormat df = new SimpleDateFormat("(MM.dd.yyyy-HH:mma)");
        Date date = new Date();
        df.format(date);
        File file = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        try {
            FileUtils.copyFile(file, new File(System.getProperty("user.dir") + "/screenshots/"
                    + screenshotName + " " + df.format(date) + ".png"));
            System.out.println("Screenshot captured");
        } catch (IOException e) {
            System.out.println("Exception while taking screenshot " + e.getMessage());
            ;
        }
    }

    public void drapNDropByXpaths(String fromLocator, String toLocator) {
        Actions actions = new Actions(driver);

        WebElement from = getElement(fromLocator);
        WebElement to = getElement(toLocator);

        actions.dragAndDrop(from, to).build().perform();

        sleepFor(5);

    }

    public void scrollToView(String locator) {
        JavascriptExecutor javascriptExecutor = (JavascriptExecutor) driver;
        javascriptExecutor.executeScript("arguments[0].scrollIntoView(true);", getElementByLinkText(locator));
        sleepFor(10);
    }

    // Explicit Wait
    public void waitExplicitlyByXpath(String locator, int seconds) {
        WebDriverWait webDriverWait = new WebDriverWait(driver, seconds);
        //webDriverWait.until(ExpectedConditions.visibilityOf(getElement(locator)));
        //webDriverWait.until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath(locator))));
        webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(locator)));

    }

    public void waitUntilSelectable(String locator, int seconds){
        WebDriverWait webDriverWait = new WebDriverWait(driver, seconds);
        webDriverWait.until(ExpectedConditions.elementToBeSelected(getElement(locator)));
    }

    public void waitUntilClickable(String locator, int seconds) {
        WebDriverWait webDriverWait = new WebDriverWait(driver, seconds);
        webDriverWait.until(ExpectedConditions.elementToBeClickable(By.xpath(locator)));
    }

    //getLink
    public String getAllLink() {
        return driver.findElement(By.tagName("a")).getText();
    }

    public List<String> getAllLinks() {
        List<WebElement> webElements = driver.findElements(By.tagName("a"));
        List<String> stringList = new ArrayList<String>();
        for (int i= 0; i<webElements.size();i++) {
            stringList.add(webElements.get(i).getText());
        }
        return stringList;
    }

    //
    public void uploadFile(String path, String locator){
        driver.findElement(By.xpath(locator)).sendKeys(path);
    }

    public void clearFieldByXpath(String locator) {
        driver.findElement(By.xpath(locator)).clear();
    }

    public void typeEnterByXpath(String locator) {
        driver.findElement(By.xpath(locator)).sendKeys(Keys.ENTER);
    }

    public Date getTimeByMilliseconds(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return calendar.getTime();
    }

    public void navigateBack() {
        driver.navigate().back();
    }

    public void navigateForward() {
        driver.navigate().forward();
    }

}
