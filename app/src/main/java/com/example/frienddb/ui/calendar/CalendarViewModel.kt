package com.example.frienddb.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.frienddb.data.db.entities.DayUnavailability
import com.example.frienddb.data.repository.FriendRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth

data class FriendChip(val friendName: String, val color: Int, val reason: String)

data class CalendarUiState(
    val visibleMonth: YearMonth = YearMonth.now(),
    val dayChips: Map<Long, List<FriendChip>> = emptyMap(),
    val isLoading: Boolean = false
)

class CalendarViewModel(private val repository: FriendRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private val loadedMonths = mutableSetOf<YearMonth>()

    init {
        onMonthChanged(YearMonth.now())
    }

    fun onMonthChanged(month: YearMonth) {
        _uiState.update { it.copy(visibleMonth = month) }
        loadMonthRange(month)
    }

    fun navigateToPreviousMonth() {
        onMonthChanged(_uiState.value.visibleMonth.minusMonths(1))
    }

    fun navigateToNextMonth() {
        onMonthChanged(_uiState.value.visibleMonth.plusMonths(1))
    }

    fun reloadCurrentMonth() {
        loadedMonths.clear()
        _uiState.update { it.copy(dayChips = emptyMap()) }
        loadMonthRange(_uiState.value.visibleMonth)
    }

    private fun loadMonthRange(month: YearMonth) {
        // Load a wider range to ensure data is ready when scrolling fast
        (-2..2).forEach { offset ->
            val m = month.plusMonths(offset.toLong())
            if (m !in loadedMonths) {
                loadedMonths.add(m)
                loadMonth(m)
            }
        }
    }

    private fun loadMonth(month: YearMonth) {
        viewModelScope.launch {
            val rows = repository.getMonthUnavailabilities(month)
            val monthChips = buildDayChipMap(rows, month)
            _uiState.update { it.copy(dayChips = it.dayChips + monthChips) }
        }
    }

    private fun buildDayChipMap(
        rows: List<DayUnavailability>,
        month: YearMonth
    ): Map<Long, List<FriendChip>> {
        val monthStart = month.atDay(1).toEpochDay()
        val monthEnd   = month.atEndOfMonth().toEpochDay()
        val result     = mutableMapOf<Long, MutableList<FriendChip>>()
        for (row in rows) {
            val rangeStart = maxOf(row.startDate, monthStart)
            val rangeEnd   = minOf(row.endDate,   monthEnd)
            for (day in rangeStart..rangeEnd) {
                result.getOrPut(day) { mutableListOf() }
                    .add(FriendChip(row.friendName, row.friendColor, row.reason))
            }
        }
        return result
    }

    companion object {
        fun factory(repo: FriendRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { CalendarViewModel(repo) }
        }
    }
}
