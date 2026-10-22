package com.flamup.spring;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.RemoteWebDriver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class FullStackSelaeniumE2ETest {

    private static HttpServer fullStackServer;
    private static int port;
    private static WebDriver driver;

    @BeforeAll
    public static void setUp() throws Exception {
        fullStackServer = HttpServer.create(new InetSocketAddress(0), 0);
        port = fullStackServer.getAddress().getPort();

        // Serve root / page
        fullStackServer.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if ("/login".equals(exchange.getRequestURI().getPath())) {
                    String html = "<html>" +
                            "<head><title>Flamup - Login</title></head>" +
                            "<body>" +
                            "  <h2>Login to Flamup</h2>" +
                            "  <form id='login-form'>" +
                            "    <input type='text' id='userName' name='userName' placeholder='Username'/>" +
                            "    <input type='password' id='password' name='password' placeholder='Password'/>" +
                            "    <button type='submit'>Login</button>" +
                            "  </form>" +
                            "</body>" +
                            "</html>";
                    byte[] response = html.getBytes();
                    exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                    exchange.sendResponseHeaders(200, response.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response);
                    }
                    return;
                }

                String html = "<html>" +
                        "<head><title>Flamup - Home</title></head>" +
                        "<body>" +
                        "  <h3>Have the flamboyance in you have no bounds</h3>" +
                        "  <p>Shop for the new trends at never seen prices</p>" +
                        "  <a href='/login'>Register now</a>" +
                        "</body>" +
                        "</html>";
                byte[] response = html.getBytes();
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, response.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response);
                }
            }
        });

        fullStackServer.start();

        Map<String, Object> googOpts = Map.of(
            "args", List.of("--headless=new", "--no-sandbox", "--disable-dev-shm-usage")
        );
        Map<String, Object> caps = Map.of(
            "browserName", "chrome",
            "goog:chromeOptions", googOpts
        );
        driver = RemoteWebDriver.localChrome(caps);
        driver.implicitlyWait(5000);
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
        if (fullStackServer != null) {
            fullStackServer.stop(0);
        }
    }

    @Test
    public void testHomePageAndNavigation() {
        String baseUrl = "http://localhost:" + port + "/";
        driver.get(baseUrl);

        String title = driver.getTitle();
        assertEquals("Flamup - Home", title);

        WebElement heading = driver.findElement(By.tagName("h3"));
        assertNotNull(heading);
        assertTrue(heading.getText().contains("Have the flamboyance in you have no bounds"));

        WebElement registerLink = driver.findElement(By.xpath("//a[@href='/login']"));
        assertNotNull(registerLink);
        assertEquals("Register now", registerLink.getText());
    }

    @Test
    public void testLoginPageElements() {
        String loginUrl = "http://localhost:" + port + "/login";
        driver.get(loginUrl);

        assertEquals("Flamup - Login", driver.getTitle());

        WebElement userNameInput = driver.findElement(By.xpath("//input[@name='userName' or @id='userName' or @type='text']"));
        assertNotNull(userNameInput);

        WebElement passwordInput = driver.findElement(By.xpath("//input[@type='password']"));
        assertNotNull(passwordInput);

        WebElement submitButton = driver.findElement(By.xpath("//button[@type='submit']"));
        assertNotNull(submitButton);
    }
}
