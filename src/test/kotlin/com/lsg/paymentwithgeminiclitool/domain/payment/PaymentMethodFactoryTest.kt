package com.lsg.paymentwithgeminiclitool.domain.payment

import com.lsg.paymentwithgeminiclitool.domain.common.Amount
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PaymentMethodFactoryTest {

    private val factory = PaymentMethodFactoryImpl()

    @Test
    fun `MyMoney 타입으로 단일 결제 객체 생성 성공`() {
        // given
        val request = PaymentMethodFactory.Request(
            paymentMethodType = "MyMoney",
            amount = Amount(10000)
        )

        // when
        val paymentMethod = factory.create(request)

        // then
        assertInstanceOf(MyMoneyPayment::class.java, paymentMethod)
    }

    @Test
    fun `MyPointComposite 타입으로 복합 결제 객체 생성 성공`() {
        // given
        val request = PaymentMethodFactory.Request(
            paymentMethodType = "MyPointComposite",
            subPayments = listOf(
                PaymentMethodFactory.SubPayment("MyPoint", Amount(1000)),
                PaymentMethodFactory.SubPayment("CardEasy", Amount(9000), "1234-5678")
            )
        )

        // when
        val paymentMethod = factory.create(request)

        // then
        assertInstanceOf(MyPointCompositePayment::class.java, paymentMethod)
    }

    @Test
    fun `지원하지 않는 결제 타입으로 생성시 예외 발생`() {
        // given
        val request = PaymentMethodFactory.Request(
            paymentMethodType = "UnsupportedType",
            amount = Amount(10000)
        )

        // when & then
        assertThrows<IllegalArgumentException> {
            factory.create(request)
        }
    }
}
