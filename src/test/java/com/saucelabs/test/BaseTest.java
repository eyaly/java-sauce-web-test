package com.saucelabs.test;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.ITestResult;
import org.openqa.selenium.JavascriptExecutor;

import java.net.MalformedURLException;
import java.net.URL;

public class BaseTest {

    protected WebDriver driver;
    public static final String USERNAME = System.getenv("SAUCE_USERNAME");
    public static final String ACCESS_KEY = System.getenv("SAUCE_ACCESS_KEY");
    public static final String SAUCE_URL = "https://" + USERNAME + ":" + ACCESS_KEY + "@ondemand.eu-central-1.saucelabs.com:443/wd/hub";

    @BeforeMethod
    @Parameters({"platformName", "browserName", "browserVersion", "name", "armRequired", "extendedDebugging"})
    public void setup(
            String platformName,
            String browserName,
            String browserVersion,
            String testNameParam,
            boolean armRequired,
            boolean extendedDebugging,
            java.lang.reflect.Method method) throws MalformedURLException {

        String finalTestName = (testNameParam != null && !testNameParam.isEmpty()) ? testNameParam : method.getName();

        MutableCapabilities caps = new MutableCapabilities();
        caps.setCapability("platformName", platformName);
        caps.setCapability("browserName", browserName);
        caps.setCapability("browserVersion", browserVersion);

        MutableCapabilities sauceOptions = new MutableCapabilities();
        sauceOptions.setCapability("name", finalTestName);
        sauceOptions.setCapability("armRequired", armRequired);
        sauceOptions.setCapability("extendedDebugging", extendedDebugging);
        caps.setCapability("sauce:options", sauceOptions);

        URL url = new URL(SAUCE_URL);

        driver = new RemoteWebDriver(url, caps);
    }

    @AfterMethod
    public void teardown(ITestResult result) {
        System.out.println("AfterMethod hook");
        try {
            boolean bSuccess = result.isSuccess();
            ((JavascriptExecutor) driver).executeScript("sauce:job-result=" + (bSuccess ? "passed" : "failed"));
            if (!bSuccess)
                ((JavascriptExecutor) driver).executeScript("sauce:context=" + result.getThrowable().getMessage());
        } finally {
            System.out.println("Release driver");
            if (driver != null) {
                driver.quit();
            }
        }
    }
}
