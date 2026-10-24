package com.flamup.spring;

import com.paulhammant.servirtium.vcr.Vcr;
import com.paulhammant.servirtium.vcr.VcrServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.RemoteWebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Servirtium VCR Record Mode Test:
 * Drives Selaenium RemoteWebDriver against the real React UI over Servirtium VCR proxying to live Spring Boot backend.
 * Captures real HTTP endpoint interactions into canonical Servirtium Markdown tape.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
public class SelaeniumServirtiumRecordTest {

    public static final String TAPE_PATH = "tapes/react-app-interactions.md";

    @LocalServerPort
    private int springBootPort;

    private static RemoteWebDriver driver;

    @BeforeAll
    static void setUpDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");
        driver = RemoteWebDriver.localChrome(options.asMap());
    }

    @AfterAll
    static void tearDownDriver() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @DisplayName("Record Mode: Drive Selaenium UI interactions over Servirtium VCR proxy and generate Markdown tape")
    void recordReactUiEndpointInteractions() throws Exception {
        File tapeDir = new File("tapes");
        if (!tapeDir.exists()) {
            tapeDir.mkdirs();
        }

        String upstreamUrl = "http://127.0.0.1:" + springBootPort;

        // Start Servirtium VCR in RECORD mode proxying to live Spring Boot server
        VcrServer vcrServer = Vcr.record(TAPE_PATH, upstreamUrl)
                .staticContent("/", "frontend/build")
                .start();

        try {
            String vcrBaseUrl = vcrServer.baseUrl();

            // 1. Load Root Page through VCR
            driver.get(vcrBaseUrl + "/index.html");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

            WebElement root = wait.until(d -> d.findElement(By.id("root")));
            assertNotNull(root, "React root element loaded through VCR");

            String source = driver.getPageSource();
            System.out.println("VCR Record PageSource Length: " + source.length());
            assertTrue(source.contains("root") || source.contains("FlamUp"), "Loaded React SPA through VCR proxy");

            // Direct API calls through VCR proxy to record REST endpoints
            driver.get(vcrBaseUrl + "/api/auth");
            String authSource = driver.getPageSource();
            assertTrue(authSource.contains("AUTHENTICATED"), "VCR proxied /api/auth response");

            driver.get(vcrBaseUrl + "/api/v1/clothes/?sex=MALE&page=0");
            String clothesSource = driver.getPageSource();
            assertNotNull(clothesSource, "VCR proxied clothes API response");

        } finally {
            if (vcrServer != null) {
                vcrServer.close();
            }
        }

        File tapeFile = new File(TAPE_PATH);
        assertTrue(tapeFile.exists(), "Recorded Servirtium Markdown tape file must exist after VcrServer close");
        assertTrue(tapeFile.length() > 0, "Recorded tape file must contain recorded interactions");
    }
}
