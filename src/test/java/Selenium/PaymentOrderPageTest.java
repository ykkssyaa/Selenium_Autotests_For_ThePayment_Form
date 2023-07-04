package Selenium;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeDriverService;
import org.openqa.selenium.edge.EdgeOptions;

import java.time.Duration;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class PaymentOrderPageTest {

    static private EdgeDriver driver;
    static boolean DEBUG = false;

    @BeforeAll
    static void start(){
        //.withBuildCheckDisabled(true)
        EdgeDriverService service = new EdgeDriverService.Builder()
                .withLogOutput(System.out)
                //.withBuildCheckDisabled(true)
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
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(500));

        // Проверяю, что перешел на страницу
        assertEquals(Titles.PaymentPageTitle, driver.getTitle());
    }


    @Nested
    class InputDataTest{

        // TODO: Протестировать ввод некорректных данных в поля оплаты

        // TODO: Протестировать что при вводе номеров разных платежных систем меняются CVV, CVC, ППК

    }

    @Nested
    class PaymentOrderTest{

        private static WebElement InputNumber;
        private static  WebElement InputValidityPeriod;
        private static  WebElement InputCVV;

        private static  WebElement PaymentButton;

        @BeforeEach
        void start(){

            InputNumber = driver.findElement(By.id("pan"));
            InputValidityPeriod = driver.findElement(By.id("cardDate"));
            InputCVV = driver.findElement(By.id("temp-cvc"));

            PaymentButton = driver.findElement(By.id("submitButton"));
        }

        @org.jetbrains.annotations.NotNull
        private static Stream<Arguments> CorrectData() {

            String defaultValidityPeriod = Variables.TestData.defaultValidityPeriod;
            String defaultCVV = Variables.TestData.defaultCVV;

            return Stream.of(
                    Arguments.of(Variables.TestData.cardMasterCard,defaultValidityPeriod, defaultCVV),
                    Arguments.of(Variables.TestData.cardMir,defaultValidityPeriod, defaultCVV),
                    Arguments.of(Variables.TestData.cardVisa,defaultValidityPeriod, defaultCVV),
                    Arguments.of(Variables.TestData.cardMaestro,defaultValidityPeriod, defaultCVV)
                    //Arguments.of("", "", ""),
            );
        }

        @ParameterizedTest
        @MethodSource("CorrectData")
        void PaymentOrderPositive(String cardNumber, String date, String CVV){

            InputNumber.sendKeys(cardNumber);
            InputValidityPeriod.sendKeys(date);
            InputCVV.sendKeys(CVV);

            String totalAmount = driver.findElement(By.id("totalAmountValue")).getText();

            PaymentButton.click();
            driver.manage().timeouts().implicitlyWait(Duration.ofMillis(300));

            // Проверка, что перешли на страницу результатов оплаты
            assertEquals(Titles.PaymentResultPageTitle, driver.getTitle());

            // Проверка, что сумма оплаты такая же
            String CurrentTotalAmount = driver.findElement(
                    By.xpath("/html/body/div/div/div[2]/div[1]/div[2]/ul/li[3]/span[2]/span[1]")).getAttribute("innerHTML");

            assertEquals(totalAmount, CurrentTotalAmount);

            String CurrentCardNumber = driver.findElement(
                    By.xpath("/html/body/div/div/div[2]/div[1]/div[2]/ul/li[2]/span[2]")).getAttribute("innerHTML");

            // Проверка, что карты сходятся
            assertEquals(cardNumber.substring(cardNumber.length()-4) ,
                    CurrentCardNumber.substring(2));

            // Проверка, что описание сходится
            assertEquals(Variables.TestData.defaultDescription,
                    driver.findElement(By.xpath("//*[@id=\"descList\"]/li/span[2]")).getAttribute("innerHTML"));

            // Проверка, что оплата прошла
            String StatusTitle = driver.findElement(By.className("notify__status-text")).getText();
            assertEquals(Titles.PassedPaymentStatusTitle, StatusTitle);

        }

        @Test
        void PayOrderWithoutNumber(){

            InputValidityPeriod.sendKeys(Variables.TestData.defaultValidityPeriod);
            InputCVV.sendKeys(Variables.TestData.defaultCVV);

            PaymentButton.click();
            driver.manage().timeouts().implicitlyWait(Duration.ofMillis(300));

            assertEquals(Titles.PaymentPageTitle, driver.getTitle());
            assertFalse(driver.findElements(By.id("pan-error")).isEmpty());
        }

        @Test
        void PayOrderWithoutDate(){

            InputNumber.sendKeys(Variables.TestData.cardVisa);
            InputCVV.sendKeys(Variables.TestData.defaultCVV);

            PaymentButton.click();
            driver.manage().timeouts().implicitlyWait(Duration.ofMillis(300));

            assertEquals(Titles.PaymentPageTitle, driver.getTitle());
            assertFalse(driver.findElements(By.id("cardDate-error")).isEmpty());
        }

        @Test
        void PayOrderWithoutCVV(){

            InputValidityPeriod.sendKeys(Variables.TestData.defaultValidityPeriod);
            InputNumber.sendKeys(Variables.TestData.cardVisa);

            PaymentButton.click();
            driver.manage().timeouts().implicitlyWait(Duration.ofMillis(300));

            assertEquals(Titles.PaymentPageTitle, driver.getTitle());
            assertFalse(driver.findElements(By.id("cvv-error")).isEmpty());
        }

        @Nested
        class PayOrderWithEmptyLines{
            @BeforeEach
            void start(){
                PaymentButton.click();
                driver.manage().timeouts().implicitlyWait(Duration.ofMillis(500));

                assertEquals(Titles.PaymentPageTitle, driver.getTitle());
            }

            @Test
            void CVV_Error(){
                assertFalse(driver.findElements(By.id("cvv-error")).isEmpty());
            }
            @Test
            void CardDate_Error(){
                assertFalse(driver.findElements(By.id("cardDate-error")).isEmpty());
            }
            @Test
            void Pan_Error(){
                assertFalse(driver.findElements(By.id("pan-error")).isEmpty());
            }
        }

    }

    @Nested
    class OtherTest{

        @Test
        void  TermsOfTheOfferTest(){

            WebElement link = driver.findElement(By.className("oferta__link"));

            driver.get(link.getAttribute("href"));

            driver.manage().timeouts().implicitlyWait(Duration.ofMillis(500));
            assertEquals(Titles.TermsOfTheOfferTitle, driver.getTitle());

        }
    }

    @AfterAll
    static void close(){
        driver.quit();
    }

}
