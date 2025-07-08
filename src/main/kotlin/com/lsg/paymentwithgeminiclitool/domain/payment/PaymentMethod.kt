package com.lsg.paymentwithgeminiclitool.domain.payment

import com.lsg.paymentwithgeminiclitool.domain.common.Amount

/**
 * 결제 방식을 나타내는 최상위 sealed interface.
 * ADT(Algebraic Data Type)의 Sum Type 역할을 하여, 허용된 결제 방식 외에는 존재할 수 없도록 강제합니다.
 */
sealed interface PaymentMethod

// --- 단일 결제 --- 

/**
 * 하나의 결제 수단으로만 결제하는 경우를 나타내는 sealed interface.
 */
sealed interface SinglePayment : PaymentMethod

data class MyPointPayment(
    val amount: Amount
) : SinglePayment, MyPointOtherPayment

data class MyMoneyPayment(
    val amount: Amount
) : SinglePayment, MyPointOtherPayment

data class CardEasyPayment(
    val amount: Amount,
    val cardInfo: String
) : SinglePayment, MyPointOtherPayment, MyMoneyOtherPayment, MyPointAndMoneyOtherPayment

data class BankEasyPayment(
    val amount: Amount,
    val bankInfo: String
) : SinglePayment, MyPointOtherPayment, MyMoneyOtherPayment, MyPointAndMoneyOtherPayment


// --- 복합 결제 --- 

/**
 * My Point와 함께 사용할 수 있는 다른 결제 수단을 나타내는 sealed interface.
 */
sealed interface MyPointOtherPayment

/**
 * My Point 복합 결제 (Product Type).
 * 반드시 MyPointPayment와 MyPointOtherPayment의 조합으로 구성됩니다.
 */
data class MyPointCompositePayment(
    val myPointPayment: MyPointPayment,
    val otherPayment: MyPointOtherPayment
) : PaymentMethod

/**
 * My Money와 함께 사용할 수 있는 다른 결제 수단을 나타내는 sealed interface.
 */
sealed interface MyMoneyOtherPayment

/**
 * My Money 복합 결제 (Product Type).
 * 반드시 MyMoneyPayment와 MyMoneyOtherPayment의 조합으로 구성됩니다.
 */
data class MyMoneyCompositePayment(
    val myMoneyPayment: MyMoneyPayment,
    val otherPayment: MyMoneyOtherPayment
) : PaymentMethod

/**
 * My Point, Money와 함께 사용할 수 있는 다른 결제 수단을 나타내는 sealed interface.
 */
sealed interface MyPointAndMoneyOtherPayment

/**
 * My Point & Money 3중 복합 결제 (Product Type).
 * 반드시 MyPointPayment, MyMoneyPayment, MyPointAndMoneyOtherPayment의 조합으로 구성됩니다.
 */
data class MyPointAndMoneyCompositePayment(
    val myPointPayment: MyPointPayment,
    val myMoneyPayment: MyMoneyPayment,
    val otherPayment: MyPointAndMoneyOtherPayment
) : PaymentMethod
