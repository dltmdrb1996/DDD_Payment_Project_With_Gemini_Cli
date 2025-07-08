package com.lsg.paymentwithgeminiclitool.application.payment

import com.lsg.paymentwithgeminiclitool.application.common.UseCase
import com.lsg.paymentwithgeminiclitool.domain.common.Amount
import com.lsg.paymentwithgeminiclitool.domain.payment.*
import java.time.LocalDateTime

class GetPaymentDetailsUseCase(
    private val paymentRepository: PaymentRepository
) : UseCase<GetPaymentDetailsUseCase.Request, GetPaymentDetailsUseCase.Response> {

    override fun invoke(request: Request): Response {
        val payment = paymentRepository.findById(request.paymentId)
            ?: throw NoSuchElementException("결제 정보를 찾을 수 없습니다. ID: ${request.paymentId.value}")

        return Response.from(payment)
    }

    data class Request(
        val paymentId: PaymentId
    )

    data class Response(
        val paymentId: PaymentId,
        val orderId: OrderId,
        val paymentMethod: PaymentMethod,
        val totalAmount: Amount,
        val status: PaymentStatus,
        val createdAt: LocalDateTime
    ) {
        companion object {
            fun from(payment: Payment): Response {
                return Response(
                    paymentId = payment.id!!,
                    orderId = payment.orderId,
                    paymentMethod = payment.paymentMethod,
                    totalAmount = payment.totalAmount,
                    status = payment.status,
                    createdAt = payment.createdAt
                )
            }
        }
    }
}
