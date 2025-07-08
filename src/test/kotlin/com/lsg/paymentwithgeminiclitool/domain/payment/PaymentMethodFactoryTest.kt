package com.lsg.paymentwithgeminiclitool.domain.payment

import com.lsg.paymentwithgeminiclitool.domain.common.Amount
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PaymentMethodFactoryTest {

    private val factory: PaymentMethodFactory = PaymentMethodFactoryImpl()

    @Test
    fun `MyMoney 타입으로 단일 결제 객체 생성 성공`() {
        // given
        val request = PaymentMethodFactory.Request(
            paymentMethodType = PaymentMethodType.MY_MONEY,
            amount = Amount(10000)
        )

        // when
        val paymentMethod = factory.create(request)

        // then
        assertInstanceOf(MyMoneyPayment::class.java, paymentMethod)
    }

    @Test
    fun `CardEasy 타입으로 단일 결제 객체 생성 성공`() {
        // given
        val request = PaymentMethodFactory.Request(
            paymentMethodType = PaymentMethodType.CARD_EASY,
            amount = Amount(10000),
            cardInfo = CardInfo("1234-5678-1234-5678")
        )

        // when
        val paymentMethod = factory.create(request)

        // then
        assertInstanceOf(CardEasyPayment::class.java, paymentMethod)
    }

    @Test
    fun `BankEasy 타입으로 단일 결제 객체 생성 성공`() {
        // given
        val request = PaymentMethodFactory.Request(
            paymentMethodType = PaymentMethodType.BANK_EASY,
            amount = Amount(10000),
            bankInfo = BankInfo("신한은행 110-123-456789")
        )

        // when
        val paymentMethod = factory.create(request)

        // then
        assertInstanceOf(BankEasyPayment::class.java, paymentMethod)
    }

    @Test
    fun `MyPointComposite 타입으로 복합 결제 객체 생성 성공`() {
        // given
        val request = PaymentMethodFactory.Request(
            paymentMethodType = PaymentMethodType.MY_POINT_COMPOSITE,
            subPayments = listOf(
                PaymentMethodFactory.SubPayment(type = "MyPoint", amount = Amount(1000)),
                PaymentMethodFactory.SubPayment(type = "CardEasy", amount = Amount(9000), cardInfo = CardInfo("1234-5678-1234-5678"))
            )
        )

        // when
        val paymentMethod = factory.create(request)

        // then
        assertInstanceOf(MyPointCompositePayment::class.java, paymentMethod)
    }

    
}