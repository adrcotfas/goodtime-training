/*
 * Copyright 2022 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package goodtime.training.wod.timer.billing

import android.app.Activity
import android.app.Application
import androidx.annotation.Keep
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.ProductDetails
import goodtime.training.wod.timer.common.preferences.PreferenceHelper
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * The [AndroidViewModel] implementation combines all flows from the repo into a single one
 * that is collected in the Composable.
 *
 * It, also, has helper methods that are used to launch the Google Play Billing purchase flow.
 *
 */
class BillingViewModel @Keep constructor(
    application: Application,
    private val preferenceHelper: PreferenceHelper
) :
    AndroidViewModel(application) {
    private var billingClient: BillingClientWrapper =
        BillingClientWrapper(application, viewModelScope)
    private val _billingConnectionState = MutableLiveData(false)
    val billingConnectionState: LiveData<Boolean> = _billingConnectionState

    // Start the billing connection when the viewModel is initialized.
    init {
        billingClient.startBillingConnection(billingConnectionState = _billingConnectionState)
    }

    val proPending = billingClient.proPending

    init {
        viewModelScope.launch {
            billingClient.isNewPurchaseAcknowledged.collectLatest {
                if (!preferenceHelper.isPro()) {
                    preferenceHelper.setPro(it)
                }
            }
        }
    }

    /**
     * BillingFlowParams Builder for normal purchases.
     *
     * @param productDetails ProductDetails object returned by the library.
     * @return [BillingFlowParams] builder.
     */
    private fun billingFlowParamsBuilder(productDetails: ProductDetails): BillingFlowParams.Builder {
        return BillingFlowParams.newBuilder().setProductDetailsParamsList(
            listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .build()
            )
        )
    }

    /**
     * Use the Google Play Billing Library to make a purchase.
     *
     * @param activity [Activity] instance.
     */
    fun buy(activity: Activity) {
        billingClient.productDetails.value?.let {
            billingClient.launchBillingFlow(
                activity, billingFlowParamsBuilder(productDetails = it).build()
            )
        }
    }

    // When an activity is destroyed the viewModel's onCleared is called, so we terminate the
    // billing connection.
    override fun onCleared() = billingClient.terminateBillingConnection()
}
