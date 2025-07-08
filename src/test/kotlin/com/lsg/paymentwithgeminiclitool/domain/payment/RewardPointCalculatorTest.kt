package com.lsg.paymentwithgeminiclitool.domain.payment

import com.lsg.paymentwithgeminiclitool.domain.common.Amount
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RewardPointCalculatorTest {

    private val calculator = RewardPointCalculator()

    @Test
    fun `My머니_단일결제시_결제금액의_1퍼센트_적립`() {
        // given
        val paymentMethod = MyMoneyPayment(amount = Amount(10000))

        // when
        val rewardPoints = calculator.calculate(paymentMethod)

        // then
        assertEquals(100L, rewardPoints)
    }

    @Test
    fun `카드_단일결제시_결제금액의_0_5퍼센트_적립`() {
        // given
        val paymentMethod = CardEasyPayment(amount = Amount(10000), cardInfo = "1234-5678")

        // when
        val rewardPoints = calculator.calculate(paymentMethod)

        // then
        assertEquals(50L, rewardPoints)
    }

    @Test
    fun `포인트_머니_복합결제시_포인트를_제외한_머니결제금액의_1퍼센트_적립`() {
        // given
        val paymentMethod = MyPointCompositePayment(
            myPointPayment = MyPointPayment(amount = Amount(1000)),
            otherPayment = MyMoneyPayment(amount = Amount(9000))
        )

        // when
        val rewardPoints = calculator.calculate(paymentMethod)

        // then
        assertEquals(90L, rewardPoints) // 9000 * 0.01
    }

    @Test
    fun `포인트_카드_복합결제시_포인트를_제외한_카드결제금액의_0_5퍼센트_적립`() {
        // given
        val paymentMethod = MyPointCompositePayment(
            myPointPayment = MyPointPayment(amount = Amount(1000)),
            otherPayment = CardEasyPayment(amount = Amount(9000), cardInfo = "1234-5678")
        )

        // when
        val rewardPoints = calculator.calculate(paymentMethod)

        // then
        assertEquals(45L, rewardPoints) // 9000 * 0.005
    }

    @Test
    fun `포인트_머니_카드_3중복합결제시_포인트를_제외한_머니와_카드결제금액의_각각_적립률로_계산`() {
        // given
        val paymentMethod = MyPointAndMoneyCompositePayment(
            myPointPayment = MyPointPayment(amount = Amount(1000)),
            myMoneyPayment = MyMoneyPayment(amount = Amount(2000)),
            otherPayment = CardEasyPayment(amount = Amount(7000), cardInfo = "1234-5678")
        )

        // when
        val rewardPoints = calculator.calculate(paymentMethod)

        // then
        val expectedPoints = (2000 * 0.01) + (7000 * 0.005) // 20 + 35 = 55
        assertEquals(expectedPoints.toLong(), rewardPoints)
    }

    @Test
    fun `포인트만으로_결제시_적립금은_0원`() {
        // given
        val paymentMethod = MyPointPayment(amount = Amount(10000))

        // when
        val rewardPoints = calculator.calculate(paymentMethod)

        // then
        assertEquals(0L, rewardPoints)
    }
}