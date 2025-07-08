package com.lsg.paymentwithgeminiclitool.domain.payment

import com.lsg.paymentwithgeminiclitool.domain.common.Amount
import java.time.LocalDateTime

@JvmInline
value class PaymentId(val value: Long)

@JvmInline
value class OrderId(val value: Long)

/**
 * 결제(Payment) 애그리게잇 루트(Aggregate Root).
 * 결제 한 건에 대한 모든 정보를 포함하며, 데이터 일관성의 단위가 됩니다.
 */
data class Payment(
    val id: PaymentId? = null,
    val orderId: OrderId,
    val paymentMethod: PaymentMethod,
    val totalAmount: Amount,
    val status: PaymentStatus = PaymentStatus.COMPLETED,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val version: Long = 0L
) {
    init {
        // 애그리게잇 내부의 총 금액이 결제수단별 금액의 합과 일치하는지 검증
        require(totalAmount.value == paymentMethod.getTotalAmount().value) {
            "결제 총액이 결제수단별 금액의 합과 일치하지 않습니다."
        }
    }

    /**
     * 결제를 취소합니다.
     *
     * @return 상태가 CANCELED로 변경된 새로운 Payment 객체
     * @throws IllegalStateException 이미 취소된 결제일 경우
     */
    fun cancel(): Payment {
        if (this.status == PaymentStatus.CANCELED) {
            throw IllegalStateException("이미 취소된 결제입니다.")
        }
        return this.copy(status = PaymentStatus.CANCELED)
    }
}

/**
 * PaymentMethod 인터페이스에 총액을 계산하는 기능을 추가합니다.
 */
fun PaymentMethod.getTotalAmount(): Amount {
    return when (this) {
        is SinglePayment -> this.amount
        is MyPointCompositePayment -> {
            val otherAmount = (this.otherPayment as? SinglePayment)?.amount ?: Amount(0)
            Amount(this.myPointPayment.amount.value + otherAmount.value)
        }
        is MyMoneyCompositePayment -> {
            val otherAmount = (this.otherPayment as? SinglePayment)?.amount ?: Amount(0)
            Amount(this.myMoneyPayment.amount.value + otherAmount.value)
        }
        is MyPointAndMoneyCompositePayment -> {
            val moneyAmount = this.myMoneyPayment.amount
            val otherAmount = (this.otherPayment as? SinglePayment)?.amount ?: Amount(0)
            Amount(this.myPointPayment.amount.value + moneyAmount.value + otherAmount.value)
        }
    }
}

/**
 * SinglePayment 인터페이스에 amount 속성을 추가합니다.
 */
val SinglePayment.amount: Amount
    get() = when (this) {
        is MyPointPayment -> this.amount
        is MyMoneyPayment -> this.amount
        is CardEasyPayment -> this.amount
        is BankEasyPayment -> this.amount
    }
