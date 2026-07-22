package com.zerost.api.soup.domain

import org.springframework.data.jpa.repository.JpaRepository

interface SoupRerollPolicyCandidateRepository : JpaRepository<SoupRerollPolicyCandidate, Long> {
    fun findAllBySoupRerollPolicyGroupIdAndActiveTrueOrderByIdAsc(
        soupRerollPolicyGroupId: Long,
    ): List<SoupRerollPolicyCandidate>
}
