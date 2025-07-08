package com.lsg.paymentwithgeminiclitool.domain.payment

import org.springframework.stereotype.Component

@Component
class RewardPointCalculatorImpl : RewardPointCalculator {

    override fun calculate(paymentMethod: PaymentMethod): Long {
        return when (paymentMethod) {
            is MyMoneyPayment -> (paymentMethod.amount.value * 0.01).toLong()
            is CardEasyPayment -> (paymentMethod.amount.value * 0.005).toLong()
            is BankEasyPayment -> (paymentMethod.amount.value * 0.005).toLong() // 카드와 동일하다고 가정
            is MyPointPayment -> 0L // 포인트 사용분은 적립 제외

            is MyPointCompositePayment -> {
                calculate(paymentMethod.otherPayment as PaymentMethod)
            }

            is MyMoneyCompositePayment -> {
                val moneyPoints = calculate(paymentMethod.myMoneyPayment)
                val otherPoints = calculate(paymentMethod.otherPayment as PaymentMethod)
                moneyPoints + otherPoints
            }

            is MyPointAndMoneyCompositePayment -> {
                val moneyPoints = calculate(paymentMethod.myMoneyPayment)
                val otherPoints = calculate(paymentMethod.otherPayment as PaymentMethod)
                moneyPoints + otherPoints
            }

            is SinglePayment -> 0L // SinglePayment의 다른 구현체는 0으로 처리 (예시)
        }
    }
}
