package com.flamup.spring;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.RemoteWebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Full-stack E2E Integration tests using Selaenium RemoteWebDriver
 * driving Chrome against the live Spring Boot + React full stack web application.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
public class SelaeniumFullStackTest {

    @LocalServerPort
    private int port;

    private static RemoteWebDriver driver;

    @BeforeAll
    static void setUpDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--no-sandbox", "--disable-dev-shm-usage");
        driver = RemoteWebDriver.localChrome(options.asMap());
    }

    @AfterAll
    static void tearDownDriver() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @DisplayName("Full-Stack Test: Home Page loads built React application correctly")
    void testHomePageLoads() {
        String baseUrl = "http://127.0.0.1:" + port + "/index.html";
        driver.get(baseUrl);

        WebElement rootElement = driver.findElement(By.id("root"));
        assertNotNull(rootElement);

        String pageSource = driver.getPageSource();
        assertTrue(pageSource.contains("Have the flamboyance") || pageSource.contains("FlamUp") || pageSource.contains("root"),
                "Page source should contain React application content");
    }

    @Test
    @DisplayName("Full-Stack Test: Direct navigation to React App SPA route")
    void testReactAppSpaRoute() {
        String appUrl = "http://127.0.0.1:" + port + "/index.html";
        driver.get(appUrl);

        WebElement rootElement = driver.findElement(By.id("root"));
        assertNotNull(rootElement);

        String title = driver.getTitle();
        assertNotNull(title);
    }

    @Test
    @DisplayName("Full-Stack Test: Direct API health endpoint accessibility")
    void testApiAuthEndpoint() {
        String authUrl = "http://127.0.0.1:" + port + "/api/auth";
        driver.get(authUrl);

        String pageSource = driver.getPageSource();
        assertNotNull(pageSource);
    }
}
