package com.zerost.api.testerlink.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

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
    fun `형식이 아니면 INVALID_INPUT_VALUE`() {
        assertThatThrownBy {
            TesterLinkParser.parse("https://example.com/open")
        }
            .isInstanceOf(BusinessException::class.java)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INVALID_INPUT_VALUE)
    }
}
