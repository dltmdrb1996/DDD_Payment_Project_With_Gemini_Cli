package com.lsg.paymentwithgeminiclitool.domain.common

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class AmountTest {

    @Test
    fun `금액이_0보다_크면_Amount_객체_생성_성공`() {
        // when & then
        assertDoesNotThrow {
            Amount(100L)
        }
    }

    @Test
    fun `금액이_0이면_Amount_객체_생성_성공`() {
        // when & then
        assertDoesNotThrow {
            Amount(0L)
        }
    }

    @Test
    fun `금액이_0보다_작으면_IllegalArgumentException_발생`() {
        // when & then
        assertThrows<IllegalArgumentException> {
            Amount(-100L)
        }
    }
}
