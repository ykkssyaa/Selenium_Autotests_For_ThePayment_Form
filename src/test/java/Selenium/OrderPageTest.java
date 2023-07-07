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
import org.openqa.selenium.support.ui.WebDriverWait;


import java.time.Duration;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class OrderPageTest {

    static private EdgeDriver driver;
    static boolean DEBUG = false;


    @BeforeAll
    static void start(){
        //.withBuildCheckDisabled(true)
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
    }

    @DisplayName("Testing Input Amount")
    @Nested
    class InputAmountTest{

        @BeforeEach
        void start(){
            driver.manage().timeouts().implicitlyWait(Duration.ofMillis(1000));
        }

        @org.jetbrains.annotations.NotNull
        private static Stream<Arguments> CorrectAmount() {
            return Stream.of(
                    Arguments.of("1000", "5,00", "1 005,00"),
                    Arguments.of("1500", "7,50", "1 507,50"),
                    Arguments.of("150,55", "0,75", "151,30"),
                    Arguments.of("150.55", "0,75", "151,30")
                    //Arguments.of("", "", ""),
            );
        }

        @ParameterizedTest
        @MethodSource("CorrectAmount")
        void testInputCorrectAmount(String amount, String commission, String total){

            WebElement amountElem = driver.findElement(By.id("amountControl"));
            amountElem.sendKeys(amount);

            // Проверяю коммиссию
            WebElement commissionElem = driver.findElement(By.id("totalCommission"));
            assertEquals(commission, commissionElem.getText());

            // Проверяю полную сумму
            WebElement totalAmountElem = driver.findElement(By.id("totalAmount"));
            assertEquals(total, totalAmountElem.getText());
        }

        @org.jetbrains.annotations.NotNull
        private static Stream<Arguments> UncorrectAmount() {
            return Stream.of(
                    Arguments.of("100,5555", "0,50", "101,05"),
                    Arguments.of("10,1111", "0,05", "10,16"),
                    Arguments.of("10,00999", "0,05", "100,05"),
                    Arguments.of("10,4444", "0,05", "10,49")
                    //Arguments.of("", "", ""),
            );
        }

        @ParameterizedTest
        @MethodSource("UncorrectAmount")
        void testInputUncorrectAmount(String amount, String commission, String total){

            WebElement amountElem = driver.findElement(By.id("amountControl"));
            amountElem.sendKeys(amount);

            // Проверяю коммиссию
            WebElement commissionElem = driver.findElement(By.id("totalCommission"));
            assertEquals(commission, commissionElem.getText());

            // Проверяю полную сумму
            WebElement totalAmountElem = driver.findElement(By.id("totalAmount"));
            assertEquals(total, totalAmountElem.getText());

        }

        @org.jetbrains.annotations.NotNull
        private static Stream<Arguments> LongAmount() {
            return Stream.of(
                    Arguments.of("100 000 000"),
                    Arguments.of("999999999999999"),
                    Arguments.of("1000000000000000"),
                    Arguments.of("10 000 001")
                    //Arguments.of("", "", ""),
            );
        }
        @ParameterizedTest
        @MethodSource("LongAmount")
        void testInputLongAmount(String amount){

            WebElement amountElem = driver.findElement(By.id("amountControl"));
            amountElem.sendKeys(amount);
            // После ввода кликаю на другое поле, чтоб высветилась ошибка
            driver.findElement(By.id("description")).click();

            // Проверка, что не переполнено поле ввода
            assertTrue(driver.findElement(By.id("amount_kop")).getText().length() <= 15);

            // Проверяем, что появилась ошибка
            WebElement amountControlErrorElem = new WebDriverWait(driver, Duration.ofMillis(500)).
                    until(driver -> driver.findElement(By.id("amountControl")));

            assertEquals("true", amountControlErrorElem.getAttribute("aria-invalid"));
        }

        @Test
        void InputWithoutIntegerPart(){
            WebElement amountElem = driver.findElement(By.id("amountControl"));
            amountElem.sendKeys(".90");
            // После ввода кликаю на другое поле, чтоб высветилась ошибка
            driver.findElement(By.id("description")).click();


            assertEquals("0,90", driver.findElement(By.id("totalAmount")).getText());

            // Проверяем, что появилась ошибка
            WebElement amountControlErrorElem = new WebDriverWait(driver, Duration.ofMillis(500)).
                    until(driver -> driver.findElement(By.id("amountControl")));
        }

        @Test
        void InputUncorrectSimbols(){

            WebElement amountElem = driver.findElement(By.id("amountControl"));
            amountElem.sendKeys("QWERTY`=-/z~+");

            assertEquals("0,00", driver.findElement(By.id("totalAmount")).getText());

            // Проверяем, что появилась ошибка
            WebElement amountControl = new WebDriverWait(driver, Duration.ofMillis(500)).
                    until(driver -> driver.findElement(By.id("amountControl")));


            assertTrue(amountControl.getAttribute("value").isEmpty());
        }


    }

    @Nested
    class MakeOrderTest{

        static private WebElement submitButton;
        static private WebElement amountElem;
        static private WebElement descriptionElem;

        static private final String defaultDescription = Variables.TestData.defaultDescription;
        static private final String defaultAmount = Variables.TestData.defaultAmount;

        @BeforeEach
        void start(){

            submitButton = driver.findElement(By.id("submitButton"));
            amountElem = driver.findElement(By.id("amountControl"));
            descriptionElem = driver.findElement(By.id("description"));

            driver.manage().timeouts().implicitlyWait(Duration.ofMillis(500));
        }

        @RepeatedTest(3)
        void MakeOrderPositiveTest(){

            // Ввод значений
            amountElem.sendKeys(defaultAmount);
            descriptionElem.sendKeys(defaultDescription);

            String ID = driver.findElement(By.name("reference")).getAttribute("value");
            String commission = driver.findElement(By.id("fee_kop")).getAttribute("value");
            String amount = driver.findElement(By.id("amount_kop")).getAttribute("value");

            // Клик по кнопке отправки
            submitButton.click();
            driver.manage().timeouts().implicitlyWait(Duration.ofMillis(500));

            // Проверка, что открыта страница оплаты
            assertEquals(Titles.PaymentPageTitle, driver.getTitle());

            String new_description =driver.findElement(By.xpath("//*[@id=\"descList\"]/li[1]/span[2]")).getAttribute("innerHTML");
            String new_ID = driver.findElement(By.xpath("//*[@id=\"descList\"]/li[2]/span[2]")).getAttribute("innerHTML");

            // Проверка, что ID и описание совпадают с начальной страницей
            assertEquals(defaultDescription, new_description);
            assertEquals(ID, new_ID);

            String new_com = driver.findElement(By.name("fee")).getAttribute("value");
            String new_amount = driver.findElement(By.name("amount")).getAttribute("value");

            // Проверка, что значение комиссии и значение оплаты совпадают
            assertEquals(commission, new_com);
            assertEquals(amount, new_amount);

        }

        @Test
        void MakeOrderWithEmptyInputLines(){

            submitButton.click();
            driver.manage().timeouts().implicitlyWait(Duration.ofMillis(200));

            // Переход на новую страницу не осуществляется
            assertEquals(Titles.MakeOrderPageTitle, driver.getTitle());

            // Появляются ошибки "Поле обязательно"
            assertFalse(driver.findElements(By.id("description-error")).isEmpty());
            assertFalse(driver.findElements(By.id("amountControl-error")).isEmpty());
        }

        @Test
        void MakeOrderWithEmptyDescription(){
            amountElem.sendKeys(defaultAmount);

            submitButton.click();
            driver.manage().timeouts().implicitlyWait(Duration.ofMillis(200));

            // Переход на новую страницу не осуществляется
            assertEquals(Titles.MakeOrderPageTitle, driver.getTitle());

            // Появляется ошибка "Поле обязательно" под описанием
            assertFalse(driver.findElements(By.id("description-error")).isEmpty());
            assertTrue(driver.findElements(By.id("amountControl-error")).isEmpty());
        }
        @Test
        void MakeOrderWithEmptyAmount(){
            descriptionElem.sendKeys(defaultDescription);

            submitButton.click();
            driver.manage().timeouts().implicitlyWait(Duration.ofMillis(200));

            // Переход на новую страницу не осуществляется
            assertEquals(Titles.MakeOrderPageTitle, driver.getTitle());

            // Появляется ошибка "Поле обязательно" под описанием
            assertTrue(driver.findElements(By.id("description-error")).isEmpty());
            assertFalse(driver.findElements(By.id("amountControl-error")).isEmpty());
        }

    }

    @AfterAll
    static void close(){
        driver.quit();
    }
}
