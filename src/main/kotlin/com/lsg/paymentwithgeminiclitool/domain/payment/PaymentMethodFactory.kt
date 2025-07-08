package com.lsg.paymentwithgeminiclitool.domain.payment

import com.lsg.paymentwithgeminiclitool.domain.common.Amount

interface PaymentMethodFactory {
    fun create(request: Request): PaymentMethod

    data class Request(
        val paymentMethodType: String,
        val amount: Amount? = null,
        val cardInfo: String? = null,
        val bankInfo: String? = null,
        val subPayments: List<SubPayment> = emptyList()
    )

    data class SubPayment(
        val type: String,
        val amount: Amount? = null,
        val cardInfo: String? = null,
        val bankInfo: String? = null
    )
}