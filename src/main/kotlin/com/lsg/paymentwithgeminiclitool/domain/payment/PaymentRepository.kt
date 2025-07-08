package com.lsg.paymentwithgeminiclitool.domain.payment

/**
 * 결제(Payment) 애그리게잇의 영속성을 위한 리포지토리 인터페이스(Port).
 * 인프라 계층에서 이 인터페이스를 구현(Adapter)합니다.
 */
interface PaymentRepository {

    /**
     * 결제 정보를 저장합니다.
     *
     * @param payment 저장할 결제 애그리게잇
     * @return 저장된 결제 애그리게잇 (ID 포함)
     */
    fun save(payment: Payment): Payment

    /**
     * 결제 ID로 결제 정보를 조회합니다.
     *
     * @param paymentId 조회할 결제 ID
     * @return 조회된 결제 애그리게잇, 없으면 null
     */
    fun findById(paymentId: PaymentId): Payment?
}
