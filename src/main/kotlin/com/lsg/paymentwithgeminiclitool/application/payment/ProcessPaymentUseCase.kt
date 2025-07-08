package com.lsg.paymentwithgeminiclitool.application.payment

import com.lsg.paymentwithgeminiclitool.application.common.UseCase
import com.lsg.paymentwithgeminiclitool.domain.common.Amount
import com.lsg.paymentwithgeminiclitool.domain.payment.*
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

        if (request is Single) {
            require(request.amount.value == totalAmount.value) {
                "요청된 결제 금액(${request.amount.value})과 실제 결제 금액(${totalAmount.value})이 일치하지 않습니다."
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
            status = PaymentStatus.COMPLETED, // String 대신 Enum 사용
            rewardPoints = rewardPoints
        )
    }

    sealed interface Request {
        val orderId: OrderId
        val paymentMethodType: PaymentMethodType
        fun toFactoryRequest(): PaymentMethodFactory.Request
    }

    data class Single(
        override val orderId: OrderId,
        override val paymentMethodType: PaymentMethodType,
        val amount: Amount,
        val cardInfo: String? = null,
        val bankInfo: String? = null,
    ) : Request {
        override fun toFactoryRequest(): PaymentMethodFactory.Request {
            return PaymentMethodFactory.Request(
                paymentMethodType = this.paymentMethodType,
                amount = this.amount,
                cardInfo = this.cardInfo?.let { CardInfo(it) },
                bankInfo = this.bankInfo?.let { BankInfo(it) }
            )
        }
    }

    data class Composite(
        override val orderId: OrderId,
        override val paymentMethodType: PaymentMethodType,
        val subPayments: List<PaymentMethodFactory.SubPayment>
    ) : Request {
        override fun toFactoryRequest(): PaymentMethodFactory.Request {
            return PaymentMethodFactory.Request(
                paymentMethodType = this.paymentMethodType,
                subPayments = subPayments
            )
        }
    }

    data class Response(
        val paymentId: PaymentId?,
        val status: PaymentStatus,
        val rewardPoints: Long
    )
}