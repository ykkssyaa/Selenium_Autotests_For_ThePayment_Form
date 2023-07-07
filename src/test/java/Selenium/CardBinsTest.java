package Selenium;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chromium.ChromiumDriverLogLevel;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeDriverService;
import org.openqa.selenium.edge.EdgeOptions;

import java.io.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class CardBinsTest {

    static private EdgeDriver driver;
    static boolean DEBUG = false;

    static String file_dir = "cardBinsLogo.txt";
    static String test_file = "allBanksBins";

    private static WebElement InputNumber;
    private static WebElement BankLogo;

    @BeforeAll
    static void start(){

        EdgeDriverService service = new EdgeDriverService.Builder()
                .withLogOutput(System.out)
                .withLoglevel(ChromiumDriverLogLevel.OFF)
                .build();

        EdgeOptions options = new EdgeOptions();

        if (!DEBUG)
            options.addArguments("--headless");

        driver = new EdgeDriver(service, options);

        driver.get("https://test.paygine.com/webapi/UniPayForm?sector=5073&code=643");
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(500));

        // Заполняю тестовые данные
        driver.findElement(By.id("amountControl")).sendKeys(Variables.TestData.defaultAmount);
        driver.findElement(By.id("description")).sendKeys(Variables.TestData.defaultDescription);

        // Отправляю данные
        driver.findElement(By.id("submitButton")).click();
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(1000));

        // Проверяю, что перешел на страницу
        assertEquals(Titles.PaymentPageTitle, driver.getTitle());

        InputNumber = driver.findElement(By.id("pan"));
        BankLogo = driver.findElement(By.className("input__ps"));
    }

    @BeforeEach
    void setup(){
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(200));
        InputNumber.clear();
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(100));
    }

    private static boolean IsValid(String str){
        if (str.isEmpty()) return false;

        return str.charAt(0) != '/' || str.charAt(1) != '/';
    }

    private static String RemoveApostrophesAndCommas(String str){
        return str.replace(",", "").replace("'", "");
    }

    private static Stream<Arguments> BINS() throws FileNotFoundException {

        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream is = classloader.getResourceAsStream(file_dir);

        assert is != null;
        Stream<String> lines = new BufferedReader(new InputStreamReader(is)).lines();

        return lines
                .filter(CardBinsTest::IsValid)
                .map(CardBinsTest::RemoveApostrophesAndCommas)
                .map(line -> Arguments.of(line.split(" ")))
                ;
    }

    static Map<String, ArrayList<String>> map = new HashMap<>();

    @Disabled
    @ParameterizedTest
    @MethodSource("BINS")
    void tt(String bin, String bank){

        InputNumber.sendKeys(bin);
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(200));

        String src = BankLogo.getAttribute("style");
        src = src.substring(23, src.length()-3);

        ArrayList<String> bankLogoSrc = map.get(bank);

        if (bankLogoSrc == null){
            bankLogoSrc = new ArrayList<String>();
            bankLogoSrc.add(src);

            map.put(bank, bankLogoSrc);
            return;
        }

        String finalSrc = src;
        if (bankLogoSrc.stream().noneMatch(item -> {
            if (item == null) return false;
            return item.equals(finalSrc);
        })){
            bankLogoSrc.add(src);
        }
    }

    @ParameterizedTest
    @MethodSource("BINS")
    void BanksLogoTest(String bin, String bank){
        InputNumber.sendKeys(bin);
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(100));

        String src = BankLogo.getAttribute("style");
        src = src.substring(23, src.length()-3);


        assertEquals(Variables.sources.get(bank), src);
    }

    @AfterAll
    static void close(){

        driver.quit();
/*        for (String key: map.keySet()) {

            List<String> list = map.get(key);
            System.out.print(key + " - ");
            for (String elem: list) {
                System.out.print(" " + elem + " ");
            }
            System.out.println();

        }*/
    }
}
