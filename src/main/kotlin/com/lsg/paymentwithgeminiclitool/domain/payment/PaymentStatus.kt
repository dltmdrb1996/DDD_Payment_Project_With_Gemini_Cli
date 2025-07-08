package com.lsg.paymentwithgeminiclitool.domain.payment

/**
 * 결제 상태를 나타내는 sealed interface
 * 결제는 요청됨, 완료됨, 취소됨 세 가지 상태를 가질 수 있습니다.
 */
sealed interface PaymentStatus {
    /** 결제 요청됨 */
    data object REQUESTED : PaymentStatus

    /** 결제 완료됨 */
    data object COMPLETED : PaymentStatus

    /** 결제 취소됨 */
    data object CANCELED : PaymentStatus
}
