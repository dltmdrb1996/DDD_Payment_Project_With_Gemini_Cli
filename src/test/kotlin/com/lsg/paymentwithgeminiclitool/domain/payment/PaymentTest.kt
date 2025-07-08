package com.lsg.paymentwithgeminiclitool.domain.payment

import com.lsg.paymentwithgeminiclitool.domain.common.Amount
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PaymentTest {

    @Test
    fun `결제_취소_성공`() {
        // given
        val payment = Payment(
            orderId = OrderId(1L),
            paymentMethod = CardEasyPayment(Amount(10000), CardInfo("1234-1234-1234-1234")),
            totalAmount = Amount(10000),
            status = PaymentStatus.COMPLETED
        )

        // when
        val canceledPayment = payment.cancel()

        // then
        assertEquals(PaymentStatus.CANCELED, canceledPayment.status)
    }

    @Test
    fun `이미_취소된_결제를_다시_취소하면_예외_발생`() {
        // given
        val payment = Payment(
            orderId = OrderId(1L),
            paymentMethod = CardEasyPayment(Amount(10000), CardInfo("1234-1234-1234-1234")),
            totalAmount = Amount(10000),
            status = PaymentStatus.CANCELED
        )

        // when & then
        assertThrows<IllegalStateException> {
            payment.cancel()
        }
    }

    @Test
    fun `전액_환불_성공`() {
        // given
        val payment = Payment(
            orderId = OrderId(1L),
            paymentMethod = CardEasyPayment(Amount(10000), CardInfo("1234-1234-1234-1234")),
            totalAmount = Amount(10000)
        )

        // when
        val refundedPayment = payment.refund(Amount(10000))

        // then
        assertEquals(PaymentStatus.REFUNDED, refundedPayment.status)
        assertEquals(Amount(10000), refundedPayment.refundedAmount)
    }

    @Test
    fun `부분_환불_성공`() {
        // given
        val payment = Payment(
            orderId = OrderId(1L),
            paymentMethod = CardEasyPayment(Amount(10000), CardInfo("1234-1234-1234-1234")),
            totalAmount = Amount(10000)
        )

        // when
        val refundedPayment = payment.refund(Amount(5000))

        // then
        assertEquals(PaymentStatus.PARTIALLY_REFUNDED, refundedPayment.status)
        assertEquals(Amount(5000), refundedPayment.refundedAmount)
    }

    @Test
    fun `부분_환불을_여러번_하여_전액_환불되는_경우`() {
        // given
        val payment = Payment(
            orderId = OrderId(1L),
            paymentMethod = CardEasyPayment(Amount(10000), CardInfo("1234-1234-1234-1234")),
            totalAmount = Amount(10000)
        )

        // when
        val firstRefund = payment.refund(Amount(5000))
        val secondRefund = firstRefund.refund(Amount(5000))

        // then
        assertEquals(PaymentStatus.REFUNDED, secondRefund.status)
        assertEquals(Amount(10000), secondRefund.refundedAmount)
    }

    @Test
    fun `환불_가능_금액을_초과하여_환불하면_예외_발생`() {
        // given
        val payment = Payment(
            orderId = OrderId(1L),
            paymentMethod = CardEasyPayment(Amount(10000), CardInfo("1234-1234-1234-1234")),
            totalAmount = Amount(10000)
        )

        // when & then
        assertThrows<IllegalStateException> {
            payment.refund(Amount(15000))
        }
    }

    @Test
    fun `취소된_결제를_환불하면_예외_발생`() {
        // given
        val payment = Payment(
            orderId = OrderId(1L),
            paymentMethod = CardEasyPayment(Amount(10000), CardInfo("1234-1234-1234-1234")),
            totalAmount = Amount(10000),
            status = PaymentStatus.CANCELED
        )

        // when & then
        assertThrows<IllegalStateException> {
            payment.refund(Amount(5000))
        }
    }
}
