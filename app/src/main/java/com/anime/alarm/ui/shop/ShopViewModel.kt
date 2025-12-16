package com.anime.alarm.ui.shop

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import com.anime.alarm.data.billing.BillingClientWrapper
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ShopViewModel(private val billingClient: BillingClientWrapper) : ViewModel() {

    val productDetails: StateFlow<List<ProductDetails>> = billingClient.productDetails

    fun buyProduct(activity: Activity, productId: String) {
        billingClient.launchBillingFlow(activity, productId)
    }
}
