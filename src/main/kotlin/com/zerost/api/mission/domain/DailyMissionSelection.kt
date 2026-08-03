package com.zerost.api.mission.domain

import com.zerost.api.common.entity.BaseEntity
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
import java.time.LocalDate

@Entity
@Table(name = "daily_mission_selections")
class DailyMissionSelection(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "selected_date", nullable = false)
    val selectedDate: LocalDate,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    val mission: Mission,

    @Enumerated(EnumType.STRING)
    @Column(name = "mission_category", nullable = false, length = 20)
    val missionCategory: MissionCategory,

    @Column(name = "display_order", nullable = false)
    val displayOrder: Int,
) : BaseEntity()
