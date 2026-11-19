package com.winter.wplayer.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.winter.wplayer.IndexHolder
import com.winter.wplayer.MusicListViewModel
import com.winter.wplayer.MusicPlayerViewModel
import com.winter.wplayer.R
import com.winter.wplayer.room.Song
import com.winter.wplayer.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(musicListViewModel: MusicListViewModel, onNavToSetting: () -> Unit) {

    var menuExpanded by remember { mutableStateOf(false) }
    var showSortSheet by rememberSaveable { mutableStateOf(false) }
    var showCreate by remember { mutableStateOf(false) }
    val configPrefs by musicListViewModel.configSort.collectAsStateWithLifecycle()

    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        actions = {
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_dots),
                        contentDescription = stringResource(R.string.others)
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.sort)) },
                        onClick = {
                            menuExpanded = false
                            showSortSheet = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.create_playlist)) },
                        onClick = {
                            menuExpanded = false
                            showCreate = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.settings)) },
                        onClick = {
                            menuExpanded = false
                            onNavToSetting()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.rescan)) },
                        onClick = {
                            menuExpanded = false
                            musicListViewModel.rescan()
                        }
                    )
                }
            }
        }
    )

    if (showSortSheet) {
        SortSheet(
            currentOrder = configPrefs.first,
            currentSort = configPrefs.second,
            onDismiss = { showSortSheet = false },
            onOrderChange = { musicListViewModel.setSongOrder(it) },
            onSortChange = { musicListViewModel.setSongSort(it) }
        )
    }

    CreatePlayList(
        show = showCreate,
        onDismiss = { showCreate = false },
        onConfirm = { name ->
            musicListViewModel.createPlaylist(name = name, coverUri = null)
            showCreate = false
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortSheet(
    currentOrder: String,
    currentSort: String,
    onDismiss: () -> Unit,
    onOrderChange: (String) -> Unit,
    onSortChange: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val fields = listOf(
        "TITLE" to R.string.title,
        "ARTIST" to R.string.artist,
        "SIZE" to R.string.size,
        "DATE_ADDED" to R.string.date_added,
        "DATE_MODIFIED" to R.string.date_modified
    )

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.screenHPadding)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.sort),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(8.dp))
            fields.forEach { (order, label) ->
                ListItem(
                    headlineContent = { Text(stringResource(label)) },
                    trailingContent = {
                        RadioButton(
                            selected = currentOrder == order,
                            onClick = { onOrderChange(order) }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOrderChange(order) }
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = currentSort == "ASC",
                    onClick = { onSortChange("ASC") },
                    label = { Text(stringResource(R.string.sort_asc)) }
                )
                FilterChip(
                    selected = currentSort == "DESC",
                    onClick = { onSortChange("DESC") },
                    label = { Text(stringResource(R.string.sort_desc)) }
                )
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.confirm))
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    musicListViewModel: MusicListViewModel,
    musicPlayerViewModel: MusicPlayerViewModel,
    onNavToSettings: () -> Unit,
    onNavToPlayScreen: () -> Unit
) {
    val songs by musicListViewModel.songLists.collectAsStateWithLifecycle()
    val isPlaying by musicPlayerViewModel.isPlaying.collectAsStateWithLifecycle()
    val currentMediaId by IndexHolder.currentMediaId.collectAsStateWithLifecycle()
    val currentSong = remember(songs, currentMediaId) {
        songs.firstOrNull { it.songId.toString() == currentMediaId }
    }

    Scaffold(
        topBar = { TopBar(musicListViewModel, onNavToSettings) },
        bottomBar = {
            if (currentSong != null) {
                MiniPlayerBar(
                    song = currentSong,
                    isPlaying = isPlaying == true,
                    onTogglePlay = {
                        if (isPlaying == true) musicPlayerViewModel.pause()
                        else musicPlayerViewModel.play()
                    },
                    onClick = onNavToPlayScreen
                )
            }
        }
    ) { innerPadding ->
        if (songs.isEmpty()) {
            EmptyState(
                text = stringResource(R.string.empty_songs),
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
                items(songs, key = { it.songId }) { song ->
                    SongRow(
                        song = song,
                        onClick = { musicPlayerViewModel.playSong(song) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniPlayerBar(
    song: Song,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 3.dp,
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.miniPlayerHeight)
                .clickable(onClick = onClick)
                .padding(start = 12.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SongCover(
                coverUri = song.coverUri,
                contentDescription = stringResource(R.string.mini_song_cover),
                size = Dimens.coverMini,
                shape = MaterialTheme.shapes.small
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = song.title ?: stringResource(R.string.unknown_title),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist ?: stringResource(R.string.unknown_artist),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(8.dp))
            FilledIconButton(
                onClick = onTogglePlay,
                modifier = Modifier.size(Dimens.touchTarget)
            ) {
                Icon(
                    painter = if (isPlaying) painterResource(R.drawable.ic_pause)
                    else painterResource(R.drawable.ic_play_outline),
                    contentDescription = stringResource(
                        if (isPlaying) R.string.pause_button else R.string.play_button
                    )
                )
            }
        }
    }
}

@Composable
fun RequestPermission(
    onPermissionsGranted: () -> Unit = {}
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var hasRequested by rememberSaveable { mutableStateOf(false) }

    val audioPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    val permissionsToRequest = remember { arrayOf(audioPermission) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            showDialog = false
            onPermissionsGranted()
        } else {
            showDialog = true
        }
    }

    LaunchedEffect(Unit) {
        if (!hasRequested) {
            hasRequested = true
            val allGranted = permissionsToRequest.all {
                ContextCompat.checkSelfPermission(context, it) ==
                        PackageManager.PERMISSION_GRANTED
            }
            if (allGranted) {
                onPermissionsGranted()
            } else {
                permissionLauncher.launch(permissionsToRequest)
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.need_permission)) },
            text = { Text(stringResource(R.string.no_permission)) },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    val allGranted = permissionsToRequest.all {
                        ContextCompat.checkSelfPermission(context, it) ==
                                PackageManager.PERMISSION_GRANTED
                    }
                    if (allGranted) {
                        onPermissionsGranted()
                    } else {
                        val intent = Intent(
                            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null)
                        )
                        context.startActivity(intent)
                    }
                }) {
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
