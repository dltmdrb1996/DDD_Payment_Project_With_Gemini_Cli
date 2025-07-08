package com.lsg.paymentwithgeminiclitool.application.payment

import com.lsg.paymentwithgeminiclitool.domain.common.Amount
import com.lsg.paymentwithgeminiclitool.domain.payment.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ProcessPaymentUseCaseTest {

    private lateinit var useCase: ProcessPaymentUseCase
    private lateinit var paymentRepository: PaymentRepository
    private lateinit var rewardPointCalculator: RewardPointCalculator
    private lateinit var paymentMethodFactory: PaymentMethodFactory

    @BeforeEach
    fun setUp() {
        paymentRepository = FakePaymentRepository()
        rewardPointCalculator = RewardPointCalculatorImpl()
        paymentMethodFactory = PaymentMethodFactoryImpl()
        useCase = ProcessPaymentUseCase(paymentRepository, rewardPointCalculator, paymentMethodFactory)
    }

    @Test
    fun `단일_머니_결제_요청_처리_성공`() {
        val request = ProcessPaymentUseCase.Single(
            orderId = OrderId(1L),
            paymentMethodType = PaymentMethodType.MY_MONEY,
            amount = Amount(10000)
        )
        val response = useCase.invoke(request)
        assertEquals(100L, response.rewardPoints)
        assertInstanceOf(MyMoneyPayment::class.java, paymentRepository.findById(response.paymentId!!)?.paymentMethod)
    }

    @Test
    fun `포인트와_카드_복합결제_요청_처리_성공`() {
        val request = ProcessPaymentUseCase.Composite(
            orderId = OrderId(4L),
            paymentMethodType = PaymentMethodType.MY_POINT_COMPOSITE,
            subPayments = listOf(
                PaymentMethodFactory.SubPayment(SubPaymentType.MY_POINT, Amount(1000)),
                PaymentMethodFactory.SubPayment(SubPaymentType.CARD_EASY, Amount(9000), CardInfo("1234-5678-1234-5678"))
            )
        )
        val response = useCase.invoke(request)
        assertEquals(45L, response.rewardPoints)
        assertInstanceOf(MyPointCompositePayment::class.java, paymentRepository.findById(response.paymentId!!)?.paymentMethod)
    }
}
