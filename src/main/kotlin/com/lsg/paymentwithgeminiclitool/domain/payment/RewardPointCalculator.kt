package com.lsg.paymentwithgeminiclitool.domain.payment

interface RewardPointCalculator {
    fun calculate(paymentMethod: PaymentMethod): Long
}