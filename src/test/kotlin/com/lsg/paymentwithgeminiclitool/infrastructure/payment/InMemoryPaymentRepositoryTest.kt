package com.lsg.paymentwithgeminiclitool.infrastructure.payment

import com.lsg.paymentwithgeminiclitool.domain.common.Amount
import com.lsg.paymentwithgeminiclitool.domain.payment.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class InMemoryPaymentRepositoryTest {

    private lateinit var paymentRepository: PaymentRepository

    @BeforeEach
    fun setUp() {
        paymentRepository = InMemoryPaymentRepository()
    }

    @Test
    fun `결제_정보_저장_및_조회_성공`() {
        // given
        val moneyPayment = MyMoneyPayment(amount = Amount(10000))
        val payment = Payment(
            orderId = OrderId(1L),
            paymentMethod = moneyPayment,
            totalAmount = Amount(10000)
        )

        // when
        val savedPayment = paymentRepository.save(payment)

        // then
        assertNotNull(savedPayment.id)
        assertEquals(1L, savedPayment.id?.value)

        // when
        val foundPayment = paymentRepository.findById(savedPayment.id!!)

        // then
        assertNotNull(foundPayment)
        assertEquals(savedPayment, foundPayment)
    }

    @Test
    fun `존재하지_않는_ID로_조회시_null_반환`() {
        // when
        val foundPayment = paymentRepository.findById(PaymentId(999L))

        // then
        assertEquals(null, foundPayment)
    }
}