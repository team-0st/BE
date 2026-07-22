package com.zerost.api.testerlink.domain

import com.zerost.api.common.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "tester_links")
class TesterLink(

    @Id
    val id: Long = SINGLETON_ID,

    @Column(name = "deep_link", nullable = false, length = 512)
    var deepLink: String,

    @Column(name = "deployment_id", nullable = false, length = 100)
    var deploymentId: String,

    @Column(name = "toss_share_url", length = 2048)
    var tossShareUrl: String? = null,

    @Column(name = "updated_by_user_id")
    var updatedByUserId: Long? = null,
) : BaseEntity() {

    fun update(
        deepLink: String,
        deploymentId: String,
        tossShareUrl: String,
        updatedByUserId: Long,
    ) {
        this.deepLink = deepLink
        this.deploymentId = deploymentId
        this.tossShareUrl = tossShareUrl
        this.updatedByUserId = updatedByUserId
    }

    companion object {
        const val SINGLETON_ID = 1L
    }
}
