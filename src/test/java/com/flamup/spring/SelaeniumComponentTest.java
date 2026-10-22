package com.flamup.spring;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.RemoteWebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Fast Selaenium component tests using standalone HTML mock fixtures
 * rendered directly by headless Chrome via Selaenium RemoteWebDriver.
 */
public class SelaeniumComponentTest {

    private static RemoteWebDriver driver;

    @BeforeAll
    static void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--no-sandbox", "--disable-dev-shm-usage");
        driver = RemoteWebDriver.localChrome(options.asMap());
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @DisplayName("Component Test: Navigation Bar component renders correctly")
    void testNavigationBarComponent() throws IOException {
        String navHtml = """
            <!DOCTYPE html>
            <html>
            <head><title>NavBar Mock Component</title></head>
            <body>
              <nav id="navbar">
                <div class="brand">FlamUp</div>
                <ul class="nav-links">
                  <li><a href="/products" id="nav-products">Products</a></li>
                  <li><a href="/cart" id="nav-cart">Cart (<span id="cart-count">2</span>)</a></li>
                  <li><a href="/login" id="nav-login">Login</a></li>
                </ul>
              </nav>
            </body>
            </html>
            """;

        File tempFile = createTempHtmlFile(navHtml);
        driver.get(tempFile.toURI().toString());

        WebElement brand = driver.findElement(By.className("brand"));
        assertEquals("FlamUp", brand.getText());

        WebElement cartCount = driver.findElement(By.id("cart-count"));
        assertEquals("2", cartCount.getText());
    }

    @Test
    @DisplayName("Component Test: Product Card component interactions and discount badge")
    void testProductCardComponent() throws IOException {
        String productHtml = """
            <!DOCTYPE html>
            <html>
            <head><title>Product Card Mock Component</title></head>
            <body>
              <div class="product-card" id="prod-101">
                <img src="shirt.jpg" alt="Casual Shirt" class="prod-img"/>
                <h3 class="prod-title">Slim Fit Denim Shirt</h3>
                <span class="prod-price">$29.99</span>
                <span class="prod-discount">20% OFF</span>
                <button id="add-to-cart-btn" onclick="document.getElementById('status').innerText = 'Added to Cart'">Add to Cart</button>
                <div id="status"></div>
              </div>
            </body>
            </html>
            """;

        File tempFile = createTempHtmlFile(productHtml);
        driver.get(tempFile.toURI().toString());

        WebElement title = driver.findElement(By.className("prod-title"));
        assertEquals("Slim Fit Denim Shirt", title.getText());

        WebElement discount = driver.findElement(By.className("prod-discount"));
        assertTrue(discount.getText().contains("20% OFF"));

        WebElement btn = driver.findElement(By.id("add-to-cart-btn"));
        btn.click();

        WebElement status = driver.findElement(By.id("status"));
        assertEquals("Added to Cart", status.getText());
    }

    @Test
    @DisplayName("Component Test: Registration Form component validation & submission mock")
    void testRegistrationFormComponent() throws IOException {
        String regHtml = """
            <!DOCTYPE html>
            <html>
            <head><title>Registration Form Component</title></head>
            <body>
              <form id="reg-form" onsubmit="event.preventDefault(); document.getElementById('success-msg').style.display='block';">
                <input type="text" id="firstName" name="firstName" placeholder="First Name" />
                <input type="text" id="lastName" name="lastName" placeholder="Last Name" />
                <input type="email" id="email" name="email" placeholder="Email" />
                <input type="password" id="password" name="password" placeholder="Password" />
                <button type="submit" id="submit-btn">Register</button>
              </form>
              <div id="success-msg" style="display:none;">Registration Successful</div>
            </body>
            </html>
            """;

        File tempFile = createTempHtmlFile(regHtml);
        driver.get(tempFile.toURI().toString());

        WebElement emailInput = driver.findElement(By.id("email"));
        emailInput.sendKeys("user@flamup.com");

        WebElement submitBtn = driver.findElement(By.id("submit-btn"));
        submitBtn.click();

        WebElement msg = driver.findElement(By.id("success-msg"));
        assertTrue(msg.isDisplayed());
        assertEquals("Registration Successful", msg.getText());
    }

    private File createTempHtmlFile(String content) throws IOException {
        File tempFile = File.createTempFile("selaenium-component-", ".html");
        tempFile.deleteOnExit();
        Files.writeString(tempFile.toPath(), content);
        return tempFile;
    }
}
