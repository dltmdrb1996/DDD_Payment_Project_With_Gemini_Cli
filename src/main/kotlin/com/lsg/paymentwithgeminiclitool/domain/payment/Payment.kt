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
    val refundedAmount: Amount = Amount(0),
    val status: PaymentStatus = PaymentStatus.COMPLETED,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val version: Long = 0L
) {
    init {
        require(totalAmount.value >= 0) { "결제 총액은 0 이상이어야 합니다." }
        require(refundedAmount.value >= 0) { "환불된 금액은 0 이상이어야 합니다." }
        require(totalAmount.value >= refundedAmount.value) { "환불된 금액은 총 결제액을 초과할 수 없습니다." }
    }

    /**
     * 결제를 취소합니다.
     *
     * @return 상태가 CANCELED로 변경된 새���운 Payment 객체
     * @throws IllegalStateException 이미 취소되었거나 환불된 결제일 경우
     */
    fun cancel(): Payment {
        if (status == PaymentStatus.CANCELED || status == PaymentStatus.REFUNDED || status == PaymentStatus.PARTIALLY_REFUNDED) {
            throw IllegalStateException("이미 취소되었거나 환불 처리된 결제는 취소할 수 없습니다.")
        }
        return this.copy(status = PaymentStatus.CANCELED)
    }

    /**
     * 결제를 환불합니다.
     *
     * @param amountToRefund 환불할 금액
     * @return 환불 처리된 새로운 Payment 객체
     * @throws IllegalStateException 취소된 결제이거나, 환불 가능 금액을 초과했을 경우
     */
    fun refund(amountToRefund: Amount): Payment {
        if (status == PaymentStatus.CANCELED) {
            throw IllegalStateException("취소된 결제는 환불할 수 없습니다.")
        }

        val newRefundedAmount = Amount(this.refundedAmount.value + amountToRefund.value)

        if (newRefundedAmount.value > this.totalAmount.value) {
            throw IllegalStateException("환불 금액이 결제 금액을 초과할 수 없습니다.")
        }

        val newStatus = if (newRefundedAmount.value == this.totalAmount.value) {
            PaymentStatus.REFUNDED
        } else {
            PaymentStatus.PARTIALLY_REFUNDED
        }

        return this.copy(
            refundedAmount = newRefundedAmount,
            status = newStatus
        )
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
