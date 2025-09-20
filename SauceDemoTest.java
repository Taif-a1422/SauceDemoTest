package task45;
import com.aventstack.extentreports.*;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.imageio.ImageIO;
import org.apache.commons.io.FileUtils;
import java.awt.image.BufferedImage;

public class SauceDemoTest {


    private static WebDriver driver;
    private static final Logger log = LogManager.getLogger(SauceDemoTest.class);
    private static ExtentReports extent;
    private static ExtentTest test;

    public static void main(String[] args) throws IOException {
        setup();
        login();
        captureProductScreenshots();
        logout();
        tearDown();
    }

    public static void setup() {
        driver = new ChromeDriver();
        WebDriverManager.chromedriver().setup();

        driver.manage().window().maximize();

        ExtentSparkReporter spark = new ExtentSparkReporter("ExtentReport.html");
        extent = new ExtentReports();
        extent.attachReporter(spark);
        test = extent.createTest("SauceDemo Test");

        log.info("Opening the browser and navigating to SauceDemo.");
        driver.get("https://www.saucedemo.com/v1/index.html");
        test.pass("Navigated to SauceDemo login page.");
    }

    public static void login() {
        try {
            driver.findElement(By.id("user-name")).sendKeys("standard_user");
            driver.findElement(By.id("password")).sendKeys("secret_sauce");
            driver.findElement(By.id("login-button")).click();

            log.info("Logged in as standard_user.");
            test.pass("Login successful.");
        } catch (Exception e) {
            log.error("Login failed.", e);
            test.fail("Login failed.");
        }
    }

    public static void captureProductScreenshots() throws IOException {
        List<WebElement> prices = driver.findElements(By.className("inventory_item_price"));
        Map<Double, WebElement> priceMap = new TreeMap<>();

        for (WebElement priceElement : prices) {
            double price = Double.parseDouble(priceElement.getText().replace("$", ""));
            WebElement parent = priceElement.findElement(By.xpath("./ancestor::div[@class='inventory_item']"));
            priceMap.put(price, parent);
        }

        WebElement lowest = priceMap.entrySet().iterator().next().getValue();
        WebElement highest = ((TreeMap<Double, WebElement>) priceMap).lastEntry().getValue();

        takeElementScreenshot(lowest, "lowest_price_product.png");
        takeElementScreenshot(highest, "highest_price_product.png");

        log.info("Captured screenshots for lowest and highest priced products.");
        test.pass("Screenshots taken for products with lowest and highest prices.");
    }

    public static void takeElementScreenshot(WebElement element, String fileName) throws IOException {
        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        BufferedImage fullImg = ImageIO.read(screenshot);

        Point point = element.getLocation();
        int eleWidth = element.getSize().getWidth();
        int eleHeight = element.getSize().getHeight();

        BufferedImage eleScreenshot = fullImg.getSubimage(point.getX(), point.getY(), eleWidth, eleHeight);
        ImageIO.write(eleScreenshot, "png", screenshot);
        FileUtils.copyFile(screenshot, new File(fileName));
    }

    public static void logout() {
        try {
            driver.findElement(By.id("react-burger-menu-btn")).click();
            WebElement logoutBtn = driver.findElement(By.id("logout_sidebar_link"));
            Thread.sleep(1000); // Wait for animation
            logoutBtn.click();

            log.info("Logged out successfully.");
            test.pass("Logged out from the application.");
        } catch (Exception e) {
            log.error("Logout failed.", e);
            test.fail("Logout failed.");
        }
    }

    public static void tearDown() {
        if (driver != null) {
            driver.quit();
            log.info("Browser closed.");
            test.pass("Browser closed.");
        }
        extent.flush();
    }
}