package com.lsg.paymentwithgeminiclitool.domain.payment

enum class PaymentMethodType(val value: String) {
    MY_POINT("MyPoint"),
    MY_MONEY("MyMoney"),
    CARD_EASY("CardEasy"),
    BANK_EASY("BankEasy"),
    MY_POINT_COMPOSITE("MyPointComposite"),
    MY_MONEY_COMPOSITE("MyMoneyComposite"),
    MY_POINT_AND_MONEY_COMPOSITE("MyPointAndMoneyComposite");

    companion object {
        fun from(value: String): PaymentMethodType = values().first { it.value == value }
    }
}
