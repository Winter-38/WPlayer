package com.winter.wplayer.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import coil3.compose.AsyncImage
import com.winter.wplayer.IndexHolder
import com.winter.wplayer.MusicListViewModel
import com.winter.wplayer.MusicPlayerViewModel
import com.winter.wplayer.R
import com.winter.wplayer.ui.theme.ListBackgroundColor

@Composable
fun TopBar(musicListViewModel: MusicListViewModel, onNavToSetting: () -> Unit){
    var mainExpended by remember { mutableStateOf(false) }
    var sortExpended by remember { mutableStateOf(false) }
    var showCreate by remember { mutableStateOf(false) }

    Column {
        Spacer(Modifier.size(20.dp))
        Row(Modifier.height(65.dp)){
            Spacer(Modifier.size(10.dp))
            Text(
                modifier = Modifier.align(Alignment.CenterVertically),
                fontSize = 25.sp,
                text = stringResource(R.string.app_name)
            )
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier.align(Alignment.CenterVertically)
            ){
                IconButton(
                    onClick = {
                        mainExpended = true
                    },
                    modifier = Modifier
                        .size(50.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_dots),
                        contentDescription = stringResource(R.string.others)
                    )
                }

                DropdownMenu(
                    expanded = mainExpended,
                    onDismissRequest = { mainExpended = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.sort)) },
                        onClick = {
                            mainExpended = false
                            sortExpended = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.create_playlist)) },
                        onClick = {
                            mainExpended = false
                            showCreate = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.settings)) },
                        onClick = {
                            mainExpended = false
                            onNavToSetting()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.rescan)) },
                        onClick = {
                            mainExpended = false
                            musicListViewModel.rescan()
                        }
                    )
                }
                DropdownMenu(
                    expanded = sortExpended,
                    onDismissRequest = { sortExpended = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.title)) },
                        onClick = {
                            sortExpended = false
                            musicListViewModel.setSongOrder("TITLE")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.artist)) },
                        onClick = {
                            sortExpended = false
                            musicListViewModel.setSongOrder("ARTIST")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.size)) },
                        onClick = {
                            sortExpended = false
                            musicListViewModel.setSongOrder("SIZE")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.date_added)) },
                        onClick = {
                            sortExpended = false
                            musicListViewModel.setSongOrder("DATE_ADDED")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.date_modified)) },
                        onClick = {
                            sortExpended = false
                            musicListViewModel.setSongOrder("DATE_MODIFIED")
                        }
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
        }
    }
}

@Composable
fun PlayList(musicPlayerViewModel: MusicPlayerViewModel, musicListViewModel: MusicListViewModel, modifier: Modifier){
    val songs by musicListViewModel.songLists.collectAsStateWithLifecycle()
    val items by musicListViewModel.mediaItemLists.collectAsStateWithLifecycle()
    val controller by musicPlayerViewModel.controller.collectAsStateWithLifecycle()

    //检测items和controller值变化，防止异步错误
    LaunchedEffect( items, controller) {
        musicPlayerViewModel.syncQueue(items)
    }

    LazyColumn(modifier) {
        itemsIndexed(songs, key = { _, song -> song.songId }) { _, song ->
            Row(
                modifier = Modifier
                    .height(60.dp)
                    .fillMaxWidth()
                    .background(
                        color = ListBackgroundColor,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable {
                        musicPlayerViewModel.playSong(song)
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))
                AsyncImage(
                    model = song.coverUri,
                    contentDescription = stringResource(R.string.mini_song_cover),
                    error = painterResource(R.drawable.ic_play_outline),
                    fallback = painterResource(R.drawable.ic_play_outline),
                    modifier = Modifier.align(Alignment.CenterVertically).weight(3f)
                )

                Spacer(modifier = Modifier.weight(1f))
                Text(
                    modifier = Modifier
                        .weight(24f),
                    maxLines = 1,
                    text = song.title + " - " + song.artist
                )
            }
            Spacer(modifier = Modifier.padding(2.dp))
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun PlayBar(musicPlayerViewModel: MusicPlayerViewModel, musicListViewModel: MusicListViewModel, onNavToDetail: () -> Unit){
    val isPlaying by musicPlayerViewModel.isPlaying.collectAsStateWithLifecycle()
    val songs by musicListViewModel.songLists.collectAsStateWithLifecycle()
    val currentMediaId by IndexHolder.currentMediaId.collectAsStateWithLifecycle()
    val currentSong = remember(songs, currentMediaId) {
        songs.firstOrNull{ it.songId.toString() == currentMediaId }
    }
    val coverUri = currentSong?.coverUri ?: R.drawable.ic_play_outline
    val title = (currentSong?.title ?: stringResource(R.string.unknown_title)) +
            " - " + (currentSong?.artist ?: stringResource(R.string.unknown_artist))

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onNavToDetail()
                Log.d("PlayBar", currentSong.toString())
            }
    ) {
        Spacer(modifier = Modifier.size(16.dp))
        AsyncImage(
            model = coverUri,
            contentDescription = stringResource(R.string.mini_song_cover),
            error = painterResource(R.drawable.ic_play_outline),
            fallback = painterResource(R.drawable.ic_play_outline),
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .weight(0.21f)
                .clip(RoundedCornerShape(16.dp)),
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = title,
            maxLines = 1,
            modifier = Modifier
                .weight(1f)
                .basicMarquee(
                    iterations = Int.MAX_VALUE,
                    repeatDelayMillis = 500,
                    initialDelayMillis = 500,
                    velocity = 30.dp
                )
        )
        IconButton(
            onClick = {
                if (isPlaying == true) {
                    musicPlayerViewModel.pause()
                } else {
                    musicPlayerViewModel.play()
                }
            },
            modifier = Modifier.weight(0.2f),
            shape = CircleShape
        ) {
            Icon(
                painter = if (isPlaying == true) painterResource(R.drawable.ic_pause)
                else painterResource(R.drawable.ic_play_outline),
                contentDescription = stringResource(R.string.play_button)
            )
        }
        Spacer(modifier = Modifier.size(16.dp))
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