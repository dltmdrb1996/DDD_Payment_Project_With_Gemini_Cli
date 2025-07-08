package com.lsg.paymentwithgeminiclitool.domain.payment

@JvmInline
value class BankInfo(val value: String) {
    init {
        require(value.isNotBlank() && value.contains(" ")) {
            "유효하지 않은 은행 정보 형식입니다. (예: '은행명 계좌번호')"
        }
    }
}
