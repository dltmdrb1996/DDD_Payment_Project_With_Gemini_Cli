package com.lsg.paymentwithgeminiclitool.application.payment

import com.lsg.paymentwithgeminiclitool.application.common.UseCase
import com.lsg.paymentwithgeminiclitool.domain.common.Amount
import com.lsg.paymentwithgeminiclitool.domain.payment.PaymentId
import com.lsg.paymentwithgeminiclitool.domain.payment.PaymentRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class RefundPaymentUseCase(
    private val paymentRepository: PaymentRepository
) : UseCase<RefundPaymentUseCase.Request, Unit> {

    @Transactional
    override fun invoke(request: Request) {
        val payment = paymentRepository.findById(request.paymentId)
            ?: throw NoSuchElementException("결제 정보를 찾을 수 없습니다. ID: ${request.paymentId.value}")

        val refundedPayment = payment.refund(request.refundAmount)

        paymentRepository.save(refundedPayment)
    }

    data class Request(
        val paymentId: PaymentId,
        val refundAmount: Amount
    )
}
