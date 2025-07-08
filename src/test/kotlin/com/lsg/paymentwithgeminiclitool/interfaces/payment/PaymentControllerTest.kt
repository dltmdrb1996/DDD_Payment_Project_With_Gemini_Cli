package com.lsg.paymentwithgeminiclitool.interfaces.payment

import com.fasterxml.jackson.databind.ObjectMapper
import com.lsg.paymentwithgeminiclitool.application.payment.ProcessPaymentUseCase
import com.lsg.paymentwithgeminiclitool.domain.payment.PaymentId
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@WebMvcTest(PaymentController::class)
class PaymentControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var processPaymentUseCase: ProcessPaymentUseCase

    @Test
    fun `결제_요청_API_호출_성공`() {
        // given
        val requestDto = PaymentRequestDto(
            orderId = 1L,
            paymentMethodType = "NPayMoney",
            amount = 10000
        )

        val useCaseResponse = ProcessPaymentUseCase.Response(
            paymentId = PaymentId(1L),
            status = "SUCCESS",
            rewardPoints = 100L
        )

        whenever(processPaymentUseCase.invoke(any())).thenReturn(useCaseResponse)

        // when & then
        mockMvc.post("/api/v1/payments") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(requestDto)
        }.andExpect {
            status { isOk() }
            jsonPath("$.paymentId") { value(1L) }
            jsonPath("$.status") { value("SUCCESS") }
            jsonPath("$.rewardPoints") { value(100L) }
        }
    }
}
