package com.lsg.paymentwithgeminiclitool.domain.payment

import com.lsg.paymentwithgeminiclitool.domain.common.Amount
import org.springframework.stereotype.Component

@Component
class PaymentMethodFactoryImpl : PaymentMethodFactory {

    override fun create(request: PaymentMethodFactory.Request): PaymentMethod {
        return when (request.paymentMethodType) {
            "MyMoney" -> MyMoneyPayment(request.amount ?: throw IllegalArgumentException("Amount is required for MyMoney payment"))
            "CardEasy" -> CardEasyPayment(request.amount ?: throw IllegalArgumentException("Amount is required for CardEasy payment"), request.cardInfo ?: throw IllegalArgumentException("Card info is required for CardEasy payment"))
            "BankEasy" -> BankEasyPayment(request.amount ?: throw IllegalArgumentException("Amount is required for BankEasy payment"), request.bankInfo ?: throw IllegalArgumentException("Bank info is required for BankEasy payment"))
            "MyPointComposite" -> {
                val pointPayment = request.subPayments.first { it.type == "MyPoint" }
                val otherPayment = request.subPayments.first { it.type != "MyPoint" }
                MyPointCompositePayment(
                    myPointPayment = createSinglePayment(pointPayment) as MyPointPayment,
                    otherPayment = createSinglePayment(otherPayment) as MyPointOtherPayment
                )
            }
            // ... 다른 복합 결제 타입 매핑
            else -> throw IllegalArgumentException("Unsupported payment method type: ${request.paymentMethodType}")
        }
    }

    private fun createSinglePayment(subPayment: PaymentMethodFactory.SubPayment): SinglePayment {
        return when (subPayment.type) {
            "MyPoint" -> MyPointPayment(subPayment.amount ?: throw IllegalArgumentException("Amount is required for MyPoint sub-payment"))
            "MyMoney" -> MyMoneyPayment(subPayment.amount ?: throw IllegalArgumentException("Amount is required for MyMoney sub-payment"))
            "CardEasy" -> CardEasyPayment(subPayment.amount ?: throw IllegalArgumentException("Amount is required for CardEasy sub-payment"), subPayment.cardInfo ?: throw IllegalArgumentException("Card info is required for CardEasy sub-payment"))
            "BankEasy" -> BankEasyPayment(subPayment.amount ?: throw IllegalArgumentException("Amount is required for BankEasy sub-payment"), subPayment.bankInfo ?: throw IllegalArgumentException("Bank info is required for BankEasy sub-payment"))
            else -> throw IllegalArgumentException("Unsupported single payment type: ${subPayment.type}")
        }
    }
}
