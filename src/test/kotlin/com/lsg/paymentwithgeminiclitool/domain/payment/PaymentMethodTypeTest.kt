package com.lsg.paymentwithgeminiclitool.domain.payment

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PaymentMethodTypeTest {

    @Test
    fun `유효한_문자열로_PaymentMethodType_찾기_성공`() {
        // given
        val validType = "MyMoney"

        // when
        val paymentMethodType = PaymentMethodType.from(validType)

        // then
        assertEquals(PaymentMethodType.MY_MONEY, paymentMethodType)
    }

    @Test
    fun `유효하지_않은_문자열로_찾을_경우_NoSuchElementException_발생`() {
        // given
        val invalidType = "UnsupportedType"

        // when & then
        assertThrows<NoSuchElementException> {
            PaymentMethodType.from(invalidType)
        }
    }
}
