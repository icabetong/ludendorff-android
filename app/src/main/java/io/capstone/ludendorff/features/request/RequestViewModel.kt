package io.capstone.ludendorff.features.request

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.ludendorff.features.shared.components.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class RequestViewModel @Inject constructor(
    firestore: FirebaseFirestore
): BaseViewModel() {

    private val requestQuery: Query = firestore.collection(Request.COLLECTION)

}