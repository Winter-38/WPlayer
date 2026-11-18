package com.winter.wplayer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.winter.wplayer.MusicListViewModel
import com.winter.wplayer.R
import com.winter.wplayer.room.Song
import com.winter.wplayer.ui.theme.ListBackgroundColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistManageScreen(musicListViewModel: MusicListViewModel, onNavToDetail: () -> Unit, onRemove: (MusicListViewModel, Long) -> Unit){
    val playlists by musicListViewModel.playlists.collectAsStateWithLifecycle()
    val songs by musicListViewModel.songLists.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )
    var showDialog by remember { mutableStateOf(false) }
    var showModal by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.size(35.dp))
        Button(
            onClick = { onNavToDetail() }
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_left),
                contentDescription = stringResource(R.string.back_button)
            )
        }
        LazyColumn {
            itemsIndexed(playlists){ index, playlist ->
                Row (
                    modifier = Modifier
                        .padding(vertical = 20.dp)
                        .clickable {
                            showModal = true
                        }
                ){
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(
                        text = "${index + 1}",
                        fontSize = 20.sp,
                        modifier = Modifier
                    )
                    Spacer(modifier = Modifier.size(10.dp))
                    AsyncImage(
                        model = playlist.coverUri,
                        contentDescription = stringResource(R.string.playlist_cover),
                        error = painterResource(R.drawable.ic_play_outline),
                        fallback = painterResource(R.drawable.ic_play_outline)
                    )
                    Spacer(modifier = Modifier.size(70.dp))
                    Text(
                        text = playlist.name,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = {
                            showDialog = true
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_delete_outline),
                            contentDescription = stringResource(R.string.delete)
                        )
                    }
                    if (showModal) {
                        ModalBottomSheet(
                            onDismissRequest = { showModal = false }
                        ) {
                            SongItems(
                                songs = songs,
                                action = { _, song ->
                                    musicListViewModel.addSongToPlaylist(playlist.playlistId, song)
                                    showModal = false
                                }
                            )
                        }
                    }
                }
                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text( stringResource(R.string.confirm) ) },
                        text = { Text( stringResource(R.string.confirm_danger)) },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showDialog = false
                                    onRemove(musicListViewModel, playlist.playlistId)
                                }
                            ) {
                                Text(stringResource(R.string.confirm))
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showDialog = false }
                            ) {
                                Text(stringResource(R.string.cancel))
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CreatePlayList(
    show: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    if (!show) return
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.create_playlist)) },
        text = {
            TextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(stringResource(R.string.playlist_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(text.trim()) },
                enabled = text.isNotBlank()
            ) { Text(stringResource(R.string.confirm)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}