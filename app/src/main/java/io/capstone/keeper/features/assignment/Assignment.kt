package io.capstone.keeper.features.assignment

import android.os.Parcelable
import androidx.recyclerview.widget.DiffUtil
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import io.capstone.keeper.features.asset.Asset
import io.capstone.keeper.features.asset.AssetCore
import io.capstone.keeper.features.category.Category
import io.capstone.keeper.features.user.User
import io.capstone.keeper.features.user.UserCore
import kotlinx.android.parcel.Parcelize
import java.time.ZonedDateTime
import java.util.*

@Parcelize
data class Assignment @JvmOverloads constructor(
    var assignmentId: String = UUID.randomUUID().toString(),
    var asset: AssetCore? = null,
    var user: UserCore? = null,
    var dateAssigned: Timestamp? = Timestamp.now(),
    var dateReturned: Timestamp? = null,
    var location: String? = null,
    var remarks: String? = null
): Parcelable {

    companion object {
        const val COLLECTION = "assignments"
        const val FIELD_ID = "assignmentId"
        const val FIELD_ASSET = "asset"
        const val FIELD_ASSET_ID = "${FIELD_ASSET}.${Asset.FIELD_ID}"
        const val FIELD_ASSET_NAME = "${FIELD_ASSET}.${Asset.FIELD_NAME}"
        const val FIELD_CATEGORY = "${FIELD_ASSET}.${Asset.FIELD_CATEGORY}"
        const val FIELD_CATEGORY_ID = "${FIELD_ASSET}.${Asset.FIELD_CATEGORY}.${Category.FIELD_ID}"
        const val FIELD_USER = "user"
        const val FIELD_USER_ID = "${FIELD_USER}.${User.FIELD_ID}"
        const val FIELD_DATE_ASSIGNED = "dateAssigned"
        const val FIELD_DATE_RETURNED = "dateReturned"
        const val LOCATION = "location"
        const val REMARKS = "remarks"

        fun from(documentSnapshot: DocumentSnapshot): Assignment? {
            return documentSnapshot.toObject(Assignment::class.java)
        }

        val DIFF_CALLBACK = object: DiffUtil.ItemCallback<Assignment>() {
            override fun areContentsTheSame(oldItem: Assignment, newItem: Assignment): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(oldItem: Assignment, newItem: Assignment): Boolean {
                return oldItem.assignmentId == newItem.assignmentId
            }
        }
    }
}
