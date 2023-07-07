package Selenium;

import java.util.HashMap;
import java.util.Map;

public class Variables {
    static class TestData{
        static public final String defaultDescription = "QAZxsw1234567890()-+";
        static public final String defaultAmount = "1000";

        static public final String cardVisa = "4000000000091001";
        static public final String cardMasterCard = "5000000000061001";
        static public final String cardMaestro = "600000000000041001";
        static public final String cardMir = "2200000000011001";

        static public final String defaultValidityPeriod = "1124";
        static public final String defaultCVV = "123";

        static public final String defaultEmail = "test@mail.ru";
    }

    static Map<String, String> sources = BanksBins.createMap();

}

class Titles{
    static public String MakeOrderPageTitle = "Создание заказа";
    static public final String PaymentPageTitle = "Страница оплаты";
    static public final String PaymentResultPageTitle = "Результат операции";
    static public final String TermsOfTheOfferTitle = "Оферта";

    static public final String PassedPaymentStatusTitle = "Заказ оплачен";
}

class BanksBins{
    public static Map<String,String> createMap(){
        Map<String,String> myMap = new HashMap<String,String>();

        myMap.put("rosbank", "/static/common/img/logos/banks/colored/bank100000000012.svg");
        myMap.put("rosgosstrah", "/static/common/img/logos/banks/colored/rosgosstrah.png");
        myMap.put("alfabank", "/static/common/img/logos/banks/colored/bank100000000008.svg");
        myMap.put("city", "/static/common/img/logos/banks/colored/bank100000000128.svg");
        myMap.put("homecredit", "/static/common/img/logos/banks/colored/bank100000000024.svg");
        myMap.put("vtb", "/static/common/img/logos/banks/colored/bank100000000005.svg");
        myMap.put("novikombank", "/static/common/img/logos/banks/colored/bank100000000177.svg");
        myMap.put("otkrytie", "/static/common/img/logos/banks/colored/bank100000000015.svg");
        myMap.put("zenit", "/static/common/img/logos/banks/colored/bank100000000045.svg");
        myMap.put("rosselhoz", "/static/common/img/logos/banks/colored/bank100000000020.svg");
        myMap.put("gazprombank", "/static/common/img/logos/banks/colored/bank100000000001.svg");
        myMap.put("pochtabank", "/static/common/img/logos/banks/colored/bank100000000016.svg");
        myMap.put("mts", "/static/common/img/logos/banks/colored/bank100000000017.svg");
        myMap.put("russtandard", "/static/common/img/logos/banks/colored/bank100000000500.svg");
        myMap.put("uralsib", "/static/common/img/logos/banks/colored/bank100000000026.svg");
        myMap.put("sberbank", "/static/common/img/logos/banks/colored/bank100000000111.svg");
        myMap.put("unicredit", "/static/common/img/logos/banks/colored/bank100000000030.svg");
        myMap.put("bcs", "/static/common/img/logos/banks/colored/bcs-bank.svg");
        myMap.put("vozrozhdenie", "/static/common/img/logos/banks/colored/vozrozdenie.svg");
        myMap.put("raiffeisen", "/static/common/img/logos/banks/colored/bank100000000007.svg");
        myMap.put("smp", "/static/common/img/logos/banks/colored/smp-bank.svg");
        myMap.put("otpbank", "/static/common/img/logos/banks/colored/bank100000000018.svg");
        myMap.put("promsvyaz", "/static/common/img/logos/banks/colored/bank100000000010.svg");
        myMap.put("tinkoff", "/static/common/img/logos/banks/colored/bank100000000004.svg");

        return myMap;
    }
}