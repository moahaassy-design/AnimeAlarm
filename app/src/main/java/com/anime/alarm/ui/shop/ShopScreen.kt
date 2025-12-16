package com.anime.alarm.ui.shop

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anime.alarm.ui.AppViewModelProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    navigateBack: () -> Unit,
    viewModel: ShopViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val productDetails by viewModel.productDetails.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Character Shop") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (productDetails.isEmpty()) {
                Text("Loading products... or No products available.")
            } else {
                productDetails.forEach { product ->
                    Card(modifier = Modifier.padding(16.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = product.name, style = MaterialTheme.typography.titleLarge)
                            Text(text = product.description)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = {
                                viewModel.buyProduct(context as Activity, product.productId)
                            }) {
                                Text("Buy for ${product.oneTimePurchaseOfferDetails?.formattedPrice}")
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = navigateBack) {
                Text("Back")
            }
        }
    }
}
