package com.example.entrenamientos.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.entrenamientos.ui.theme.AttendanceGreen
import com.example.entrenamientos.ui.theme.AttendanceRed

@Composable
fun ActionButtonsRow(
    onCancel: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(
            onClick = onCancel,
            colors = ButtonDefaults.buttonColors(containerColor = AttendanceRed)
        ) {
            Text("Cancelar")
        }

        Button(
            onClick = onSave,
            colors = ButtonDefaults.buttonColors(containerColor = AttendanceGreen)
        ) {
            Text("Guardar", color = androidx.compose.ui.graphics.Color.Black)
        }
    }
}