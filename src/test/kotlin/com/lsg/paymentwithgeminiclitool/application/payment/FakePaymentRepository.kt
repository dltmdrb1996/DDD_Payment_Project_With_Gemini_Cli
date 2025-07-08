package com.lsg.paymentwithgeminiclitool.application.payment

import com.lsg.paymentwithgeminiclitool.domain.payment.Payment
import com.lsg.paymentwithgeminiclitool.domain.payment.PaymentId
import com.lsg.paymentwithgeminiclitool.domain.payment.PaymentRepository
import org.springframework.dao.OptimisticLockingFailureException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class FakePaymentRepository : PaymentRepository {

    private val store = ConcurrentHashMap<PaymentId, Payment>()
    private val sequence = AtomicLong(0)

    override fun save(payment: Payment): Payment {
        val currentVersion = payment.version
        val newId = payment.id ?: PaymentId(sequence.incrementAndGet())

        val existingPayment = store[newId]

        if (existingPayment != null && existingPayment.version != currentVersion) {
            throw OptimisticLockingFailureException("Optimistic locking failed for Payment with ID: ${newId.value}")
        }

        val newPayment = payment.copy(id = newId, version = currentVersion + 1)
        store[newId] = newPayment
        return newPayment
    }

    override fun findById(paymentId: PaymentId): Payment? {
        return store[paymentId]
    }
}