package io.capstone.keeper.android.features.category

import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.keeper.android.features.shared.components.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    var repository: CategoryRepository
): BaseViewModel() {


}