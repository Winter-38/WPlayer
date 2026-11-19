package com.winter.wplayer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.winter.wplayer.MusicListViewModel
import com.winter.wplayer.R
import com.winter.wplayer.room.Playlist
import com.winter.wplayer.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistManageScreen(
    musicListViewModel: MusicListViewModel,
    onNavToDetail: () -> Unit,
    onRemove: (MusicListViewModel, Long) -> Unit
) {
    val playlists by musicListViewModel.playlists.collectAsStateWithLifecycle()
    val songs by musicListViewModel.songLists.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Dialog and sheet state are hoisted out of the list so exactly one of each
    // can ever be open, and it is always about the tapped playlist.
    var pendingDelete by remember { mutableStateOf<Playlist?>(null) }
    var addTarget by remember { mutableStateOf<Playlist?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.playlists)) },
                navigationIcon = {
                    IconButton(onClick = onNavToDetail) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_left),
                            contentDescription = stringResource(R.string.back_button)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (playlists.isEmpty()) {
            EmptyState(
                text = stringResource(R.string.empty_playlists),
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(
                    start = Dimens.screenHPadding,
                    end = Dimens.screenHPadding,
                    top = 8.dp,
                    bottom = 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(Dimens.listGap)
            ) {
                items(playlists, key = { it.playlistId }) { playlist ->
                    ListItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.small)
                            .clickable { addTarget = playlist },
                        colors = ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        leadingContent = {
                            SongCover(
                                coverUri = playlist.coverUri,
                                contentDescription = stringResource(R.string.playlist_cover),
                                size = Dimens.coverLarge,
                                shape = MaterialTheme.shapes.small
                            )
                        },
                        headlineContent = {
                            Text(
                                text = playlist.name,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        trailingContent = {
                            IconButton(onClick = { pendingDelete = playlist }) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_delete_outline),
                                    contentDescription = stringResource(R.string.delete),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )
                }
            }
        }
    }

    addTarget?.let { playlist ->
        ModalBottomSheet(
            onDismissRequest = { addTarget = null },
            sheetState = sheetState
        ) {
            Column(Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.add_songs_to, playlist.name),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = Dimens.screenHPadding)
                )
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxHeight(0.6f)) {
                    if (songs.isEmpty()) {
                        EmptyState(text = stringResource(R.string.empty_songs))
                    } else {
                        SongItems(
                            songs = songs,
                            onSongClick = { song ->
                                musicListViewModel.addSongToPlaylist(playlist.playlistId, song)
                                addTarget = null
                            }
                        )
                    }
                }
            }
        }
    }

    pendingDelete?.let { playlist ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(stringResource(R.string.delete)) },
            text = { Text(stringResource(R.string.confirm_danger)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDelete = null
                        onRemove(musicListViewModel, playlist.playlistId)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
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
            OutlinedTextField(
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
