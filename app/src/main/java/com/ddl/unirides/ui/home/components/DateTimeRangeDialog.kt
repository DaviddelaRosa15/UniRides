package com.ddl.unirides.ui.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.firebase.Timestamp
import java.util.Calendar
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimeRangeDialog(
    currentFromDate: Timestamp?,
    currentToDate: Timestamp?,
    onDismiss: () -> Unit,
    onConfirm: (fromDate: Timestamp?, toDate: Timestamp?) -> Unit
) {
    var showingStep by remember { mutableStateOf(DateTimeStep.FROM_DATE) }

    val fromDateState = rememberDatePickerState(
        initialSelectedDateMillis = currentFromDate?.toDate()?.time ?: System.currentTimeMillis()
    )

    val fromCalendar = Calendar.getInstance().apply {
        if (currentFromDate != null) {
            time = currentFromDate.toDate()
        }
    }

    val fromTimeState = rememberTimePickerState(
        initialHour = fromCalendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = fromCalendar.get(Calendar.MINUTE)
    )

    val toDateState = rememberDatePickerState(
        initialSelectedDateMillis = currentToDate?.toDate()?.time ?: System.currentTimeMillis()
    )

    val toCalendar = Calendar.getInstance().apply {
        if (currentToDate != null) {
            time = currentToDate.toDate()
        }
    }

    val toTimeState = rememberTimePickerState(
        initialHour = toCalendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = toCalendar.get(Calendar.MINUTE)
    )

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (showingStep) {
                DateTimeStep.FROM_DATE -> {
                    DatePickerDialog(
                        onDismissRequest = onDismiss,
                        confirmButton = {
                            TextButton(
                                onClick = { showingStep = DateTimeStep.FROM_TIME }
                            ) {
                                Text("Siguiente")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = onDismiss) {
                                Text("Cancelar")
                            }
                        }
                    ) {
                        DatePicker(
                            state = fromDateState,
                            title = {
                                Text(
                                    text = "Fecha inicial",
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        )
                    }
                }

                DateTimeStep.FROM_TIME -> {
                    TimePickerDialog(
                        title = "Hora inicial",
                        onDismiss = onDismiss,
                        onConfirm = { showingStep = DateTimeStep.TO_DATE },
                        timePickerState = fromTimeState
                    )
                }

                DateTimeStep.TO_DATE -> {
                    DatePickerDialog(
                        onDismissRequest = onDismiss,
                        confirmButton = {
                            TextButton(
                                onClick = { showingStep = DateTimeStep.TO_TIME }
                            ) {
                                Text("Siguiente")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showingStep = DateTimeStep.FROM_TIME }) {
                                Text("Atrás")
                            }
                        }
                    ) {
                        DatePicker(
                            state = toDateState,
                            title = {
                                Text(
                                    text = "Fecha final",
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        )
                    }
                }

                DateTimeStep.TO_TIME -> {
                    TimePickerDialog(
                        title = "Hora final",
                        onDismiss = onDismiss,
                        onConfirm = {
                            // Combinar fecha y hora usando UTC para evitar problemas de zona horaria
                            val fromCalendar =
                                Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                                    timeInMillis = fromDateState.selectedDateMillis
                                        ?: System.currentTimeMillis()
                                }

                            // Crear fecha final en zona horaria local
                            val fromDateTime = Calendar.getInstance().apply {
                                set(Calendar.YEAR, fromCalendar.get(Calendar.YEAR))
                                set(Calendar.MONTH, fromCalendar.get(Calendar.MONTH))
                                set(Calendar.DAY_OF_MONTH, fromCalendar.get(Calendar.DAY_OF_MONTH))
                                set(Calendar.HOUR_OF_DAY, fromTimeState.hour)
                                set(Calendar.MINUTE, fromTimeState.minute)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }

                            val toCalendar =
                                Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                                    timeInMillis =
                                        toDateState.selectedDateMillis ?: System.currentTimeMillis()
                                }

                            // Crear fecha final en zona horaria local
                            val toDateTime = Calendar.getInstance().apply {
                                set(Calendar.YEAR, toCalendar.get(Calendar.YEAR))
                                set(Calendar.MONTH, toCalendar.get(Calendar.MONTH))
                                set(Calendar.DAY_OF_MONTH, toCalendar.get(Calendar.DAY_OF_MONTH))
                                set(Calendar.HOUR_OF_DAY, toTimeState.hour)
                                set(Calendar.MINUTE, toTimeState.minute)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }

                            onConfirm(
                                Timestamp(fromDateTime.time),
                                Timestamp(toDateTime.time)
                            )
                        },
                        timePickerState = toTimeState,
                        showBackButton = true,
                        onBack = { showingStep = DateTimeStep.TO_DATE }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    timePickerState: androidx.compose.material3.TimePickerState,
    showBackButton: Boolean = false,
    onBack: (() -> Unit)? = null
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            TimePicker(state = timePickerState)
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            Row {
                if (showBackButton && onBack != null) {
                    OutlinedButton(onClick = onBack) {
                        Text("Atrás")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                OutlinedButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        }
    )
}

private enum class DateTimeStep {
    FROM_DATE,
    FROM_TIME,
    TO_DATE,
    TO_TIME
}

