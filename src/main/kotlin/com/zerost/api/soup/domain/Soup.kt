package com.zerost.api.soup.domain

import com.zerost.api.common.entity.BaseEntity
import com.zerost.api.recipe.domain.Recipe
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
@Table(name = "soups")
class Soup(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    val recipe: Recipe,

    @Enumerated(EnumType.STRING)
    @Column(name = "reward_grade", nullable = false, length = 30)
    var rewardGrade: SoupRewardGrade,

    @Column(name = "reward_eco_jam", nullable = false)
    var rewardEcoJam: Int = 0,

    @Column(name = "reward_point", nullable = false)
    var rewardPoint: Int = 0,

    @Column(nullable = false)
    var rerolled: Boolean = false,
) : BaseEntity() {

    fun markRerolled() {
        this.rerolled = true
    }
}
