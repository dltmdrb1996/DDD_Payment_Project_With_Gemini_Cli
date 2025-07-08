package com.lsg.paymentwithgeminiclitool.domain.payment

/**
 * 결제 상태를 나타내는 sealed interface
 * 결제는 요청됨, 완료됨, 취소됨, 환불됨, 부분 환불됨 상태를 가질 수 있습니다.
 */
sealed interface PaymentStatus {
    data object REQUESTED : PaymentStatus
    data object COMPLETED : PaymentStatus
    data object CANCELED : PaymentStatus
    data object REFUNDED : PaymentStatus
    data object PARTIALLY_REFUNDED : PaymentStatus
}