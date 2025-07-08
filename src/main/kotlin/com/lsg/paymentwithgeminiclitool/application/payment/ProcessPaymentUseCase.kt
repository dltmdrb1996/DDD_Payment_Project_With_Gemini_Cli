package com.lsg.paymentwithgeminiclitool.application.payment

import com.lsg.paymentwithgeminiclitool.application.common.UseCase
import com.lsg.paymentwithgeminiclitool.domain.common.Amount
import com.lsg.paymentwithgeminiclitool.domain.payment.*
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProcessPaymentUseCase(
    private val paymentRepository: PaymentRepository,
    private val rewardPointCalculator: RewardPointCalculator,
    private val paymentMethodFactory: PaymentMethodFactory
) : UseCase<ProcessPaymentUseCase.Request, ProcessPaymentUseCase.Response> {

    @Transactional
    override operator fun invoke(request: Request): Response {
        val factoryRequest = request.toFactoryRequest()
        val paymentMethod = paymentMethodFactory.create(factoryRequest)
        val totalAmount = paymentMethod.getTotalAmount()

        request.amount?.let {
            require(it.value == totalAmount.value) {
                "요청된 결제 금액(${it.value})과 실제 결제 금액(${totalAmount.value})이 일치하지 않습니다."
            }
        }

        val payment = Payment(
            orderId = request.orderId,
            paymentMethod = paymentMethod,
            totalAmount = totalAmount
        )

        val savedPayment = paymentRepository.save(payment)
        val rewardPoints = rewardPointCalculator.calculate(savedPayment.paymentMethod)

        return Response(
            paymentId = savedPayment.id,
            status = "SUCCESS",
            rewardPoints = rewardPoints
        )
    }

    data class Request(
        val orderId: OrderId,
        val paymentMethodType: String,
        val amount: Amount? = null,
        val cardInfo: String? = null,
        val bankInfo: String? = null,
        val subPayments: List<SubPayment> = emptyList(),
        val paymentId: PaymentId? = null,
        val version: Long? = null
    ) {
        fun toFactoryRequest(): PaymentMethodFactory.Request {
            return PaymentMethodFactory.Request(
                paymentMethodType = this.paymentMethodType,
                amount = this.amount,
                cardInfo = this.cardInfo,
                bankInfo = this.bankInfo,
                subPayments = this.subPayments.map { it.toFactorySubPayment() }
            )
        }
    }

    data class SubPayment(
        val type: String,
        val amount: Amount? = null,
        val cardInfo: String? = null,
        val bankInfo: String? = null
    ) {
        fun toFactorySubPayment(): PaymentMethodFactory.SubPayment {
            return PaymentMethodFactory.SubPayment(
                type = this.type,
                amount = this.amount,
                cardInfo = this.cardInfo,
                bankInfo = this.bankInfo
            )
        }
    }

    data class Response(
        val paymentId: PaymentId?,
        val status: String,
        val rewardPoints: Long
    )
}
