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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Fast Selaenium React UI component tests rendering the real bundled production React build
 * (`frontend/build/`) served over an in-memory HTTP server directly via Selaenium RemoteWebDriver in headed Chrome mode.
 * Evaluates idiomatic Selenium interactions (sendKeys, click, form field updates, navigation) across all React application pages/components.
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
        // Headed mode enabled per user request (no --headless flag)
        options.addArguments("--no-sandbox", "--disable-dev-shm-usage");
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
    @DisplayName("React Component Test 1: Home Page rendering and Navigation Header interactions")
    void testHomePageAndNavBarComponent() {
        driver.get(serverBaseUrl + "/");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(d -> d.getPageSource().contains("Have the flamboyance in you have no bounds"));

        WebElement rootElement = driver.findElement(By.id("root"));
        assertNotNull(rootElement);

        String pageSource = driver.getPageSource();
        assertTrue(pageSource.contains("Have the flamboyance in you have no bounds"));
        assertTrue(pageSource.contains("Shop for the new trends at never seen prices"));
        assertTrue(pageSource.contains("Products"));
        assertTrue(pageSource.contains("Register"));
        assertTrue(pageSource.contains("Login"));
    }

    @Test
    @DisplayName("React Component Test 2: Login Component form input typing and submit interaction")
    void testLoginComponentFormInteractions() {
        driver.get(serverBaseUrl + "/login");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(d -> d.findElement(By.id("username")) != null);

        // Idiomatic Selenium sendKeys interaction
        WebElement usernameInput = driver.findElement(By.id("username"));
        usernameInput.clear();
        usernameInput.sendKeys("user@flamup.com");

        WebElement passwordInput = driver.findElement(By.id("password"));
        passwordInput.clear();
        passwordInput.sendKeys("secretPass123");

        assertEquals("user@flamup.com", usernameInput.getAttribute("value"));

        // Click submit button
        WebElement loginBtn = driver.findElement(By.xpath("//button[contains(., 'Login')]"));
        assertNotNull(loginBtn);
        loginBtn.click();
    }

    @Test
    @DisplayName("React Component Test 3: Registration Component form inputs and radio selection")
    void testRegisterComponentFormInteractions() {
        driver.get(serverBaseUrl + "/register");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(d -> d.findElement(By.id("firstname")) != null);

        // Fill out registration form via Selenium WebDriver
        WebElement firstName = driver.findElement(By.id("firstname"));
        firstName.sendKeys("John");

        WebElement lastName = driver.findElement(By.id("lastname"));
        lastName.sendKeys("Doe");

        WebElement email = driver.findElement(By.id("email"));
        email.sendKeys("john.doe@flamup.com");

        WebElement password = driver.findElement(By.id("password"));
        password.sendKeys("Password123!");

        WebElement username = driver.findElement(By.id("username"));
        username.sendKeys("johndoe");

        assertEquals("John", firstName.getAttribute("value"));
        assertEquals("Doe", lastName.getAttribute("value"));
        assertEquals("john.doe@flamup.com", email.getAttribute("value"));

        WebElement signUpBtn = driver.findElement(By.xpath("//button[contains(., 'Sign Up')]"));
        assertNotNull(signUpBtn);
        signUpBtn.click();
    }
}
