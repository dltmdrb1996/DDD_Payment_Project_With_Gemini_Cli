package com.lsg.paymentwithgeminiclitool.infrastructure.payment

import com.lsg.paymentwithgeminiclitool.domain.payment.Payment
import com.lsg.paymentwithgeminiclitool.domain.payment.PaymentId
import com.lsg.paymentwithgeminiclitool.domain.payment.PaymentRepository
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

@Repository
class InMemoryPaymentRepository : PaymentRepository {

    private val store = ConcurrentHashMap<PaymentId, PaymentJpaEntity>()
    private val sequence = AtomicLong(0)

    override fun save(payment: Payment): Payment {
        val currentVersion = payment.version
        val newId = payment.id ?: PaymentId(sequence.incrementAndGet())

        val existingEntity = store[newId]

        if (existingEntity != null && existingEntity.version != currentVersion) {
            throw OptimisticLockingFailureException("Optimistic locking failed for Payment with ID: ${newId.value}")
        }

        val newPaymentEntity = PaymentJpaEntity.fromDomain(payment.copy(version = currentVersion + 1, id = newId))
        store[newId] = newPaymentEntity
        return newPaymentEntity.toDomain()
    }

    override fun findById(paymentId: PaymentId): Payment? {
        return store[paymentId]?.toDomain()
    }
}