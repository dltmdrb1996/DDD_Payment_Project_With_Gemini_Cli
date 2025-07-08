package com.lsg.paymentwithgeminiclitool.domain.payment

enum class SubPaymentType(val value: String) {
    MY_POINT("MyPoint"),
    MY_MONEY("MyMoney"),
    CARD_EASY("CardEasy"),
    BANK_EASY("BankEasy");

    companion object {
        fun from(value: String): SubPaymentType = entries.first { it.value == value }
    }
}