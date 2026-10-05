package com.winter.wplayer

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import coil3.compose.AsyncImage
import com.winter.wplayer.ui.theme.WPlayerTheme


class MainActivity : ComponentActivity() {

    companion object{
        private const val TAG = "MainActivityAction"
    }


    val musicListViewModel by lazy { MusicListViewModel(this.applicationContext as Application) }
    val musicPlayerViewModel by lazy { MusicPlayerViewModel(this.applicationContext as Application) }
    private var songIndex = mutableStateOf(0)


    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            WPlayerTheme {
                RequestPermission()
                Column(modifier = Modifier.fillMaxSize()) {
                    PlayList(Modifier.weight(15f))
                    PlayBar(Modifier.weight(1f))
                }
            }
        }
    }

    @OptIn(UnstableApi::class)
    @Composable
    fun PlayList(modifier: Modifier){
        val songs by musicListViewModel.songs.collectAsStateWithLifecycle()
        val items by musicListViewModel.medias.collectAsStateWithLifecycle()
        val controller by musicPlayerViewModel.controller.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) {
            musicListViewModel.loadSongs()
            musicListViewModel.loadMediaItems()
            musicPlayerViewModel.buildController()
        }

        LaunchedEffect(musicPlayerViewModel.isReady, items, controller) {
            if (controller != null) {
                controller?.setMediaItems(items, 0, 0)
                controller?.prepare()
            }
        }

        LazyColumn(modifier) {
            itemsIndexed(songs) { index, song ->
                Text(modifier = Modifier.clickable{
                    if (controller != null) {
                        controller?.apply {
                            seekTo(index, 0L)
                            play()
                        }
                        songIndex.value = index
                    }
                },
                    text ="${song.title} - ${song.artist}")
            }
        }
    }


    @OptIn(UnstableApi::class)
    @Composable
    fun PlayBar(modifier: Modifier){
        val controller by musicPlayerViewModel.controller.collectAsStateWithLifecycle()
        val items by musicListViewModel.songs.collectAsStateWithLifecycle()
        val coverUri = items.getOrNull(songIndex.value)?.coverUri ?: R.drawable.ic_play_outline

        Row(modifier){
            AsyncImage(
                model = coverUri,
                contentDescription = "cover of the playing song",
                modifier = Modifier.size(80.dp)
            )

            //seekToPrevious Button
            Button(
                onClick = {
                    controller?.apply {
                        if (hasPreviousMediaItem()) seekToPrevious()
                    }
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_skipprevious_outline),
                    contentDescription = "skip to previous song"
                )
            }

            //(stop/play) button
            Button(
                onClick = {
                    controller?.apply {
                        if (isPlaying) pause()
                        else play()
                    }
                },
                shape = CircleShape
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_play_outline),
                    contentDescription = "play_button"
                )
            }

            //seekToNext Button
            Button(
                onClick = {
                    controller?.apply {
                        if (hasNextMediaItem()) seekToNext()
                    }
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_skipnext_outline),
                    contentDescription = "skip to next song"
                )
            }
        }
    }

    @Composable
    fun RequestPermission(){
        var showDialog by remember { mutableStateOf(false) }

        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions.values.all { it }) {
                showDialog = false
            } else {
                showDialog = true
            }
        }

        if (showDialog){
            androidx.compose.material3.AlertDialog(
                onDismissRequest = {},
                title = { Text("permission request") },
                text = { Text( "PLEASE give me the permission") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDialog = false
                            permissionLauncher.launch(arrayOf(
                                android.Manifest.permission.READ_MEDIA_AUDIO,
                                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                                android.Manifest.permission.FOREGROUND_SERVICE,
                                android.Manifest.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK
                            ))
                        }) {
                        Text("confirm")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDialog = false
                        }
                    ) {
                        Text("dismiss")
                    }
                }
            )
        }
    }
}




