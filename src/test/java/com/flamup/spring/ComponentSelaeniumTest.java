package com.flamup.spring;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
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

public class ComponentSelaeniumTest {

    private static HttpServer mockServer;
    private static int mockPort;
    private static WebDriver driver;

    @BeforeAll
    public static void setUp() throws Exception {
        // Start lightweight mock HTTP server for fast isolated component testing
        mockServer = HttpServer.create(new InetSocketAddress(0), 0);
        mockPort = mockServer.getAddress().getPort();

        // Serve a mocked Product Card Component HTML
        mockServer.createContext("/product-card", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String html = "<html>" +
                        "<head><title>Product Component Test</title></head>" +
                        "<body>" +
                        "  <div id='product-123' class='product-card'>" +
                        "    <h2 class='product-title'>Flamup Classic Denim Jacket</h2>" +
                        "    <span class='product-price'>$49.99</span>" +
                        "    <button id='add-to-cart-btn' onclick='document.getElementById(\"cart-status\").innerText=\"Item Added!\";'>Add to Cart</button>" +
                        "    <p id='cart-status'>Empty</p>" +
                        "  </div>" +
                        "</body>" +
                        "</html>";
                byte[] response = html.getBytes();
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, response.length);
                OutputStream os = exchange.getResponseBody();
                os.write(response);
                os.close();
            }
        });

        // Serve a mocked User Registration Component HTML
        mockServer.createContext("/registration-form", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String html = "<html>" +
                        "<head><title>Registration Component</title></head>" +
                        "<body>" +
                        "  <form id='reg-form' onsubmit='event.preventDefault(); document.getElementById(\"msg\").innerText=\"Registered Successfully!\";'>" +
                        "    <input id='first-name' type='text' name='firstName' value=''/>" +
                        "    <input id='email' type='email' name='email' value=''/>" +
                        "    <button id='submit-btn' type='submit'>Register</button>" +
                        "    <div id='msg'></div>" +
                        "  </form>" +
                        "</body>" +
                        "</html>";
                byte[] response = html.getBytes();
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, response.length);
                OutputStream os = exchange.getResponseBody();
                os.write(response);
                os.close();
            }
        });

        mockServer.start();

        // Initialize Selaenium Headless Chrome Driver
        Map<String, Object> googOpts = Map.of(
            "args", List.of("--headless=new", "--no-sandbox", "--disable-dev-shm-usage")
        );
        Map<String, Object> caps = Map.of(
            "browserName", "chrome",
            "goog:chromeOptions", googOpts
        );
        driver = RemoteWebDriver.localChrome(caps);
        driver.implicitlyWait(2000);
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
        if (mockServer != null) {
            mockServer.stop(0);
        }
    }

    @Test
    public void testProductCardComponentInteraction() {
        driver.get("http://localhost:" + mockPort + "/product-card");

        assertEquals("Product Component Test", driver.getTitle());

        WebElement title = driver.findElement(By.className("product-title"));
        assertEquals("Flamup Classic Denim Jacket", title.getText());

        WebElement price = driver.findElement(By.className("product-price"));
        assertEquals("$49.99", price.getText());

        WebElement status = driver.findElement(By.id("cart-status"));
        assertEquals("Empty", status.getText());

        WebElement addButton = driver.findElement(By.id("add-to-cart-btn"));
        addButton.click();

        assertEquals("Item Added!", status.getText());
    }

    @Test
    public void testRegistrationComponentFormSubmission() {
        driver.get("http://localhost:" + mockPort + "/registration-form");

        WebElement firstNameInput = driver.findElement(By.id("first-name"));
        firstNameInput.sendKeys("Jane");

        WebElement emailInput = driver.findElement(By.id("email"));
        emailInput.sendKeys("jane@example.com");

        WebElement submitBtn = driver.findElement(By.id("submit-btn"));
        submitBtn.click();

        WebElement msg = driver.findElement(By.id("msg"));
        assertEquals("Registered Successfully!", msg.getText());
    }
}
