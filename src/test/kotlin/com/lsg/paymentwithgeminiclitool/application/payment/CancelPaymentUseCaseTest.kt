package com.lsg.paymentwithgeminiclitool.application.payment

import com.lsg.paymentwithgeminiclitool.domain.common.Amount
import com.lsg.paymentwithgeminiclitool.domain.payment.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class CancelPaymentUseCaseTest : BehaviorSpec({

    val paymentRepository = FakePaymentRepository()
    val cancelPaymentUseCase = CancelPaymentUseCase(paymentRepository)

    Given("결제가 완료된 상태일 때") {
        val payment = paymentRepository.save(
            Payment(
                orderId = OrderId(1L),
                paymentMethod = CardEasyPayment(Amount(1000), "1234-5678"),
                totalAmount = Amount(1000),
                status = PaymentStatus.COMPLETED
            )
        )
        val paymentId = payment.id!!

        When("결제 취소를 요청하면") {
            val request = CancelPaymentUseCase.Request(paymentId)
            cancelPaymentUseCase.invoke(request)

            Then("결제 상태가 CANCELED로 변경되어야 한다") {
                val canceledPayment = paymentRepository.findById(paymentId)
                canceledPayment?.status shouldBe PaymentStatus.CANCELED
            }
        }
    }

    Given("이미 취소된 결제일 때") {
        val payment = paymentRepository.save(
            Payment(
                orderId = OrderId(2L),
                paymentMethod = CardEasyPayment(Amount(2000), "5678-1234"),
                totalAmount = Amount(2000),
                status = PaymentStatus.CANCELED
            )
        )
        val paymentId = payment.id!!

        When("다시 결제 취소를 요청하면") {
            val request = CancelPaymentUseCase.Request(paymentId)

            Then("IllegalStateException 예외가 발생해야 한다") {
                shouldThrow<IllegalStateException> {
                    cancelPaymentUseCase.invoke(request)
                }
            }
        }
    }

    Given("존재하지 않는 결제일 때") {
        val nonExistentPaymentId = PaymentId(999L)

        When("결제 취소를 요청하면") {
            val request = CancelPaymentUseCase.Request(nonExistentPaymentId)

            Then("NoSuchElementException 예외가 발생해야 한다") {
                shouldThrow<NoSuchElementException> {
                    cancelPaymentUseCase.invoke(request)
                }
            }
        }
    }
})
