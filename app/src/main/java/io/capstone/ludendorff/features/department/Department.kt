package io.capstone.ludendorff.features.department

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.recyclerview.widget.DiffUtil
import com.algolia.instantsearch.core.highlighting.HighlightedString
import com.algolia.instantsearch.helper.highlighting.Highlightable
import com.algolia.search.model.Attribute
import com.google.firebase.firestore.Exclude
import io.capstone.ludendorff.components.utils.IDGenerator
import io.capstone.ludendorff.features.user.User
import io.capstone.ludendorff.features.user.UserCore
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Keep
@Serializable
@Parcelize
data class Department @JvmOverloads constructor(
    var departmentId: String = IDGenerator.generateRandom(),
    var name: String? = null,
    var manager: UserCore? = null,
    var count: Int = 0,
    @Exclude
    override var _highlightResult: @RawValue JsonObject? = null
): Parcelable, Highlightable {

    val highlightedName: HighlightedString?
        get() = getHighlight(Attribute(FIELD_NAME))
    val highlightedManager: HighlightedString?
        get() = getHighlight(Attribute(FIELD_MANAGER))

    fun minimize(): DepartmentCore {
        return DepartmentCore.from(this)
    }

    companion object {
        const val COLLECTION = "departments"
        const val FIELD_ID = "departmentId"
        const val FIELD_NAME = "name"
        const val FIELD_MANAGER = "manager"
        const val FIELD_MANAGER_ID = "${FIELD_MANAGER}.${User.FIELD_ID}"
        const val FIELD_MANAGER_NAME = "${FIELD_MANAGER}.${User.FIELD_NAME}"
        const val FIELD_COUNT = "count"

        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Department>() {
            override fun areItemsTheSame(oldItem: Department, newItem: Department): Boolean {
                return oldItem.departmentId == newItem.departmentId
            }

            override fun areContentsTheSame(oldItem: Department, newItem: Department): Boolean {
                return oldItem == newItem
            }
        }
    }
}