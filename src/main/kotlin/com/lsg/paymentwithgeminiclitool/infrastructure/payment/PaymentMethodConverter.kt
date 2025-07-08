package com.lsg.paymentwithgeminiclitool.infrastructure.payment

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.lsg.paymentwithgeminiclitool.domain.payment.PaymentMethod
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class PaymentMethodConverter : AttributeConverter<PaymentMethod, String> {

    private val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())

    override fun convertToDatabaseColumn(attribute: PaymentMethod?): String? {
        return attribute?.let { objectMapper.writeValueAsString(it) }
    }

    override fun convertToEntityAttribute(dbData: String?): PaymentMethod? {
        return dbData?.let { objectMapper.readValue(it) }
    }
}
