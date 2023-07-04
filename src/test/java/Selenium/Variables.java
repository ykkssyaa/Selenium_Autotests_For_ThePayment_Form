package Selenium;

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
}

class Titles{
    static public String MakeOrderPageTitle = "Создание заказа";
    static public final String PaymentPageTitle = "Страница оплаты";
    static public final String PaymentResultPageTitle = "Результат операции";
    static public final String TermsOfTheOfferTitle = "Оферта";

    static public final String PassedPaymentStatusTitle = "Заказ оплачен";
}

