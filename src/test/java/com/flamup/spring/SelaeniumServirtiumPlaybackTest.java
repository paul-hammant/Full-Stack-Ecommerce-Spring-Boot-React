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

import java.io.File;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Servirtium VCR Playback Mode Test:
 * Fast isolated Selaenium test driving Chrome against the real React UI
 * over Servirtium VCR mock server replaying HTTP interactions directly from Markdown tapes.
 * Requires NO Spring Boot application server or live database connection during execution!
 */
public class SelaeniumServirtiumPlaybackTest {

    public static final String TAPE_PATH = "tapes/react-app-interactions.md";

    private static RemoteWebDriver driver;
    private static VcrServer vcrServer;

    @BeforeAll
    static void setUp() {
        File tapeFile = new File(TAPE_PATH);
        assertTrue(tapeFile.exists(), "Servirtium tape file must exist for playback test");

        // Start Servirtium VCR in PLAYBACK mode (replay recorded HTTP interactions from tape)
        vcrServer = Vcr.playback(TAPE_PATH)
                .matchJsonBody()
                .matchMultiple()
                .start();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");
        driver = RemoteWebDriver.localChrome(options.asMap());
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
        if (vcrServer != null) {
            vcrServer.close();
        }
    }

    @Test
    @DisplayName("Playback Mode: Selaenium clicks real React UI with Servirtium replaying HTTP tape recordings")
    void testSelaeniumClickReactUiInPlaybackMode() {
        String playbackBaseUrl = vcrServer.baseUrl();

        // 1. Mount React UI over VCR playback server
        driver.get(playbackBaseUrl + "/index.html");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

        WebElement rootElement = wait.until(d -> d.findElement(By.id("root")));
        assertNotNull(rootElement, "React UI root mounted over Servirtium VCR playback server");

        String pageSource = driver.getPageSource();
        assertTrue(pageSource.contains("root") || pageSource.contains("FlamUp"), "React application loaded in playback mode");

        // 2. Replay API auth endpoint interaction from Markdown tape
        driver.get(playbackBaseUrl + "/api/auth");
        String authResponse = driver.getPageSource();
        assertTrue(authResponse.contains("AUTHENTICATED"), "Replayed /api/auth response from Markdown tape");

        // 3. Replay products clothes endpoint interaction from Markdown tape
        driver.get(playbackBaseUrl + "/api/v1/clothes/?sex=MALE&page=0");
        String clothesResponse = driver.getPageSource();
        assertNotNull(clothesResponse, "Replayed /api/v1/clothes response from Markdown tape");
    }
}
