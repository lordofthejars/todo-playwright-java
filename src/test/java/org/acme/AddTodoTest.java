package org.acme;

import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.net.URL;
import java.nio.file.Paths;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@QuarkusTest
public class AddTodoTest {

    static Playwright playwright;
    static Browser browser;

    // New instance for each test method.
    BrowserContext context;
    Page page;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
            new BrowserType.LaunchOptions().setSlowMo(2000).setHeadless(false)
            );
    }

    @AfterAll
    static void closeBrowser() {
        playwright.close();
    }
    
    @BeforeEach
    void createContextAndPage() {
        context = browser.newContext();

        context.tracing().start(new Tracing.StartOptions()
            .setScreenshots(true)
            .setSnapshots(true));

        page = context.newPage();
    }

    @AfterEach
    void closeContext(TestInfo testInfo) {
        context.tracing().stop(new Tracing.StopOptions()
            .setPath(Paths.get("trace" + testInfo.getDisplayName() +".zip")));
        context.close();
    }

    @TestHTTPResource("todo.html") 
    URL url;

    @Test
    public void addTodo() {
        page.navigate(url.toExternalForm());

        page.onRequest(request -> System.out.println(">> " + request.method() + " " + request.url()));
        page.onResponse(response -> System.out.println("<<" + response.status() + " " + response.url()));

        page.locator("[placeholder=\"What needs to be done\\?\"]").click();
        // Fill [placeholder="What needs to be done\?"]
        page.locator("[placeholder=\"What needs to be done\\?\"]").fill("Attend expoQA");
        // Press Enter
        

        page.waitForResponse(r -> {
            return r.status() == 201 && r.url().endsWith("/api/");
        } 
        , () -> {
            page.locator("[placeholder=\"What needs to be done\\?\"]").press("Enter");
        });
    }

}
