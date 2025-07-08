package com.lsg.paymentwithgeminiclitool.interfaces.payment

import com.lsg.paymentwithgeminiclitool.application.payment.ProcessPaymentUseCase
import com.lsg.paymentwithgeminiclitool.domain.common.Amount
import com.lsg.paymentwithgeminiclitool.domain.payment.OrderId
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
        return ProcessPaymentUseCase.Request(
            orderId = OrderId(this.orderId),
            paymentMethodType = this.paymentMethodType,
            amount = this.amount?.let { Amount(it) },
            cardInfo = this.cardInfo,
            bankInfo = this.bankInfo,
            subPayments = this.subPayments.map { it.toUseCaseSubPayment() }
        )
    }
}

data class SubPaymentDto(
    val type: String,
    val amount: Long? = null,
    val cardInfo: String? = null,
    val bankInfo: String? = null
) {
    fun toUseCaseSubPayment(): ProcessPaymentUseCase.SubPayment {
        return ProcessPaymentUseCase.SubPayment(
            type = this.type,
            amount = this.amount?.let { Amount(it) },
            cardInfo = this.cardInfo,
            bankInfo = this.bankInfo
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
                status = response.status,
                rewardPoints = response.rewardPoints
            )
        }
    }
}
