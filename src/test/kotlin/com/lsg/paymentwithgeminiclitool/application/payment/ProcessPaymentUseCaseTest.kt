package com.lsg.paymentwithgeminiclitool.application.payment

import com.lsg.paymentwithgeminiclitool.domain.common.Amount
import com.lsg.paymentwithgeminiclitool.domain.payment.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.dao.OptimisticLockingFailureException

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
    fun `결제_요청이_오면_결제_애그리���잇을_생성하고_저장한_후_결과를_반환한다`() {
        // given
        val request = ProcessPaymentUseCase.Request(
            orderId = OrderId(1L),
            paymentMethodType = "MyMoney",
            amount = Amount(10000)
        )

        // when
        val response = useCase.invoke(request)

        // then
        assertNotNull(response.paymentId)
        assertEquals("SUCCESS", response.status)
        assertEquals(100L, response.rewardPoints) // 10000 * 0.01

        // 저장소 확인
        val savedPayment = paymentRepository.findById(response.paymentId!!)
        assertNotNull(savedPayment)
        assertEquals(request.orderId, savedPayment?.orderId)
        assertEquals(request.amount, savedPayment?.totalAmount)
        assertInstanceOf(MyMoneyPayment::class.java, savedPayment?.paymentMethod)
    }

    @Test
    fun `복합결제_요청_처리_성공`() {
        // given
        val request = ProcessPaymentUseCase.Request(
            orderId = OrderId(2L),
            paymentMethodType = "MyPointComposite",
            amount = Amount(10000), // totalAmount
            subPayments = listOf(
                ProcessPaymentUseCase.SubPayment("MyPoint", Amount(1000)),
                ProcessPaymentUseCase.SubPayment("CardEasy", Amount(9000), "1234-5678")
            )
        )

        // when
        val response = useCase.invoke(request)

        // then
        assertNotNull(response.paymentId)
        assertEquals("SUCCESS", response.status)
        assertEquals(45L, response.rewardPoints) // 9000 * 0.005

        val savedPayment = paymentRepository.findById(response.paymentId!!)
        assertNotNull(savedPayment)
        assertEquals(Amount(10000), savedPayment?.totalAmount)
        assertInstanceOf(MyPointCompositePayment::class.java, savedPayment?.paymentMethod)
    }

    @Test
    @Disabled("이 테스트는 ProcessPaymentUseCase가 ID로 기존 결제를 수정하는 기능을 지원할 때 유효합니다.")
    fun `동일한_결제_정보를_동시에_수정시_낙관적_잠금_실패`() {
        // given
        val request = ProcessPaymentUseCase.Request(
            orderId = OrderId(3L),
            paymentMethodType = "MyMoney",
            amount = Amount(5000)
        )

        // 첫 번째 저장 (version = 0 -> 1)
        val initialResponse = useCase.invoke(request)
        val initialPayment = paymentRepository.findById(initialResponse.paymentId!!)!!

        // 두 번째 요청 (첫 번째 요청과 동일한 PaymentId, 다른 version)
        val secondRequest = ProcessPaymentUseCase.Request(
            orderId = initialPayment.orderId,
            paymentMethodType = "MyMoney",
            amount = Amount(5000),
            paymentId = initialPayment.id,
            version = initialPayment.version - 1 // 이전 버전으로 요청
        )

        // when & then
        assertThrows(OptimisticLockingFailureException::class.java) {
            useCase.invoke(secondRequest)
        }
    }
}