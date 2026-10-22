# Selaenium Experience Report

## Overview
This report evaluates the Developer Experience (DX) of integrating **Selaenium v0.12.0** (an unofficial, optimized native fork of Selenium) into a Spring Boot and React full-stack application, along with test execution statistics categorized by test class.

---

## 1. Artifacts & Developer Experience (DX) Analysis

### Asked Requirement & Artifacts
The task required integrating Selaenium using two primary release artifacts from `v0.12.0`:
1. `selenium-client-standalone.jar`: The Java client library providing the Selenium W3C WebDriver API interfaces (`WebDriver`, `RemoteWebDriver`, `WebElement`, `By`, `ChromeDriver`, etc.).
2. `libselenium_core-v0.12.0-linux-x86_64.so`: The platform-native binary engine powering low-level driver execution via direct native bindings.

### Developer Experience (DX) Highlights

#### Positives & Strengths
* **Zero Binary Driver Management Needed**: Unlike traditional Selenium which requires downloading and matching specific `chromedriver` / `geckodriver` binaries to host browser versions, Selaenium's native engine (`libselenium_core.so`) manages driver process resolution and launch automatically (`RemoteWebDriver.localChrome()`).
* **Clean API Familiarity**: Selaenium retains 100% standard W3C Selenium API signatures (`driver.get()`, `driver.findElement()`, `By.xpath()`, `WebElement.click()`, etc.), allowing existing Selenium tests to be migrated with zero learning curve.
* **In-Memory & Embedded Server Compatibility**: Selaenium works seamlessly against embedded HTTP servers (such as `com.sun.net.httpserver.HttpServer` or Spring Boot embedded Tomcat), allowing ultra-fast component-level browser testing.

#### Challenges & Technical Nuances
* **Java Version & Bytecode Requirements**:
  * Selaenium `v0.12.0` class files are compiled targeting class version 66.0 (Java 22). Running Selaenium requires OpenJDK 22 or higher.
  * Selaenium utilizes the Java Foreign Function & Memory (FFM) API (`java.lang.foreign.*`). When executing tests under Maven Surefire, JVM native access must be explicitly allowed using `--enable-native-access=ALL-UNNAMED` in `maven-surefire-plugin`.
* **Spring Boot Context & ASM Scanning Compatibility**:
  * Legacy Spring Boot 2.4.x / Spring Framework 5.3 embedded ASM ClassReader caps bytecode parsing at Java 17 (major version 61).
  * **Solution / Workaround**: By configuring Maven Compiler Plugin with `<release>17</release>` for application source files while running the build on OpenJDK 22+, Spring's component scanner reads the bytecode without error while Selaenium native bindings run with full Java 22 FFM capability.
* **Headless Browser Arguments**:
  * Chrome headless mode requires `--headless=new`, `--no-sandbox`, and `--disable-dev-shm-usage` passed via `goog:chromeOptions` when instantiating `RemoteWebDriver.localChrome(caps)`.

---

## 2. Testing Statistics & Breakdown

The test suite consists of both existing application tests and newly created Selaenium component and E2E tests, categorized by test class to illustrate testing emphasis in the final result:

| Category | Test Class / File | Framework | Count | Status |
| :--- | :--- | :--- | :---: | :---: |
| **Existing Frontend Unit Test** | `frontend/src/App.test.js` | React Testing Library / Jest | 1 | **PASS** |
| **Existing Backend Integration Test** | `com.flamup.spring.ApplicationTests` | JUnit 5 / Spring Boot Test | 1 | **PASS** |
| **New Selaenium Component Test** | `com.flamup.spring.ComponentSelaeniumTest` | JUnit 5 / Selaenium v0.12.0 | 2 | **PASS** |
| **New Selaenium Full-Stack E2E Test**| `com.flamup.spring.FullStackSelaeniumE2ETest` | JUnit 5 / Selaenium v0.12.0 | 2 | **PASS** |
| **TOTAL** | | | **6** | **ALL GREEN** |

### Test Class Descriptions & Emphasis

1. **Frontend Unit Tests** (`App.test.js`):
   - Verifies isolated React UI component rendering in JSDOM (e.g., verifying home screen headline and branding).
2. **Backend Context Integration Tests** (`ApplicationTests`):
   - Verifies Spring Boot application context loading, JPA/H2 entity mappings, and Spring Security filter chain initializations.
3. **Selaenium Component Tests** (`ComponentSelaeniumTest`):
   - High-speed component-level browser automation testing against mock HTTP component endpoints.
   - Tests `ProductCard` component interactions (clicks, price assertions, status DOM updates) and `RegistrationForm` submissions.
4. **Selaenium Full-Stack E2E Tests** (`FullStackSelaeniumE2ETest`):
   - Full-stack end-to-end web browser test driving headless Chrome.
   - Validates live application routing, page title rendering, heading assertions, and navigation/login form DOM elements.

---

## 3. Summary & Conclusion
The two-artifact distribution model (`selenium-client-standalone.jar` + `libselenium_core.so`) offers a streamlined setup for native web driver automation in Java 22+ environments. Once JVM native access flags (`--enable-native-access`) and compiler release compatibility (`<release>17</release>`) were configured, all 6 test suites passed cleanly with 100% green builds across frontend and backend layers.
