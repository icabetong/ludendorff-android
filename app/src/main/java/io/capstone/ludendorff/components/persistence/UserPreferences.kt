package io.capstone.ludendorff.components.persistence

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.google.firebase.firestore.Query
import io.capstone.ludendorff.features.asset.Asset
import io.capstone.ludendorff.features.inventory.InventoryReport
import io.capstone.ludendorff.features.issued.IssuedReport
import io.capstone.ludendorff.features.settings.core.CorePreferences
import io.capstone.ludendorff.features.settings.data.child.*
import io.capstone.ludendorff.features.stockcard.StockCard
import io.capstone.ludendorff.features.user.User
import java.util.*

class UserPreferences(private val context: Context?) {
    private val sharedPreference by lazy {
        PreferenceManager.getDefaultSharedPreferences(context!!)
    }

    enum class Theme {
        SYSTEM, DARK, LIGHT;

        companion object {
            fun parse(s: String?): Theme {
                return when(s) {
                    DARK.toString().lowercase(Locale.getDefault()) -> DARK
                    LIGHT.toString().lowercase(Locale.getDefault()) -> LIGHT
                    SYSTEM.toString().lowercase(Locale.getDefault()) -> SYSTEM
                    else -> SYSTEM
                }
            }
        }
    }

    var deviceToken: String?
        get() = sharedPreference.getString(PREFERENCE_DEVICE_TOKEN, null)
        set(value) {
            sharedPreference.edit {
                putString(PREFERENCE_DEVICE_TOKEN, value)
            }
        }

    var theme: Theme
        get() = Theme.parse(sharedPreference.getString(
            CorePreferences.PREFERENCE_KEY_THEME,
            Theme.SYSTEM.toString()))
        set(value) {
            sharedPreference.edit {
                putString(CorePreferences.PREFERENCE_KEY_THEME, value.toString())
            }
        }

    var sortDirection: Query.Direction
        get() {
            return if (sharedPreference.getString(CorePreferences.PREFERENCE_KEY_SORT,
                    "ascending") != "ascending")
                        Query.Direction.DESCENDING
            else Query.Direction.ASCENDING
        }
        set(value) {
            sharedPreference.edit {
                putString(CorePreferences.PREFERENCE_KEY_SORT,
                    value.toString().lowercase(Locale.getDefault())
                )
            }
        }

    val dataAssetsHeader: String
        get() = sharedPreference.getString(AssetDataDisplayFragment.PREFERENCE_KEY_ASSET_HEADER,
            Asset.FIELD_DESCRIPTION) ?: Asset.FIELD_DESCRIPTION
    val dataAssetSummary: String
        get() = sharedPreference.getString(AssetDataDisplayFragment.PREFERENCE_KEY_ASSET_SUMMARY,
            Asset.FIELD_SUBCATEGORY) ?: Asset.FIELD_SUBCATEGORY

    val dataInventoryOverline: String
        get() = sharedPreference.getString(InventoryDataDisplayFragment.PREFERENCE_KEY_INVENTORY_OVERLINE,
            InventoryReport.FIELD_YEAR_MONTH) ?: InventoryReport.FIELD_YEAR_MONTH
    val dataInventoryHeader: String
        get() = sharedPreference.getString(InventoryDataDisplayFragment.PREFERENCE_KEY_INVENTORY_HEADER,
            InventoryReport.FIELD_FUND_CLUSTER) ?: InventoryReport.FIELD_FUND_CLUSTER
    val dataInventorySummary: String
        get() = sharedPreference.getString(InventoryDataDisplayFragment.PREFERENCE_KEY_INVENTORY_SUMMARY,
            InventoryReport.FIELD_ENTITY_NAME) ?: InventoryReport.FIELD_ENTITY_NAME

    val dataIssuedOverline: String
        get() = sharedPreference.getString(IssuedDataDisplayFragment.PREFERENCE_KEY_ISSUED_OVERLINE,
            IssuedReport.FIELD_SERIAL_NUMBER) ?: IssuedReport.FIELD_SERIAL_NUMBER
    val dataIssuedHeader: String
        get() = sharedPreference.getString(IssuedDataDisplayFragment.PREFERENCE_KEY_ISSUED_HEADER,
            IssuedReport.FIELD_FUND_CLUSTER) ?: IssuedReport.FIELD_FUND_CLUSTER
    val dataIssuedSummary: String
        get() = sharedPreference.getString(IssuedDataDisplayFragment.PREFERENCE_KEY_ISSUED_SUMMARY,
            IssuedReport.FIELD_ENTITY_NAME) ?: IssuedReport.FIELD_ENTITY_NAME

    val dataStockCardOverline: String
        get() = sharedPreference.getString(StockCardDataDisplayFragment.PREFERENCE_KEY_STOCK_CARD_OVERLINE,
            StockCard.FIELD_STOCK_NUMBER) ?: StockCard.FIELD_STOCK_NUMBER
    val dataStockCardHeader: String
        get() = sharedPreference.getString(StockCardDataDisplayFragment.PREFERENCE_KEY_STOCK_CARD_HEADER,
            StockCard.FIELD_DESCRIPTION) ?: StockCard.FIELD_DESCRIPTION
    val dataStockCardSummary: String
        get() = sharedPreference.getString(StockCardDataDisplayFragment.PREFERENCE_KEY_STOCK_CARD_SUMMARY,
            StockCard.FIELD_ENTITY_NAME) ?: StockCard.FIELD_ENTITY_NAME

    val dataUserOverline: String
        get() = sharedPreference.getString(UserDataDisplayFragment.PREFERENCE_KEY_USER_OVERLINE,
            User.FIELD_POSITION) ?: User.FIELD_POSITION
    val dataUserSummary: String
        get() = sharedPreference.getString(UserDataDisplayFragment.PREFERENCE_KEY_USER_SUMMARY,
            User.FIELD_EMAIL) ?: User.FIELD_EMAIL

    companion object {
        const val PREFERENCE_DEVICE_TOKEN = "preference:device_token"

        fun notifyThemeChanged(theme: Theme) {
            when(theme) {
                Theme.LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                Theme.DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                Theme.SYSTEM -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
                }
            }
        }
    }
}