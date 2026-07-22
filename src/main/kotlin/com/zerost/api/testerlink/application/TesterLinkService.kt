package com.zerost.api.testerlink.application

import com.zerost.api.testerlink.domain.TesterLink
import com.zerost.api.testerlink.domain.TesterLinkRepository
import com.zerost.api.testerlink.presentation.dto.AdminTesterLinkResponse
import com.zerost.api.testerlink.presentation.dto.CurrentTesterLinkResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.format.DateTimeFormatter

@Service
class TesterLinkService(
    private val testerLinkRepository: TesterLinkRepository,
    @Value("\${app.tester-link.share-url:https://zero-st.com/open}")
    private val shareUrl: String,
) {

    @Transactional(readOnly = true)
    fun getAdminTesterLink(): AdminTesterLinkResponse {
        val link = testerLinkRepository.findById(TesterLink.SINGLETON_ID).orElse(null)
        return toAdminResponse(link)
    }

    @Transactional(readOnly = true)
    fun getCurrentTesterLink(): CurrentTesterLinkResponse {
        val link = testerLinkRepository.findById(TesterLink.SINGLETON_ID).orElse(null)
        return CurrentTesterLinkResponse(
            deepLink = link?.deepLink,
            deploymentId = link?.deploymentId,
            tossShareUrl = link?.tossShareUrl,
        )
    }

    @Transactional
    fun updateTesterLink(
        rawDeepLink: String,
        rawTossShareUrl: String,
        adminUserId: Long,
    ): AdminTesterLinkResponse {
        val parsed = TesterLinkParser.parse(rawDeepLink)
        val tossShareUrl = TossShareUrlNormalizer.normalize(rawTossShareUrl)
        val existing = testerLinkRepository.findById(TesterLink.SINGLETON_ID).orElse(null)
        val saved = if (existing == null) {
            testerLinkRepository.save(
                TesterLink(
                    deepLink = parsed.deepLink,
                    deploymentId = parsed.deploymentId,
                    tossShareUrl = tossShareUrl,
                    updatedByUserId = adminUserId,
                ),
            )
        } else {
            existing.update(parsed.deepLink, parsed.deploymentId, tossShareUrl, adminUserId)
            existing
        }
        return toAdminResponse(saved)
    }

    private fun toAdminResponse(link: TesterLink?): AdminTesterLinkResponse =
        AdminTesterLinkResponse(
            shareUrl = shareUrl,
            deepLink = link?.deepLink,
            deploymentId = link?.deploymentId,
            tossShareUrl = link?.tossShareUrl,
            updatedAt = link?.updatedAt?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
        )
}
