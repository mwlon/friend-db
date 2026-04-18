package com.example.frienddb.ui.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.frienddb.data.db.entities.UnavailabilityEntity
import com.example.frienddb.data.db.entities.UnavailabilityWithFriend
import com.example.frienddb.data.repository.FriendRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class FriendGroup(
    val friendName: String,
    val color: Int,
    val items: List<UnavailabilityWithFriend>
)

data class EditUiState(
    val groupedItems: List<FriendGroup> = emptyList(),
    val showAddDialog: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

class EditViewModel(private val repository: FriendRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(EditUiState())
    val uiState: StateFlow<EditUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.allUnavailabilitiesWithFriend.collect { list ->
                val groups = list
                    .groupBy { it.friend.name }
                    .map { (name, items) ->
                        FriendGroup(
                            friendName = name,
                            color      = items.first().friend.color,
                            items      = items.sortedByDescending { it.unavailability.startDate }
                        )
                    }
                    .sortedBy { it.friendName }
                _uiState.update { it.copy(groupedItems = groups) }
            }
        }
    }

    fun showAddDialog()  { _uiState.update { it.copy(showAddDialog = true) } }
    fun dismissDialog()  { _uiState.update { it.copy(showAddDialog = false) } }
    fun clearError()     { _uiState.update { it.copy(errorMessage = null) } }

    fun addUnavailability(name: String, startDate: LocalDate, endDate: LocalDate, reason: String) {
        if (name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Name cannot be empty") }
            return
        }
        if (endDate < startDate) {
            _uiState.update { it.copy(errorMessage = "End date must be on or after start date") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, showAddDialog = false) }
            repository.addUnavailability(name, startDate, endDate, reason)
            _uiState.update { it.copy(isSaving = false) }
        }
    }

    fun deleteUnavailability(entity: UnavailabilityEntity) {
        viewModelScope.launch {
            repository.deleteUnavailability(entity)
        }
    }

    companion object {
        fun factory(repo: FriendRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { EditViewModel(repo) }
        }
    }
}
