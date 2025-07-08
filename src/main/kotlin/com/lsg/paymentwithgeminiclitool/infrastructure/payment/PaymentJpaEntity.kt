package com.lsg.paymentwithgeminiclitool.infrastructure.payment

import com.lsg.paymentwithgeminiclitool.domain.common.Amount
import com.lsg.paymentwithgeminiclitool.domain.payment.OrderId
import com.lsg.paymentwithgeminiclitool.domain.payment.Payment
import com.lsg.paymentwithgeminiclitool.domain.payment.PaymentId
import com.lsg.paymentwithgeminiclitool.domain.payment.PaymentMethod
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "payments")
class PaymentJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val orderId: Long,

    @Convert(converter = PaymentMethodConverter::class)
    @Column(columnDefinition = "TEXT")
    val paymentMethod: PaymentMethod,

    val totalAmount: Long,

    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Version
    val version: Long = 0L
) {
    fun toDomain(): Payment {
        return Payment(
            id = PaymentId(id!!),
            orderId = OrderId(orderId),
            paymentMethod = paymentMethod,
            totalAmount = Amount(totalAmount),
            createdAt = createdAt,
            version = version
        )
    }

    companion object {
        fun fromDomain(domain: Payment): PaymentJpaEntity {
            return PaymentJpaEntity(
                id = domain.id?.value,
                orderId = domain.orderId.value,
                paymentMethod = domain.paymentMethod,
                totalAmount = domain.totalAmount.value,
                createdAt = domain.createdAt,
                version = domain.version
            )
        }
    }
}
