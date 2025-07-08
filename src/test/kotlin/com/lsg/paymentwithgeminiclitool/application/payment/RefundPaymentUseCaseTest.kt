package com.lsg.paymentwithgeminiclitool.application.payment

import com.lsg.paymentwithgeminiclitool.domain.common.Amount
import com.lsg.paymentwithgeminiclitool.domain.payment.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class RefundPaymentUseCaseTest : BehaviorSpec({

    val paymentRepository = FakePaymentRepository()
    val refundPaymentUseCase = RefundPaymentUseCase(paymentRepository)

    Given("완료된 결제가 존재할 때") {
        val payment = paymentRepository.save(
            Payment(
                orderId = OrderId(1L),
                paymentMethod = CardEasyPayment(Amount(10000), "1234-5678"),
                totalAmount = Amount(10000)
            )
        )
        val paymentId = payment.id!!

        When("전체 환불을 요청하면") {
            val request = RefundPaymentUseCase.Request(paymentId, Amount(10000))
            refundPaymentUseCase.invoke(request)

            Then("결제 상태는 REFUNDED가 되고, 환불 금액이 일치해야 한다") {
                val refundedPayment = paymentRepository.findById(paymentId)
                refundedPayment?.status shouldBe PaymentStatus.REFUNDED
                refundedPayment?.refundedAmount shouldBe Amount(10000)
            }
        }

        When("부분 환불을 요청하면") {
            // 새로운 결제 건으로 테스트
            val partialPayment = paymentRepository.save(
                Payment(
                    orderId = OrderId(2L),
                    paymentMethod = CardEasyPayment(Amount(10000), "5678-1234"),
                    totalAmount = Amount(10000)
                )
            )
            val partialPaymentId = partialPayment.id!!
            val request = RefundPaymentUseCase.Request(partialPaymentId, Amount(5000))
            refundPaymentUseCase.invoke(request)

            Then("결제 상태는 PARTIALLY_REFUNDED가 되고, 환불 금액이 일치해야 한다") {
                val refundedPayment = paymentRepository.findById(partialPaymentId)
                refundedPayment?.status shouldBe PaymentStatus.PARTIALLY_REFUNDED
                refundedPayment?.refundedAmount shouldBe Amount(5000)
            }
        }

        When("결제 금액보다 많은 금액을 환불 요청하면") {
            val request = RefundPaymentUseCase.Request(paymentId, Amount(15000))

            Then("IllegalStateException 예외가 발생해야 한다") {
                shouldThrow<IllegalStateException> {
                    refundPaymentUseCase.invoke(request)
                }
            }
        }
    }

    Given("취소된 결제가 존재할 때") {
        val canceledPayment = paymentRepository.save(
            Payment(
                orderId = OrderId(3L),
                paymentMethod = CardEasyPayment(Amount(10000), "9012-3456"),
                totalAmount = Amount(10000),
                status = PaymentStatus.CANCELED
            )
        )
        val paymentId = canceledPayment.id!!

        When("환불을 요청하면") {
            val request = RefundPaymentUseCase.Request(paymentId, Amount(10000))

            Then("IllegalStateException 예외가 발생해야 한다") {
                shouldThrow<IllegalStateException> {
                    refundPaymentUseCase.invoke(request)
                }
            }
        }
    }
})
