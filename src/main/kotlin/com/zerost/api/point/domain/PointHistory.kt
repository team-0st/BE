package com.zerost.api.point.domain

import com.zerost.api.common.entity.BaseEntity
import com.zerost.api.user.domain.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "point_histories")
class PointHistory(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(nullable = false)
    val amount: Int,

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 30)
    val sourceType: PointHistorySourceType,

    @Column(name = "source_id", nullable = false)
    val sourceId: Long,
) : BaseEntity() {

    companion object {
        fun earn(
            user: User,
            amount: Int,
            sourceType: PointHistorySourceType,
            sourceId: Long,
        ): PointHistory {
            return PointHistory(
                user = user,
                amount = amount,
                sourceType = sourceType,
                sourceId = sourceId,
            )
        }
    }
}
