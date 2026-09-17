package com.example.services

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Clean architectural abstraction for Ads as requested by specification.
 * ADS_ENABLED is false by default.
 */
object AdsService {
    const val ADS_ENABLED = false

    enum class RewardedType {
        EXTRA_LIFE,
        COINS_50,
        EXTRA_HINT,
        EXTRA_UNDO
    }

    fun showRewardedAd(type: RewardedType, onReward: () -> Unit) {
        if (!ADS_ENABLED) {
            // When ads disabled or in offline sandbox, immediately fulfill the action
            onReward()
        }
    }

    fun showInterstitialAd() {
        if (!ADS_ENABLED) return
    }
}

/**
 * Clean Google Play Billing abstraction prepared for production integration.
 */
object IAPService {
    data class Product(
        val sku: String,
        val title: String,
        val priceFormatted: String,
        val description: String,
        val coinsReward: Int = 0,
        val isSubscription: Boolean = false
    )

    val products = listOf(
        Product("coins_small", "500 Neon Coins", "$0.99", "A pouch of vibrant neon coins", coinsReward = 500),
        Product("coins_medium", "1,500 Neon Coins", "$2.49", "A chest of glowing puzzle currency", coinsReward = 1500),
        Product("coins_large", "5,000 Neon Coins", "$4.99", "A vault of shimmering energy", coinsReward = 5000),
        Product("bundle_unlimited_undos", "Infinite Undos Pack", "$1.99", "Never worry about mistakes again"),
        Product("bundle_no_ads_vip", "ArrowFlux VIP Pass", "$3.99", "Permanent no ads, instant heart refills, exclusive golden trail", isSubscription = false)
    )

    fun purchase(sku: String, onSuccess: (Product) -> Unit) {
        val product = products.find { it.sku == sku }
        if (product != null) {
            onSuccess(product)
        }
    }
}

/**
 * Cloud & Firebase architecture ready interfaces with local fallback.
 */
interface ICloudSaveService {
    suspend fun uploadSave(jsonString: String): Boolean
    suspend fun downloadSave(): String?
}

object LocalCloudFallbackService : ICloudSaveService {
    override suspend fun uploadSave(jsonString: String): Boolean = true
    override suspend fun downloadSave(): String? = null
}
