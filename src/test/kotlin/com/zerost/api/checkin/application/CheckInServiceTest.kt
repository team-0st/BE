package com.zerost.api.checkin.application

import com.zerost.api.checkin.domain.CheckInRepository
import com.zerost.api.checkin.domain.CheckIn
import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.ingredient.domain.IngredientHistoryRepository
import com.zerost.api.ingredient.domain.IngredientType
import com.zerost.api.ingredient.domain.IngredientRepository
import com.zerost.api.ingredient.domain.UserIngredient
import com.zerost.api.ingredient.domain.UserIngredientRepository
import com.zerost.api.support.createIngredient
import com.zerost.api.support.createUser
import com.zerost.api.user.domain.UserRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.time.LocalDate
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CheckInServiceTest {

    private val userRepository = mock(UserRepository::class.java)
    private val ingredientRepository = mock(IngredientRepository::class.java)
    private val userIngredientRepository = mock(UserIngredientRepository::class.java)
    private val checkInRepository = mock(CheckInRepository::class.java)
    private val ingredientHistoryRepository = mock(IngredientHistoryRepository::class.java)
    private val checkInRandomProvider = mock(CheckInRandomProvider::class.java)
    private val checkInService = CheckInService(
        userRepository = userRepository,
        ingredientRepository = ingredientRepository,
        userIngredientRepository = userIngredientRepository,
        checkInRepository = checkInRepository,
        ingredientHistoryRepository = ingredientHistoryRepository,
        checkInRandomProvider = checkInRandomProvider,
    )

    @Test
    fun `출석 시 랜덤 일반 재료를 지급하고 출석 기록을 저장한다`() {
        val user = createUser()
        val ingredient1 = createIngredient(id = 3L, name = "양배추", imageUrl = "image-3")
        val ingredient2 = createIngredient(id = 4L, name = "토마토", imageUrl = "image-4")
        `when`(userRepository.findByDeviceId("device-1")).thenReturn(Optional.of(user))
        `when`(checkInRepository.existsByUserIdAndCheckedDate(1L, LocalDate.now())).thenReturn(false)
        `when`(ingredientRepository.findAllByType(IngredientType.COMMON)).thenReturn(listOf(ingredient1, ingredient2))
        `when`(checkInRandomProvider.nextInt(2)).thenReturn(1)
        `when`(userIngredientRepository.findByUserAndIngredient(user, ingredient2)).thenReturn(Optional.empty())
        `when`(userIngredientRepository.save(any(UserIngredient::class.java))).thenAnswer { it.arguments[0] as UserIngredient }
        `when`(checkInRepository.save(any(CheckIn::class.java))).thenAnswer { invocation ->
            val checkIn = invocation.arguments[0] as CheckIn
            CheckIn(
                id = 10L,
                user = checkIn.user,
                rewardedIngredient = checkIn.rewardedIngredient,
                checkedDate = checkIn.checkedDate,
            )
        }

        val response = checkInService.checkIn("device-1")

        assertEquals(4L, response.rewardedIngredient.id)
        assertEquals("토마토", response.rewardedIngredient.name)
        assertEquals("COMMON", response.rewardedIngredient.type)
        assertEquals("image-4", response.rewardedIngredient.imageUrl)
        verify(userIngredientRepository).save(any(UserIngredient::class.java))
        verify(checkInRepository).save(any())
        verify(ingredientHistoryRepository).save(any())
    }

    @Test
    fun `이미 출석한 유저면 예외가 발생한다`() {
        val user = createUser()
        `when`(userRepository.findByDeviceId("device-1")).thenReturn(Optional.of(user))
        `when`(checkInRepository.existsByUserIdAndCheckedDate(1L, LocalDate.now())).thenReturn(true)

        val exception = assertThrows<BusinessException> {
            checkInService.checkIn("device-1")
        }

        assertEquals(ErrorCode.ALREADY_CHECKED_IN, exception.errorCode)
        verify(ingredientRepository, never()).findAllByType(IngredientType.COMMON)
    }

    @Test
    fun `오늘 출석 여부를 조회할 수 있다`() {
        val user = createUser()
        `when`(userRepository.findByDeviceId("device-1")).thenReturn(Optional.of(user))
        `when`(checkInRepository.existsByUserIdAndCheckedDate(1L, LocalDate.now())).thenReturn(true)

        val response = checkInService.getTodayStatus("device-1")

        assertTrue(response.checkedIn)
    }

    @Test
    fun `일반 재료가 없으면 출석할 수 없다`() {
        val user = createUser()
        `when`(userRepository.findByDeviceId("device-1")).thenReturn(Optional.of(user))
        `when`(checkInRepository.existsByUserIdAndCheckedDate(1L, LocalDate.now())).thenReturn(false)
        `when`(ingredientRepository.findAllByType(IngredientType.COMMON)).thenReturn(emptyList())

        val exception = assertThrows<BusinessException> {
            checkInService.checkIn("device-1")
        }

        assertEquals(ErrorCode.INGREDIENT_NOT_FOUND, exception.errorCode)
    }
}
