package com.flamup.spring;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.RemoteWebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Fast Selaenium React UI component tests rendering the real bundled production React build
 * (`frontend/build/`) served over an in-memory HTTP server directly via Selaenium RemoteWebDriver in headless Chrome.
 */
public class SelaeniumComponentTest {

    private static RemoteWebDriver driver;
    private static HttpServer staticServer;
    private static String serverBaseUrl;

    @BeforeAll
    static void setUp() throws IOException {
        File buildDir = new File("frontend/build");
        assertTrue(buildDir.exists() && buildDir.isDirectory(), "frontend/build directory must exist");

        // Start lightweight in-memory static file HTTP server to serve real compiled React assets
        staticServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        staticServer.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";
            File file = new File(buildDir, path);
            if (!file.exists() || file.isDirectory()) {
                file = new File(buildDir, "index.html");
            }
            byte[] bytes = new FileInputStream(file).readAllBytes();
            exchange.sendResponseHeaders(200, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        });
        staticServer.start();
        int port = staticServer.getAddress().getPort();
        serverBaseUrl = "http://127.0.0.1:" + port;

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--no-sandbox", "--disable-dev-shm-usage");
        driver = RemoteWebDriver.localChrome(options.asMap());
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
        if (staticServer != null) {
            staticServer.stop(0);
        }
    }

    @Test
    @DisplayName("Real React Component Test: App Root and Main Heading render in DOM")
    void testRealReactAppHeadingComponent() {
        driver.get(serverBaseUrl + "/");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(d -> d.getPageSource().contains("Have the flamboyance in you have no bounds"));

        String pageSource = driver.getPageSource();
        assertTrue(pageSource.contains("Have the flamboyance in you have no bounds"),
                "Real React UI component heading should render in DOM");
    }

    @Test
    @DisplayName("Real React Component Test: Home Page Subtitle and Action Link components render")
    void testRealReactAppHomeSubcomponents() {
        driver.get(serverBaseUrl + "/");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(d -> d.getPageSource().contains("Shop for the new trends at never seen prices"));

        String pageSource = driver.getPageSource();
        assertTrue(pageSource.contains("Shop for the new trends at never seen prices"));
        assertTrue(pageSource.contains("Explore more trends"));
        assertTrue(pageSource.contains("Register now"));
    }

    @Test
    @DisplayName("Real React Component Test: Navigation Bar and Action buttons render")
    void testRealReactAppBarComponent() {
        driver.get(serverBaseUrl + "/");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(d -> d.findElement(By.id("root")) != null);

        WebElement root = driver.findElement(By.id("root"));
        assertNotNull(root);

        String pageSource = driver.getPageSource();
        assertTrue(pageSource.contains("Products"));
        assertTrue(pageSource.contains("Register"));
        assertTrue(pageSource.contains("Login"));
    }
}
