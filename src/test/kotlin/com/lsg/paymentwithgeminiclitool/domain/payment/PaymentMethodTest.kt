package com.lsg.paymentwithgeminiclitool.domain.payment

import com.lsg.paymentwithgeminiclitool.domain.common.Amount
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

class PaymentMethodTest {

    @Test
    fun `단일결제_My포인트_생성_성공`() {
        // when & then
        assertDoesNotThrow {
            MyPointPayment(amount = Amount(10000))
        }
    }

    @Test
    fun `단일결제_My머니_생성_성공`() {
        // when & then
        assertDoesNotThrow {
            MyMoneyPayment(amount = Amount(10000))
        }
    }

    @Test
    fun `단일결제_카드간편결제_생성_성공`() {
        // when & then
        assertDoesNotThrow {
            CardEasyPayment(amount = Amount(10000), cardInfo = CardInfo("1234-5678-1234-5678"))
        }
    }

    @Test
    fun `단일결제_계좌간편결제_생성_성공`() {
        // when & then
        assertDoesNotThrow {
            BankEasyPayment(amount = Amount(10000), bankInfo = BankInfo("신한은행 110-123-456789"))
        }
    }

    @Test
    fun `복합결제_My포인트와_머니_생성_성공`() {
        // given
        val pointPayment = MyPointPayment(amount = Amount(1000))
        val moneyPayment = MyMoneyPayment(amount = Amount(9000))

        // when
        val compositePayment = MyPointCompositePayment(
            myPointPayment = pointPayment,
            otherPayment = moneyPayment
        )

        // then
        assertInstanceOf(MyPointPayment::class.java, compositePayment.myPointPayment)
        assertInstanceOf(MyMoneyPayment::class.java, compositePayment.otherPayment)
    }

    @Test
    fun `복합결제_My포인트와_카드_생성_성공`() {
        // given
        val pointPayment = MyPointPayment(amount = Amount(1000))
        val cardPayment = CardEasyPayment(amount = Amount(9000), cardInfo = CardInfo("1234-5678-1234-5678"))

        // when
        val compositePayment = MyPointCompositePayment(
            myPointPayment = pointPayment,
            otherPayment = cardPayment
        )

        // then
        assertInstanceOf(MyPointPayment::class.java, compositePayment.myPointPayment)
        assertInstanceOf(CardEasyPayment::class.java, compositePayment.otherPayment)
    }

    @Test
    fun `복합결제_My머니와_계좌_생성_성공`() {
        // given
        val moneyPayment = MyMoneyPayment(amount = Amount(1000))
        val bankPayment = BankEasyPayment(amount = Amount(9000), bankInfo = BankInfo("신한은행 110-123-456789"))

        // when
        val compositePayment = MyMoneyCompositePayment(
            myMoneyPayment = moneyPayment,
            otherPayment = bankPayment
        )

        // then
        assertInstanceOf(MyMoneyPayment::class.java, compositePayment.myMoneyPayment)
        assertInstanceOf(BankEasyPayment::class.java, compositePayment.otherPayment)
    }

    @Test
    fun `3중복합결제_포인트_머니_카드_생성_성공`() {
        // given
        val pointPayment = MyPointPayment(amount = Amount(1000))
        val moneyPayment = MyMoneyPayment(amount = Amount(2000))
        val cardPayment = CardEasyPayment(amount = Amount(7000), cardInfo = CardInfo("1234-5678-1234-5678"))

        // when
        val compositePayment = MyPointAndMoneyCompositePayment(
            myPointPayment = pointPayment,
            myMoneyPayment = moneyPayment,
            otherPayment = cardPayment
        )

        // then
        assertInstanceOf(MyPointPayment::class.java, compositePayment.myPointPayment)
        assertInstanceOf(MyMoneyPayment::class.java, compositePayment.myMoneyPayment)
        assertInstanceOf(CardEasyPayment::class.java, compositePayment.otherPayment)
    }
}
