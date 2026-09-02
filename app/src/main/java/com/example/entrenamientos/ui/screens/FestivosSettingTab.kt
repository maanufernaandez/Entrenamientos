package com.example.entrenamientos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.entrenamientos.data.Holiday
import com.example.entrenamientos.ui.BasketViewModel

@Composable
fun FestivosSettingsTab(viewModel: BasketViewModel) {
    val holidays by viewModel.holidays.collectAsState()
    var newHolidayDate by remember { mutableStateOf("") }
    var holidayToDelete by remember { mutableStateOf<Holiday?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val scale = (configuration.screenWidthDp / 360f).coerceIn(0.85f, 1.25f)
    fun sp(base: Int) = (base * scale).sp

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Añadir Día Festivo", style = MaterialTheme.typography.titleMedium.copy(fontSize = sp(16)))
        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(
                onClick = {
                    val cal = java.util.Calendar.getInstance()
                    if (newHolidayDate.isNotEmpty()) {
                        try {
                            val parts = newHolidayDate.split("-")
                            cal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
                        } catch (_: Exception) {}
                    }

                    val y = cal.get(java.util.Calendar.YEAR)
                    val m = cal.get(java.util.Calendar.MONTH)
                    val d = cal.get(java.util.Calendar.DAY_OF_MONTH)

                    android.app.DatePickerDialog(context, { _, year, month, day ->
                        newHolidayDate = String.format(java.util.Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day)
                    }, y, m, d).show()
                },
                modifier = Modifier.weight(1f)
            ) {
                val formattedDisplayDate = if (newHolidayDate.isNotEmpty()) {
                    val parts = newHolidayDate.split("-")
                    if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}" else newHolidayDate
                } else "Seleccionar fecha"

                Text(
                    text = formattedDisplayDate,
                    color = if (newHolidayDate.isEmpty()) Color.Gray else Color.Black,
                    fontSize = sp(13)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    if (newHolidayDate.isNotEmpty()) {
                        if (holidays.none { it.date == newHolidayDate }) {
                            viewModel.addHoliday(newHolidayDate)
                            newHolidayDate = ""
                        } else {
                            android.widget.Toast.makeText(context, "Ese día ya es festivo", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        android.widget.Toast.makeText(context, "Por favor, selecciona una fecha primero", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.DarkGray,
                    disabledContainerColor = Color.LightGray
                ),
                enabled = newHolidayDate.isNotEmpty()
            ) {
                Text("Añadir", color = if (newHolidayDate.isNotEmpty()) Color.White else Color.DarkGray, fontSize = sp(13))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Festivos Registrados", style = MaterialTheme.typography.titleMedium.copy(fontSize = sp(16)))
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) {
            if (holidays.isEmpty()) {
                item {
                    Text("No hay días festivos configurados.", color = Color.Gray)
                }
            } else {
                items(holidays.sortedBy { it.date }) { holiday ->
                    val cellBg = Color.LightGray.copy(alpha = 0.2f)
                    val cellShape = MaterialTheme.shapes.small
                    val cellHeight = (42 * scale).dp

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val hDate = try { java.time.LocalDate.parse(holiday.date) } catch (e: Exception) { null }
                        val formatted = if (hDate != null) "${hDate.dayOfMonth} de ${hDate.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "ES"))} ${hDate.year}" else holiday.date

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(cellHeight)
                                .clip(cellShape)
                                .background(cellBg)
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = formatted,
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = sp(15)),
                                color = Color.Red,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(cellHeight)
                                .clip(cellShape)
                                .background(cellBg),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(onClick = { holidayToDelete = holiday }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = com.example.entrenamientos.ui.theme.AttendanceRed)
                            }
                        }
                    }
                }
            }
        }
    }

    if (holidayToDelete != null) {
        AlertDialog(
            onDismissRequest = { holidayToDelete = null },
            title = { Text("Eliminar Festivo", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
            text = { Text("¿Estás seguro de que quieres eliminar este día festivo?", textAlign = TextAlign.Center) },
            confirmButton = {},
            dismissButton = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { holidayToDelete = null }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) { Text("Cancelar", color = Color.White) }
                    Button(onClick = { viewModel.removeHoliday(holidayToDelete!!); holidayToDelete = null }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceRed)) { Text("Eliminar", color = Color.White) }
                }
            }
        )
    }
}