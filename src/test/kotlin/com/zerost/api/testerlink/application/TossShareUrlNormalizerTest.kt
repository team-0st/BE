package com.zerost.api.testerlink.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class TossShareUrlNormalizerTest {

    @Test
    fun `유효한 https URL을 정규화한다`() {
        val normalized = TossShareUrlNormalizer.normalize("  https://toss.im/_m/example  ")

        assertThat(normalized).isEqualTo("https://toss.im/_m/example")
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "http://toss.im/_m/example",
            "https://",
            "https://?x=1",
            "not-a-url",
            "",
            "   ",
        ],
    )
    fun `형식이 아니면 INVALID_INPUT_VALUE`(raw: String) {
        assertThatThrownBy {
            TossShareUrlNormalizer.normalize(raw)
        }
            .isInstanceOf(BusinessException::class.java)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INVALID_INPUT_VALUE)
    }

    @Test
    fun `길이가 2048을 초과하면 INVALID_INPUT_VALUE`() {
        val tooLong = "https://toss.im/" + "a".repeat(2048)

        assertThatThrownBy {
            TossShareUrlNormalizer.normalize(tooLong)
        }
            .isInstanceOf(BusinessException::class.java)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INVALID_INPUT_VALUE)
    }
}
