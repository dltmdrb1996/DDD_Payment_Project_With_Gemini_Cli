package com.lsg.paymentwithgeminiclitool.domain.common

@JvmInline
value class Amount(val value: Long) {
    init {
        require(value > 0) { "금액은 0보다 커야 합니다." }
    }
}
