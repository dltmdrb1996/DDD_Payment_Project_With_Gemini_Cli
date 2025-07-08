package com.lsg.paymentwithgeminiclitool.domain.payment

import com.lsg.paymentwithgeminiclitool.domain.common.Amount
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RewardPointCalculatorTest {

    private val calculator: RewardPointCalculator = RewardPointCalculatorImpl()

    @Test
    fun `My머니_단일결제시_결제금액의_1퍼센트_적립`() {
        val paymentMethod = MyMoneyPayment(amount = Amount(10000))
        val rewardPoints = calculator.calculate(paymentMethod)
        assertEquals(100L, rewardPoints)
    }

    @Test
    fun `카드_단일결제시_결제금액의_0_5퍼센트_적립`() {
        val paymentMethod = CardEasyPayment(amount = Amount(10000), cardInfo = CardInfo("1234-1234-1234-1234"))
        val rewardPoints = calculator.calculate(paymentMethod)
        assertEquals(50L, rewardPoints)
    }
    
    @Test
    fun `계좌_단일결제시_결제금액의_0_5퍼센트_적립`() {
        val paymentMethod = BankEasyPayment(amount = Amount(10000), bankInfo = BankInfo("신한은행 110-123-456789"))
        val rewardPoints = calculator.calculate(paymentMethod)
        assertEquals(50L, rewardPoints)
    }

    @Test
    fun `포인���만으로_결제시_적립금은_0원`() {
        val paymentMethod = MyPointPayment(amount = Amount(10000))
        val rewardPoints = calculator.calculate(paymentMethod)
        assertEquals(0L, rewardPoints)
    }

    @Test
    fun `포인트와_머니_복합결제시_머니결제금액만_적립`() {
        val paymentMethod = MyPointCompositePayment(
            myPointPayment = MyPointPayment(amount = Amount(1000)),
            otherPayment = MyMoneyPayment(amount = Amount(9000))
        )
        val rewardPoints = calculator.calculate(paymentMethod)
        assertEquals(90L, rewardPoints) // 9000 * 0.01
    }

    @Test
    fun `포인트와_카드_복합결제시_카드결제금액만_적립`() {
        val paymentMethod = MyPointCompositePayment(
            myPointPayment = MyPointPayment(amount = Amount(1000)),
            otherPayment = CardEasyPayment(amount = Amount(9000), cardInfo = CardInfo("1234-1234-1234-1234"))
        )
        val rewardPoints = calculator.calculate(paymentMethod)
        assertEquals(45L, rewardPoints) // 9000 * 0.005
    }
    
    @Test
    fun `포인트와_계좌_복합결제시_계좌결제금액만_적립`() {
        val paymentMethod = MyPointCompositePayment(
            myPointPayment = MyPointPayment(amount = Amount(1000)),
            otherPayment = BankEasyPayment(amount = Amount(8000), bankInfo = BankInfo("우리은행 1002-123-456789"))
        )
        val rewardPoints = calculator.calculate(paymentMethod)
        assertEquals(40L, rewardPoints) // 8000 * 0.005
    }

    @Test
    fun `머니와_카드_복합결제시_각각_적립률로_계산`() {
        val paymentMethod = MyMoneyCompositePayment(
            myMoneyPayment = MyMoneyPayment(amount = Amount(3000)),
            otherPayment = CardEasyPayment(amount = Amount(7000), cardInfo = CardInfo("1234-5678-1234-5678"))
        )
        val rewardPoints = calculator.calculate(paymentMethod)
        assertEquals((3000 * 0.01 + 7000 * 0.005).toLong(), rewardPoints) // 30 + 35 = 65
    }

    @Test
    fun `포인트와_머니와_카드_3중복합결제시_포인트제외하고_각각_적립률로_계산`() {
        val paymentMethod = MyPointAndMoneyCompositePayment(
            myPointPayment = MyPointPayment(amount = Amount(1000)),
            myMoneyPayment = MyMoneyPayment(amount = Amount(2000)),
            otherPayment = CardEasyPayment(amount = Amount(7000), cardInfo = CardInfo("1234-1234-1234-1234"))
        )
        val rewardPoints = calculator.calculate(paymentMethod)
        assertEquals((2000 * 0.01 + 7000 * 0.005).toLong(), rewardPoints) // 20 + 35 = 55
    }
    
    @Test
    fun `포인트와_머니와_계좌_3중복합결제시_포인트제외하고_각각_적립률로_계산`() {
        val paymentMethod = MyPointAndMoneyCompositePayment(
            myPointPayment = MyPointPayment(amount = Amount(1000)),
            myMoneyPayment = MyMoneyPayment(amount = Amount(4000)),
            otherPayment = BankEasyPayment(amount = Amount(5000), bankInfo = BankInfo("국민은행 987-654-3210"))
        )
        val rewardPoints = calculator.calculate(paymentMethod)
        assertEquals((4000 * 0.01 + 5000 * 0.005).toLong(), rewardPoints) // 40 + 25 = 65
    }
}