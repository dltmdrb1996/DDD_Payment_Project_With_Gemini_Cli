package com.lsg.paymentwithgeminiclitool.domain.payment

@JvmInline
value class CardInfo(val value: String) {
    init {
        require(value.matches(Regex("^\\d{4}-\\d{4}-\\d{4}-\\d{4}$"))) {
            "유효하지 않은 카드 정보 형식입니다."
        }
    }
}
