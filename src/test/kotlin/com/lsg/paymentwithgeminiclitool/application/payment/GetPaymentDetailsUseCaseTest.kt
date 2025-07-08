package com.lsg.paymentwithgeminiclitool.application.payment

import com.lsg.paymentwithgeminiclitool.domain.common.Amount
import com.lsg.paymentwithgeminiclitool.domain.payment.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime

class GetPaymentDetailsUseCaseTest : BehaviorSpec({

    val paymentRepository = FakePaymentRepository()
    val getPaymentDetailsUseCase = GetPaymentDetailsUseCase(paymentRepository)

    Given("결제 내역이 존재하는 경우") {
        val paymentId = PaymentId(1L)
        val orderId = OrderId(100L)
        val paymentMethod = CardEasyPayment(amount = Amount(1000), cardInfo = CardInfo("1234-5678-1234-5678"))
        val totalAmount = Amount(1000)
        val createdAt = LocalDateTime.now()

        val payment = Payment(
            id = paymentId,
            orderId = orderId,
            paymentMethod = paymentMethod,
            totalAmount = totalAmount,
            status = PaymentStatus.COMPLETED,
            createdAt = createdAt
        )
        paymentRepository.save(payment)

        When("존재하는 결제 ID로 조회를 요청하면") {
            val request = GetPaymentDetailsUseCase.Request(paymentId = paymentId)
            val response = getPaymentDetailsUseCase.invoke(request)

            Then("해당 결제의 상세 내역이 반환되어야 한다") {
                response.paymentId shouldBe paymentId
                response.orderId shouldBe orderId
                response.paymentMethod shouldBe paymentMethod
                response.totalAmount shouldBe totalAmount
                response.status shouldBe PaymentStatus.COMPLETED
                // createdAt은 근사치 비교 또는 별도 검증이 필요할 수 있음
            }
        }
    }

    Given("결제 내역이 존재하지 않는 경우") {
        val nonExistentPaymentId = PaymentId(999L)

        When("존재하지 않는 결제 ID로 조회를 요청하면") {
            val request = GetPaymentDetailsUseCase.Request(paymentId = nonExistentPaymentId)

            Then("예외가 발생해야 한다") {
                shouldThrow<NoSuchElementException> {
                    getPaymentDetailsUseCase.invoke(request)
                }
            }
        }
    }
})
