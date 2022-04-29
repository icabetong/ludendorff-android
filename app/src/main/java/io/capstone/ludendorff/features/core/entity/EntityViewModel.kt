package io.capstone.ludendorff.features.core.entity

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.capstone.ludendorff.features.core.backend.Response
import io.capstone.ludendorff.features.shared.BaseViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EntityViewModel @Inject constructor(
    private val entityRepository: EntityRepository
): BaseViewModel() {

    private val _entity: Channel<Entity?> = Channel(Channel.BUFFERED)
    val entity = _entity.receiveAsFlow()

    init {
        fetch()
    }

    fun fetch() = viewModelScope.launch {
        val response = entityRepository.fetch()
        if (response is Response.Success)
            _entity.send(response.data)
        else _entity.send(null)
    }

    fun update(entity: Entity) = viewModelScope.launch {
        entityRepository.update(entity)
    }
}