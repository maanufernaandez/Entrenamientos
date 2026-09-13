package com.example.entrenamientos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.entrenamientos.data.Player


@Composable
fun QuintetosTotalDialog(
    txtActa: String,
    summonedPlayers: List<Player>,
    playerSortComparator: Comparator<Player>,
    is3x3: Boolean,
    isMini: Boolean,
    lineups: List<List<Long>>,
    totalQuarters: Int,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxWidth(0.98f).padding(16.dp),
        title = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(txtActa, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Black)
                HorizontalDivider(modifier = Modifier.padding(top = 8.dp), color = Color.Black, thickness = 1.dp)
            }
        },
        text = {
            val dialogListState = rememberLazyListState()

            LazyColumn(
                state = dialogListState,
                modifier = Modifier.fillMaxWidth().quintetosVerticalScrollShadow(dialogListState)
            ) {
                val sortedPlayersDialog = summonedPlayers.sortedWith(playerSortComparator)

                items(sortedPlayersDialog) { player ->
                    val dDisplay = player.dorsal?.takeIf { it.isNotBlank() } ?: "s.n."
                    val baseName = if (player.lastName.isNotBlank()) "${player.name} ${player.lastName}" else player.name

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (is3x3 || isMini) {
                            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = dDisplay,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(36.dp),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = baseName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            val quartersPlayed = lineups.count { it.contains(player.id) }
                            val qText = if (quartersPlayed == 1) "1 cuarto" else "$quartersPlayed cuartos"

                            Text(
                                text = "($qText)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray
                            )
                        } else {
                            Text(
                                text = baseName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Black,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = dDisplay,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(36.dp),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.width(12.dp))

                            val playerScrollState = rememberScrollState()

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(0.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .horizontalScrollShadow(playerScrollState)
                                    .horizontalScroll(playerScrollState)
                            ) {
                                for (qIndex in 0 until totalQuarters) {
                                    val playedInQuarter = lineups.getOrNull(qIndex)?.contains(player.id) == true

                                    val modifierCell = if (qIndex == 0) {
                                        Modifier.size(26.dp).border(1.dp, Color.DarkGray).background(Color.White)
                                    } else {
                                        Modifier.size(26.dp).offset(x = (-1 * qIndex).dp).border(1.dp, Color.DarkGray).background(Color.White)
                                    }

                                    Box(modifier = modifierCell, contentAlignment = Alignment.Center) {
                                        if (playedInQuarter) {
                                            Text(
                                                text = "X",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.Black,
                                                fontSize = 17.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
            ) {
                Text("Cerrar", color = Color.White)
            }
        }
    )
}