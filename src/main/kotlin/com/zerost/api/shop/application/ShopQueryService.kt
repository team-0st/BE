package com.zerost.api.shop.application

import com.zerost.api.shop.domain.ShopRepository
import com.zerost.api.shop.presentation.dto.ShopResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ShopQueryService(
    private val shopRepository: ShopRepository,
) {

    @Transactional(readOnly = true)
    fun getShops(): List<ShopResponse> {
        return shopRepository.findAllByOrderByIdAsc()
            .map { shop ->
                ShopResponse(
                    id = requireNotNull(shop.id),
                    name = shop.name,
                    description = shop.description,
                    imageUrl = shop.imageUrl,
                )
            }
    }
}
