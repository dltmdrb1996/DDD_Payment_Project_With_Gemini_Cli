package com.lsg.paymentwithgeminiclitool.application.payment

import com.lsg.paymentwithgeminiclitool.application.common.UseCase
import com.lsg.paymentwithgeminiclitool.domain.common.Amount
import com.lsg.paymentwithgeminiclitool.domain.payment.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.dao.OptimisticLockingFailureException

@Service
class ProcessPaymentUseCase(
    private val paymentRepository: PaymentRepository,
    private val rewardPointCalculator: RewardPointCalculator
) : UseCase<ProcessPaymentUseCase.Request, ProcessPaymentUseCase.Response> {

    @Transactional
    override operator fun invoke(request: Request): Response {
        val paymentMethod = mapToPaymentMethod(request)
        val totalAmount = paymentMethod.getTotalAmount()

        val payment = if (request.paymentId != null) {
            val existingPayment = paymentRepository.findById(request.paymentId)
                ?: throw IllegalArgumentException("Payment not found with ID: ${request.paymentId.value}")
            
            if (existingPayment.version != request.version) {
                throw OptimisticLockingFailureException("Optimistic locking failed for Payment with ID: ${request.paymentId.value}")
            }
            existingPayment.copy(
                paymentMethod = paymentMethod,
                totalAmount = totalAmount,
                version = existingPayment.version + 1
            )
        } else {
            Payment(
                orderId = request.orderId,
                paymentMethod = paymentMethod,
                totalAmount = totalAmount
            )
        }

        val savedPayment = paymentRepository.save(payment)
        val rewardPoints = rewardPointCalculator.calculate(savedPayment.paymentMethod)

        return Response(
            paymentId = savedPayment.id,
            status = "SUCCESS",
            rewardPoints = rewardPoints
        )
    }

    private fun mapToPaymentMethod(request: Request): PaymentMethod {
        return when (request.paymentMethodType) {
            "MyMoney" -> MyMoneyPayment(request.amount ?: throw IllegalArgumentException("Amount is required for MyMoney payment"))
            "CardEasy" -> CardEasyPayment(request.amount ?: throw IllegalArgumentException("Amount is required for CardEasy payment"), request.cardInfo ?: throw IllegalArgumentException("Card info is required for CardEasy payment"))
            "BankEasy" -> BankEasyPayment(request.amount ?: throw IllegalArgumentException("Amount is required for BankEasy payment"), request.bankInfo ?: throw IllegalArgumentException("Bank info is required for BankEasy payment"))
            "MyPointComposite" -> {
                val pointPayment = request.subPayments.first { it.type == "MyPoint" }
                val otherPayment = request.subPayments.first { it.type != "MyPoint" }
                MyPointCompositePayment(
                    myPointPayment = MyPointPayment(pointPayment.amount ?: throw IllegalArgumentException("Amount is required for MyPoint sub-payment")),
                    otherPayment = mapToSinglePayment(otherPayment) as MyPointOtherPayment
                )
            }
            // ... 다른 복합 결제 타입 매핑
            else -> throw IllegalArgumentException("Unsupported payment method type: ${request.paymentMethodType}")
        }
    }

    private fun mapToSinglePayment(subPayment: SubPayment): SinglePayment {
        return when (subPayment.type) {
            "MyPoint" -> MyPointPayment(subPayment.amount ?: throw IllegalArgumentException("Amount is required for MyPoint sub-payment"))
            "MyMoney" -> MyMoneyPayment(subPayment.amount ?: throw IllegalArgumentException("Amount is required for MyMoney sub-payment"))
            "CardEasy" -> CardEasyPayment(subPayment.amount ?: throw IllegalArgumentException("Amount is required for CardEasy sub-payment"), subPayment.cardInfo ?: throw IllegalArgumentException("Card info is required for CardEasy sub-payment"))
            "BankEasy" -> BankEasyPayment(subPayment.amount ?: throw IllegalArgumentException("Amount is required for BankEasy sub-payment"), subPayment.bankInfo ?: throw IllegalArgumentException("Bank info is required for BankEasy sub-payment"))
            else -> throw IllegalArgumentException("Unsupported single payment type: ${subPayment.type}")
        }
    }

    data class Request(
        val orderId: OrderId,
        val paymentMethodType: String,
        val amount: Amount? = null, // 단일 결제 시 사용
        val cardInfo: String? = null,
        val bankInfo: String? = null,
        val subPayments: List<SubPayment> = emptyList(), // 복합 결제 시 사용
        val paymentId: PaymentId? = null, // 낙관적 잠금을 위한 ID
        val version: Long? = null // 낙관적 잠금을 위한 버전
    )

    data class SubPayment(
        val type: String,
        val amount: Amount? = null,
        val cardInfo: String? = null,
        val bankInfo: String? = null
    )

    data class Response(
        val paymentId: PaymentId?,
        val status: String,
        val rewardPoints: Long
    )
}
