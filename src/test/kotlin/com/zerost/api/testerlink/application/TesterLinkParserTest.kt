package com.zerost.api.testerlink.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class TesterLinkParserTest {

    @Test
    fun `유효한 intoss-private deep link를 파싱한다`() {
        val parsed = TesterLinkParser.parse(
            "intoss-private://0st?_deploymentId=019f893b-a962-71de-b3ea-2c2544ad7afa",
        )

        assertThat(parsed.deploymentId).isEqualTo("019f893b-a962-71de-b3ea-2c2544ad7afa")
        assertThat(parsed.deepLink).startsWith("intoss-private://0st")
    }

    @Test
    fun `다른 쿼리와 함께 있어도 _deploymentId 키만 읽는다`() {
        val parsed = TesterLinkParser.parse(
            "intoss-private://0st?x=1&_deploymentId=abc-123&y=2",
        )

        assertThat(parsed.deploymentId).isEqualTo("abc-123")
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "https://example.com/open",
            "intoss-private://0st?_deploymentId=abc junk",
            "intoss-private://0st?x=_deploymentId=abc",
            "intoss-private://0st",
            "intoss-private://0st?foo=bar",
            "intoss-private://0st?_deploymentId=",
        ],
    )
    fun `형식이 아니면 INVALID_INPUT_VALUE`(raw: String) {
        assertThatThrownBy {
            TesterLinkParser.parse(raw)
        }
            .isInstanceOf(BusinessException::class.java)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INVALID_INPUT_VALUE)
    }
}
