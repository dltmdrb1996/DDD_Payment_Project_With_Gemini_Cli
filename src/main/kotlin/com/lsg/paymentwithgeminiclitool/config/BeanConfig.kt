package com.lsg.paymentwithgeminiclitool.config

import com.lsg.paymentwithgeminiclitool.domain.payment.RewardPointCalculator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BeanConfig {

    @Bean
    fun rewardPointCalculator(): RewardPointCalculator {
        return RewardPointCalculator()
    }
}
