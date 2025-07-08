package com.lsg.paymentwithgeminiclitool.domain.payment

import com.lsg.paymentwithgeminiclitool.domain.common.Amount
import org.springframework.stereotype.Component

@Component
class PaymentMethodFactoryImpl : PaymentMethodFactory {

    override fun create(request: PaymentMethodFactory.Request): PaymentMethod {
        return when (request.paymentMethodType) {
            PaymentMethodType.MY_POINT -> MyPointPayment(request.amount ?: throw IllegalArgumentException("Amount is required for MyPoint payment"))
            PaymentMethodType.MY_MONEY -> MyMoneyPayment(request.amount ?: throw IllegalArgumentException("Amount is required for MyMoney payment"))
            PaymentMethodType.CARD_EASY -> CardEasyPayment(request.amount ?: throw IllegalArgumentException("Amount is required for CardEasy payment"), request.cardInfo ?: throw IllegalArgumentException("Card info is required for CardEasy payment"))
            PaymentMethodType.BANK_EASY -> BankEasyPayment(request.amount ?: throw IllegalArgumentException("Amount is required for BankEasy payment"), request.bankInfo ?: throw IllegalArgumentException("Bank info is required for BankEasy payment"))
            PaymentMethodType.MY_POINT_COMPOSITE -> {
                val pointPayment = request.subPayments.first { it.type == SubPaymentType.MY_POINT }
                val otherPayment = request.subPayments.first { it.type != SubPaymentType.MY_POINT }
                MyPointCompositePayment(
                    myPointPayment = createSinglePayment(pointPayment) as MyPointPayment,
                    otherPayment = createSinglePayment(otherPayment) as MyPointOtherPayment
                )
            }
            PaymentMethodType.MY_MONEY_COMPOSITE -> {
                val moneyPayment = request.subPayments.first { it.type == SubPaymentType.MY_MONEY }
                val otherPayment = request.subPayments.first { it.type != SubPaymentType.MY_MONEY }
                MyMoneyCompositePayment(
                    myMoneyPayment = createSinglePayment(moneyPayment) as MyMoneyPayment,
                    otherPayment = createSinglePayment(otherPayment) as MyMoneyOtherPayment
                )
            }
            PaymentMethodType.MY_POINT_AND_MONEY_COMPOSITE -> {
                val pointPayment = request.subPayments.first { it.type == SubPaymentType.MY_POINT }
                val moneyPayment = request.subPayments.first { it.type == SubPaymentType.MY_MONEY }
                val otherPayment = request.subPayments.first { it.type != SubPaymentType.MY_POINT && it.type != SubPaymentType.MY_MONEY }
                MyPointAndMoneyCompositePayment(
                    myPointPayment = createSinglePayment(pointPayment) as MyPointPayment,
                    myMoneyPayment = createSinglePayment(moneyPayment) as MyMoneyPayment,
                    otherPayment = createSinglePayment(otherPayment) as MyPointAndMoneyOtherPayment
                )
            }
        }
    }

    private fun createSinglePayment(subPayment: PaymentMethodFactory.SubPayment): SinglePayment {
        return when (subPayment.type) {
            SubPaymentType.MY_POINT -> MyPointPayment(subPayment.amount ?: throw IllegalArgumentException("Amount is required for MyPoint sub-payment"))
            SubPaymentType.MY_MONEY -> MyMoneyPayment(subPayment.amount ?: throw IllegalArgumentException("Amount is required for MyMoney sub-payment"))
            SubPaymentType.CARD_EASY -> CardEasyPayment(subPayment.amount ?: throw IllegalArgumentException("Amount is required for CardEasy sub-payment"), subPayment.cardInfo ?: throw IllegalArgumentException("Card info is required for CardEasy sub-payment"))
            SubPaymentType.BANK_EASY -> BankEasyPayment(subPayment.amount ?: throw IllegalArgumentException("Amount is required for BankEasy sub-payment"), subPayment.bankInfo ?: throw IllegalArgumentException("Bank info is required for BankEasy sub-payment"))
        }
    }
}
