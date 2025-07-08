package com.lsg.paymentwithgeminiclitool.config

import com.lsg.paymentwithgeminiclitool.domain.payment.PaymentMethodFactory
import com.lsg.paymentwithgeminiclitool.domain.payment.PaymentMethodFactoryImpl
import com.lsg.paymentwithgeminiclitool.domain.payment.RewardPointCalculator
import com.lsg.paymentwithgeminiclitool.domain.payment.RewardPointCalculatorImpl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BeanConfig {

    @Bean
    fun rewardPointCalculator(): RewardPointCalculator {
        return RewardPointCalculatorImpl()
    }

    @Bean
    fun paymentMethodFactory(): PaymentMethodFactory {
        return PaymentMethodFactoryImpl()
    }
}
