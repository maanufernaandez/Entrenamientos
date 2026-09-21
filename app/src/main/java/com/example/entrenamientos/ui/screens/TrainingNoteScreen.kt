package com.example.entrenamientos.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.entrenamientos.logic.CategoryCatalog
import com.example.entrenamientos.ui.BasketViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

private const val MAX_PHOTO_DIMENSION = 900
private const val TARGET_PHOTO_BYTES = 300_000

private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
    if (degrees == 0f) return bitmap
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

private fun exifRotationDegrees(file: File): Float {
    return try {
        val exif = ExifInterface(file.absolutePath)
        when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
    } catch (_: Exception) {
        0f
    }
}

private fun compressBitmapToBase64(bitmap: Bitmap): String {
    var quality = 85
    var jpegBytes: ByteArray
    do {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        jpegBytes = stream.toByteArray()
        quality -= 15
    } while (jpegBytes.size > TARGET_PHOTO_BYTES && quality > 20)

    return Base64.encodeToString(jpegBytes, Base64.DEFAULT)
}

private fun rotateStoredPhoto(currentBase64: String, degrees: Float): String? {
    return try {
        val bytes = Base64.decode(currentBase64, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
        compressBitmapToBase64(rotateBitmap(bitmap, degrees))
    } catch (_: Exception) {
        null
    }
}

private fun compressPhotoToBase64(file: File): String? {
    val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.absolutePath, boundsOptions)

    var sampleSize = 1
    while (
        boundsOptions.outWidth / sampleSize > MAX_PHOTO_DIMENSION * 2 ||
        boundsOptions.outHeight / sampleSize > MAX_PHOTO_DIMENSION * 2
    ) {
        sampleSize *= 2
    }

    val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
    var bitmap = BitmapFactory.decodeFile(file.absolutePath, decodeOptions) ?: return null

    // Corregimos la orientación ANTES de escalar, para que el recorte a
    // MAX_PHOTO_DIMENSION se calcule ya sobre las dimensiones correctas
    // (una foto vertical girada 90° tiene ancho/alto intercambiados).
    bitmap = rotateBitmap(bitmap, exifRotationDegrees(file))

    val longestSide = maxOf(bitmap.width, bitmap.height)
    if (longestSide > MAX_PHOTO_DIMENSION) {
        val scale = MAX_PHOTO_DIMENSION.toFloat() / longestSide
        bitmap = Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * scale).toInt().coerceAtLeast(1),
            (bitmap.height * scale).toInt().coerceAtLeast(1),
            true
        )
    }

    return compressBitmapToBase64(bitmap)
}

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

    val teamColor = team?.colorHex?.let {
        try { Color(android.graphics.Color.parseColor(it)) } catch (_: Exception) { Color.Black }
    } ?: Color.Black

    val fullCategory = CategoryCatalog.displayName(team?.categoryYear, team?.gender)

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var photoBase64 by remember(existingNote) { mutableStateOf(existingNote?.photoBase64) }
    val hasPhoto = !photoBase64.isNullOrBlank()

    var isProcessingPhoto by remember { mutableStateOf(false) }
    var isRotatingPhoto by remember { mutableStateOf(false) }
    var pendingCaptureFile by remember { mutableStateOf<File?>(null) }
    var showPhotoDialog by remember { mutableStateOf(false) }
    var photoError by remember { mutableStateOf<String?>(null) }

    fun createTempCaptureFile(): File {
        val dir = File(context.cacheDir, "training_photos_tmp")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "capture_${System.currentTimeMillis()}.jpg")
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val capturedFile = pendingCaptureFile
        pendingCaptureFile = null

        if (success && capturedFile != null) {
            isProcessingPhoto = true
            photoError = null

            scope.launch {
                val base64 = withContext(Dispatchers.IO) {
                    val result = try {
                        compressPhotoToBase64(capturedFile)
                    } catch (_: Exception) {
                        null
                    }
                    capturedFile.delete()
                    result
                }

                isProcessingPhoto = false

                if (base64 == null) {
                    photoError = "No se ha podido procesar la foto. Inténtalo de nuevo."
                } else {
                    photoBase64 = base64
                    viewModel.updateTrainingNotePhoto(
                        date = dateStr,
                        teamYear = teamYear,
                        type = noteType,
                        photoBase64 = base64,
                        existingNote = existingNote,
                        onError = { msg -> photoError = msg }
                    )
                }
            }
        } else capturedFile?.delete()
    }

    fun launchCamera() {
        val file = createTempCaptureFile()
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        pendingCaptureFile = file
        takePictureLauncher.launch(uri)
    }

    fun rotatePhoto() {
        val currentBase64 = photoBase64 ?: return
        isRotatingPhoto = true

        scope.launch {
            val rotated = withContext(Dispatchers.Default) {
                rotateStoredPhoto(currentBase64, 90f)
            }

            isRotatingPhoto = false

            if (rotated == null) {
                photoError = "No se ha podido girar la foto. Inténtalo de nuevo."
            } else {
                photoBase64 = rotated
                viewModel.updateTrainingNotePhoto(
                    date = dateStr,
                    teamYear = teamYear,
                    type = noteType,
                    photoBase64 = rotated,
                    existingNote = existingNote,
                    onError = { msg -> photoError = msg }
                )
            }
        }
    }

    fun deletePhoto() {
        photoBase64 = null
        viewModel.updateTrainingNotePhoto(
            date = dateStr,
            teamYear = teamYear,
            type = noteType,
            photoBase64 = null,
            existingNote = existingNote,
            onError = { msg -> photoError = msg }
        )
        showPhotoDialog = false
    }

    // Gestionar el botón "Atrás" del móvil
    // Si la foto está en grande, la cierra. Si no, vuelve al calendario.
    androidx.activity.compose.BackHandler(enabled = showPhotoDialog) {
        showPhotoDialog = false
    }

    androidx.activity.compose.BackHandler(enabled = !showPhotoDialog) {
        navController.popBackStack()
    }

    // Un Box padre que contiene la pantalla normal y, encima, la previsualización de la foto
    Box(modifier = Modifier.fillMaxSize()) {

        // ------------------------------------------------------------
        // PANTALLA NORMAL DE NOTAS
        // ------------------------------------------------------------
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(text = titlePrefix, style = MaterialTheme.typography.headlineMedium)
            Text(text = "$dayOfWeek ${date.dayOfMonth} de $monthName", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)

            Spacer(modifier = Modifier.height(8.dp))

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

                if (photoError != null) {
                    Text(photoError ?: "", color = com.example.entrenamientos.ui.theme.AttendanceRed, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(4.dp))
                }

                when {
                    isProcessingPhoto -> {
                        OutlinedButton(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Procesando foto...")
                        }
                    }
                    hasPhoto -> {
                        OutlinedButton(
                            onClick = { showPhotoDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Photo, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ver Foto")
                        }
                    }
                    else -> {
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
        // CAPA SUPERIOR: VISOR DE FOTO A PANTALLA COMPLETA
        // (Sustituye al problemático Dialog)
        // ------------------------------------------------------------
        if (showPhotoDialog && hasPhoto) {
            val bitmap = remember(photoBase64) {
                try {
                    val bytes = Base64.decode(photoBase64, Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                } catch (_: Exception) {
                    null
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    // Añadimos el inset padding nativo para evitar solapes con notch/barras en teléfonos "edge-to-edge"
                    .windowInsetsPadding(WindowInsets.safeDrawing)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Foto del entrenamiento",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                        )
                    } else {
                        Text("No se ha podido cargar la foto.", color = Color.White)
                    }
                }

                // Aquí están los botones. Respetan un margen de 16dp desde abajo, garantizando que no se cortan.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showPhotoDialog = false },
                        enabled = !isRotatingPhoto,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                    ) { Text("Cerrar", color = Color.White) }

                    Button(
                        onClick = { rotatePhoto() },
                        enabled = !isRotatingPhoto,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.LinkBlue)
                    ) {
                        if (isRotatingPhoto) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                        } else {
                            Icon(Icons.AutoMirrored.Filled.RotateRight, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Girar", color = Color.White)
                        }
                    }

                    Button(
                        onClick = { deletePhoto() },
                        enabled = !isRotatingPhoto,
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