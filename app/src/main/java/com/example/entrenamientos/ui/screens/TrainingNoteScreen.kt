package com.example.entrenamientos.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.entrenamientos.ui.BasketViewModel
import java.io.File

@Composable
fun TrainingNoteScreen(viewModel: BasketViewModel = hiltViewModel(), navController: NavController, noteType: String) {
    val dateStr by viewModel.selectedDate.collectAsState()
    val teamYear by viewModel.selectedTeamYear.collectAsState()
    val teamsList by viewModel.teams.collectAsState()

    val existingNote by viewModel.getTrainingNoteForDateAndTeam(dateStr, teamYear, noteType).collectAsState(initial = null)

    var noteContent by remember(existingNote) { mutableStateOf(existingNote?.content ?: "") }

    val team = teamsList.find { it.year == teamYear }
    val titlePrefix = if (noteType == "ENTRENAMIENTO") "Entrenamiento" else "Notas"

    val date = java.time.LocalDate.parse(dateStr)
    val dayOfWeek = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "ES")).replaceFirstChar { it.uppercase() }
    val monthName = date.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "ES"))

    // Lógica para obtener el color y la categoría completa del equipo
    val teamColor = team?.colorHex?.let {
        try { Color(android.graphics.Color.parseColor(it)) } catch (_: Exception) { Color.Black }
    } ?: Color.Black

    val genderStr = when (team?.gender) {
        "M" -> "Masculino"
        "F" -> "Femenino"
        else -> "Mixto"
    }

    val catSplit = team?.categoryYear?.split(" ") ?: emptyList()
    val fullCategory = if (catSplit.size >= 2 && (catSplit.last() == "1ª" || catSplit.last() == "2ª")) {
        val baseCat = catSplit.dropLast(1).joinToString(" ")
        "$baseCat $genderStr ${catSplit.last()}"
    } else if (!team?.categoryYear.isNullOrBlank()) {
        "${team?.categoryYear} $genderStr"
    } else {
        genderStr
    }

    // ------------------------------------------------------------
    // FOTO DEL ENTRENAMIENTO (solo aplica al tipo "ENTRENAMIENTO")
    // ------------------------------------------------------------
    val context = LocalContext.current

    // Ruta guardada en la nota; se sincroniza con Firestore, pero el propio
    // archivo de la foto vive solo en este dispositivo.
    var photoPath by remember(existingNote) { mutableStateOf(existingNote?.photoPath) }
    val photoFile = remember(photoPath) { photoPath?.let { File(it) } }
    val hasPhoto = photoFile != null && photoFile.exists()

    var pendingPhotoFile by remember { mutableStateOf<File?>(null) }
    var showPhotoDialog by remember { mutableStateOf(false) }

    fun createPhotoFile(): File {
        val dir = File(context.filesDir, "training_photos")
        if (!dir.exists()) dir.mkdirs()
        // Siempre el mismo nombre por día+equipo: una foto nueva sustituye a la anterior.
        return File(dir, "entrenamiento_${dateStr}_${teamYear}.jpg")
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && pendingPhotoFile != null) {
            val savedPath = pendingPhotoFile!!.absolutePath
            photoPath = savedPath
            viewModel.updateTrainingNotePhoto(
                date = dateStr,
                teamYear = teamYear,
                type = noteType,
                photoPath = savedPath,
                existingNote = existingNote
            )
        }
        pendingPhotoFile = null
    }

    fun launchCamera() {
        val file = createPhotoFile()
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        pendingPhotoFile = file
        takePictureLauncher.launch(uri)
    }

    fun deletePhoto() {
        photoFile?.delete()
        photoPath = null
        viewModel.updateTrainingNotePhoto(
            date = dateStr,
            teamYear = teamYear,
            type = noteType,
            photoPath = null,
            existingNote = existingNote
        )
        showPhotoDialog = false
    }

    androidx.activity.compose.BackHandler {
        navController.popBackStack()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = titlePrefix, style = MaterialTheme.typography.headlineMedium)
        Text(text = "$dayOfWeek ${date.dayOfMonth} de $monthName", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)

        Spacer(modifier = Modifier.height(8.dp))

        // Categoría centrada y con el color del equipo
        Text(
            text = fullCategory,
            style = MaterialTheme.typography.titleLarge,
            color = teamColor,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = noteContent,
            onValueChange = { noteContent = it },
            modifier = Modifier.fillMaxWidth().weight(1f),
            placeholder = { Text("Escribe aquí...") },
            textStyle = MaterialTheme.typography.bodyLarge
        )

        if (noteType == "ENTRENAMIENTO") {
            Spacer(modifier = Modifier.height(12.dp))

            if (hasPhoto) {
                OutlinedButton(
                    onClick = { showPhotoDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Photo, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ver Foto")
                }
            } else {
                OutlinedButton(
                    onClick = { launchCamera() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sacar Foto")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceRed)
            ) { Text("Cancelar", color = Color.White) }

            Button(
                onClick = {
                    viewModel.saveTrainingNote(date = dateStr, teamYear = teamYear, type = noteType, content = noteContent, existingNote = existingNote)
                    navController.popBackStack()
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceGreen)
            ) { Text("Guardar", color = Color.Black) }
        }
    }

    // ------------------------------------------------------------
    // DIÁLOGO PARA VER / ELIMINAR LA FOTO
    // ------------------------------------------------------------
    if (showPhotoDialog && hasPhoto) {
        val bitmap = remember(photoPath) {
            BitmapFactory.decodeFile(photoFile!!.absolutePath)
        }

        Dialog(onDismissRequest = { showPhotoDialog = false }) {
            Card(shape = MaterialTheme.shapes.medium) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Foto del entrenamiento",
                            modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)
                        )
                    } else {
                        Text("No se ha podido cargar la foto.")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { showPhotoDialog = false },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                        ) { Text("Cerrar", color = Color.White) }

                        Button(
                            onClick = { deletePhoto() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceRed)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Eliminar", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}