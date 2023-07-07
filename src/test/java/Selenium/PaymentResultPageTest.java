package Selenium;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeDriverService;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PaymentResultPageTest {


    static private EdgeDriver driver;
    static boolean DEBUG = false;
    static WebElement emailButton;

    @BeforeAll
    static void start(){

        EdgeDriverService service = new EdgeDriverService.Builder()
                .withLogOutput(System.out)
                .build();

        EdgeOptions options = new EdgeOptions();

        if (!DEBUG)
            options.addArguments("--headless");

        driver = new EdgeDriver(service, options);
    }

    @BeforeEach
    void setup(){

        driver.get("https://test.paygine.com/webapi/UniPayForm?sector=5073&code=643");
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(500));

        // Заполняю тестовые данные
        driver.findElement(By.id("amountControl")).sendKeys(Variables.TestData.defaultAmount);
        driver.findElement(By.id("description")).sendKeys(Variables.TestData.defaultDescription);

        // Отправляю данные
        driver.findElement(By.id("submitButton")).click();
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(1000));

        // Проверяю, что перешел на страницу оплаты
        assertEquals(Titles.PaymentPageTitle, driver.getTitle());

        driver.findElement(By.id("pan")).sendKeys(Variables.TestData.cardVisa);
        driver.findElement(By.id("cardDate")).sendKeys(Variables.TestData.defaultValidityPeriod);
        driver.findElement(By.id("temp-cvc")).sendKeys(Variables.TestData.defaultCVV);

        driver.findElement(By.id("submitButton")).click();
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(400));

        // Проверяю, что перешел на страницу результатов
        assertEquals(Titles.PaymentResultPageTitle, driver.getTitle());

        // Проверка, что оплата прошла
        String StatusTitle = driver.findElement(By.className("notify__status-text")).getText();
        assertEquals(Titles.PassedPaymentStatusTitle, StatusTitle);

        // Проверка, что описание сходится
        assertEquals(Variables.TestData.defaultDescription,
                driver.findElement(By.xpath("//*[@id=\"descList\"]/li/span[2]")).getAttribute("innerHTML"));


        emailButton = driver.findElement(By.xpath("//*[@id=\"showEmail\"]/button"));
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(500));

    }

    @Test
    void MakeEmailReceiptPositive(){

        emailButton.click();
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(100));

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(6));
        WebElement emailTextBox = wait.until(ExpectedConditions.elementToBeClickable(By.name("emailTo")));
        emailTextBox.sendKeys(Variables.TestData.defaultEmail);
        emailTextBox.sendKeys(Keys.ENTER);

        WebElement emailElem =  wait.until(ExpectedConditions.
                visibilityOf(driver.findElement(By.id("emailText"))));

        String email = emailElem.getAttribute("innerHTML");
        assertEquals(Variables.TestData.defaultEmail, email);


        String passedEmailText = driver.findElement(By.id("emailSendResultTextSuccess"))
                .getAttribute("innerHTML");
        assertEquals("Квитанция будет отправлена на почту", passedEmailText);

    }

    @Test
    void MakeEmailReceiptWithButton(){

        emailButton.click();
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(300));

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        WebElement emailTextBox = wait.until(ExpectedConditions.elementToBeClickable(By.name("emailTo")));
        emailTextBox.sendKeys(Variables.TestData.defaultEmail);

        driver.findElement(By.id("sendEmailButton")).click();

        WebElement emailElem =  wait.until(ExpectedConditions.
                visibilityOf(driver.findElement(By.id("emailText"))));

        String email = emailElem.getAttribute("innerHTML");
        assertEquals(Variables.TestData.defaultEmail, email);


        String passedEmailText = driver.findElement(By.id("emailSendResultTextSuccess"))
                .getAttribute("innerHTML");
        assertEquals("Квитанция будет отправлена на почту", passedEmailText);

    }

    @Test
    void MakeEmailReceiptWithEmptyLine(){
        emailButton.click();
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(300));

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        WebElement emailTextBox = wait.until(ExpectedConditions.elementToBeClickable(By.name("emailTo")));
        emailTextBox.sendKeys(Keys.ENTER);

        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(1000));

        WebElement error =  wait.until(ExpectedConditions.
                presenceOfElementLocated(By.id("email-error")));

        assertEquals("Поле обязательно", error.getAttribute("innerHTML"));
    }


    @org.jetbrains.annotations.NotNull
    private static Stream<Arguments> UncorrectEmail() {
        return Stream.of(
                Arguments.of("t@t.r"),
                Arguments.of("@mail.ru"),
                Arguments.of("test@ma@mail.com"),
                Arguments.of("mail@.net"),
                Arguments.of("-mail")
                //Arguments.of("", "", ""),
        );
    }

    @ParameterizedTest
    @MethodSource("UncorrectEmail")
    void MakeEmailReceiptWithUncorrectData(String email){

        emailButton.click();
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(500));

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(6));

        WebElement emailTextBox = wait.until(ExpectedConditions.elementToBeClickable(By.name("emailTo")));

        emailTextBox.sendKeys(email);
        emailTextBox.sendKeys(Keys.ENTER);

        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(1000));

        wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        WebElement error =  wait.until(ExpectedConditions.presenceOfElementLocated(By.id("email-error")));

        assertEquals("Email указан неверно", error.getAttribute("innerHTML"));

    }


    @AfterAll
    static void sleep(){
        driver.quit();
    }
}
