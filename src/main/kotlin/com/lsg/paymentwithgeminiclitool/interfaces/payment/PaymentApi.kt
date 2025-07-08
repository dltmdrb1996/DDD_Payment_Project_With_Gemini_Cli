package com.lsg.paymentwithgeminiclitool.interfaces.payment

import com.lsg.paymentwithgeminiclitool.application.payment.*
import com.lsg.paymentwithgeminiclitool.domain.common.Amount
import com.lsg.paymentwithgeminiclitool.domain.payment.*
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/payments")
class PaymentController(
    private val processPaymentUseCase: ProcessPaymentUseCase
) {

    @PostMapping
    fun processPayment(@RequestBody requestDto: PaymentRequestDto): PaymentResponseDto {
        val useCaseRequest = requestDto.toUseCaseRequest()
        val useCaseResponse = processPaymentUseCase.invoke(useCaseRequest)
        return PaymentResponseDto.from(useCaseResponse)
    }
}

data class PaymentRequestDto(
    val orderId: Long,
    val paymentMethodType: String,
    val amount: Long? = null,
    val cardInfo: String? = null,
    val bankInfo: String? = null,
    val subPayments: List<SubPaymentDto> = emptyList()
) {
    fun toUseCaseRequest(): ProcessPaymentUseCase.Request {
        val paymentType = PaymentMethodType.from(this.paymentMethodType)
        return if (subPayments.isNotEmpty()) {
            ProcessPaymentUseCase.Composite(
                orderId = OrderId(this.orderId),
                paymentMethodType = paymentType,
                subPayments = this.subPayments.map { it.toUseCaseSubPayment() }
            )
        } else {
            ProcessPaymentUseCase.Single(
                orderId = OrderId(this.orderId),
                paymentMethodType = paymentType,
                amount = this.amount?.let { Amount(it) } ?: throw IllegalArgumentException("Amount is required for single payment"),
                cardInfo = this.cardInfo,
                bankInfo = this.bankInfo
            )
        }
    }
}

data class SubPaymentDto(
    val type: String,
    val amount: Long,
    val cardInfo: String? = null,
    val bankInfo: String? = null
) {
    fun toUseCaseSubPayment(): PaymentMethodFactory.SubPayment {
        return PaymentMethodFactory.SubPayment(
            type = SubPaymentType.from(this.type),
            amount = Amount(this.amount),
            cardInfo = this.cardInfo?.let { CardInfo(it) },
            bankInfo = this.bankInfo?.let { BankInfo(it) }
        )
    }
}

data class PaymentResponseDto(
    val paymentId: Long?,
    val status: String,
    val rewardPoints: Long
) {
    companion object {
        fun from(response: ProcessPaymentUseCase.Response): PaymentResponseDto {
            return PaymentResponseDto(
                paymentId = response.paymentId?.value,
                status = response.status.toString(),
                rewardPoints = response.rewardPoints
            )
        }
    }
}
