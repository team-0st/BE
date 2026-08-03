package com.zerost.api.shop.application

import com.zerost.api.support.createShop
import com.zerost.api.shop.domain.ShopRepository
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import kotlin.test.assertEquals

class ShopQueryServiceTest {

    private val shopRepository = mock(ShopRepository::class.java)
    private val shopQueryService = ShopQueryService(shopRepository)

    @Test
    fun `상점 목록을 응답 DTO 목록으로 변환한다`() {
        `when`(shopRepository.findAllByOrderByIdAsc()).thenReturn(
            listOf(
                createShop(id = 1L, name = "알맹상점1", description = "제로웨이스트 샵", imageUrl = "image-1"),
                createShop(id = 2L, name = "알맹상점2"),
            ),
        )

        val response = shopQueryService.getShops()

        assertEquals(2, response.size)
        assertEquals(1L, response[0].id)
        assertEquals("알맹상점1", response[0].name)
        assertEquals("제로웨이스트 샵", response[0].description)
        assertEquals("image-1", response[0].imageUrl)
        assertEquals(2L, response[1].id)
        assertEquals("알맹상점2", response[1].name)
    }
}
