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
public class ClearCompletedTodoTest {
    
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
        context = browser.newContext(new Browser.NewContextOptions().setRecordVideoDir(Paths.get("videos/")));

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
    public void clearTodo() {
        page.navigate(url.toExternalForm());

        page.locator("input[type=\"checkbox\"]").nth(2).check();
        page.locator("text=Clear completed").click();
        assertThat(page.locator(".todo")).hasCount(2);
    }

}
