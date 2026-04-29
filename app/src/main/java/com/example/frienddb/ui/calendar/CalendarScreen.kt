package com.example.frienddb.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.kizitonwose.calendar.compose.VerticalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.OutDateStyle
import com.kizitonwose.calendar.core.daysOfWeek
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onNavigateToEdit: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.reloadCurrentMonth()
        }
    }

    // Initialize the calendar range once to prevent state re-initialization during scrolls
    val initialMonth = remember { uiState.visibleMonth }
    val calendarState = rememberCalendarState(
        startMonth        = initialMonth.minusMonths(500),
        endMonth          = initialMonth.plusMonths(500),
        firstVisibleMonth = initialMonth,
        firstDayOfWeek    = DayOfWeek.MONDAY,
        outDateStyle      = OutDateStyle.EndOfRow
    )

    // Sync state only if external navigation happens (via buttons)
    LaunchedEffect(uiState.visibleMonth) {
        if (calendarState.firstVisibleMonth.yearMonth != uiState.visibleMonth && !calendarState.isScrollInProgress) {
            calendarState.animateScrollToMonth(uiState.visibleMonth)
        }
    }

    // Update ViewModel as user scrolls
    LaunchedEffect(calendarState) {
        snapshotFlow { calendarState.firstVisibleMonth }
            .collect { month ->
                viewModel.onMonthChanged(month.yearMonth)
            }
    }

    val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FriendDB") },
                actions = {
                    IconButton(onClick = viewModel::navigateToPreviousMonth) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous")
                    }
                    IconButton(onClick = viewModel::navigateToNextMonth) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
                    }
                    IconButton(onClick = onNavigateToEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            DaysOfWeekHeader()
            VerticalCalendar(
                modifier            = Modifier.weight(1f),
                state               = calendarState,
                calendarScrollPaged = false, // Enable free scrolling
                dayContent          = { day ->
                    DayCell(
                        day   = day,
                        chips = if (day.position == DayPosition.MonthDate)
                                    uiState.dayChips[day.date.toEpochDay()] ?: emptyList()
                                else emptyList(),
                        onChipClick = { chip ->
                            scope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                val message = chip.reason.ifBlank { "No reason provided" }
                                snackbarHostState.showSnackbar("${chip.friendName}: $message")
                            }
                        }
                    )
                },
                monthHeader = { month ->
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 8.dp, start = 16.dp),
                        text       = month.yearMonth.format(monthFormatter),
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
            Legend(uiState.dayChips.values.flatten().distinctBy { it.friendName })
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun Legend(friends: List<FriendChip>) {
    if (friends.isEmpty()) return
    
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "friends on the move",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
        ) {
            friends.forEach { friend ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color(friend.color))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = friend.friendName,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun DaysOfWeekHeader() {
    Row(modifier = Modifier.fillMaxWidth()) {
        daysOfWeek(firstDayOfWeek = DayOfWeek.MONDAY).forEach { dow ->
            Text(
                modifier  = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                text      = dow.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                style     = MaterialTheme.typography.labelSmall,
                color     = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun DayCell(
    day: CalendarDay,
    chips: List<FriendChip>,
    onChipClick: (FriendChip) -> Unit
) {
    val isCurrentMonth = day.position == DayPosition.MonthDate
    val isToday        = isCurrentMonth && day.date == LocalDate.now()

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(
                if (isToday) MaterialTheme.colorScheme.primaryContainer
                else Color.Transparent
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.fillMaxSize()
        ) {
            Text(
                text  = day.date.dayOfMonth.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = when {
                    !isCurrentMonth -> MaterialTheme.colorScheme.outlineVariant
                    isToday         -> MaterialTheme.colorScheme.onPrimaryContainer
                    else            -> MaterialTheme.colorScheme.onSurface
                }
            )
            val maxVisible = 3
            val visible    = chips.take(maxVisible)
            val overflow   = chips.size - maxVisible

            visible.forEach { chip ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 1.dp, vertical = 1.dp)
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(Color(chip.color))
                        .clickable { onChipClick(chip) }
                )
            }
            if (overflow > 0) {
                Text(
                    text  = "+$overflow",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp),
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
