package com.winter.wplayer

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import com.winter.core.AudioItem
import com.winter.core.ExoplayerManager
import com.winter.wplayer.ui.theme.WPlayerTheme
import com.winter.core.MusicViewModel
import kotlinx.coroutines.flow.MutableStateFlow


class MainActivity : ComponentActivity() {

    lateinit var exoManager: ExoplayerManager
    val musicViewModel by lazy { MusicViewModel(this.applicationContext as Application) }
    var songIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exoManager = ExoplayerManager(this)

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



    @Composable
    fun PlayList(modifier: Modifier){
        val songs by musicViewModel.songs.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) {
            musicViewModel.loadSongs()
        }

        LazyColumn(modifier) {
            itemsIndexed(songs) { index, song ->
                Text(modifier = Modifier.clickable{
                    songIndex = index
                    exoManager.play(MediaItem.fromUri(song.uri))
                },
                    text ="${song.title} - ${song.artist}")

            }
        }
    }

    @Composable
    fun PlayBar(modifier: Modifier){
        val songs by musicViewModel.songs.collectAsStateWithLifecycle()
        val item by remember { mutableStateOf(songs.getOrNull(songIndex)) }
        Row(modifier){
            Button(
                onClick = {
                    val itemF = item
                    if (itemF != null) {
                        exoManager.play(MediaItem.fromUri(itemF.uri))
                    } else {
                        exoManager.pause()
                    }
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_skipprevious_outline),
                    contentDescription = "skip to previous song"
                )
            }
            Button(
                onClick = {
                    val state = exoManager.getState()
                    if (state == ExoplayerManager.PlayerState.PLAYING) {
                        exoManager.pause()
                    } else {
                        exoManager.play()
                    }
                },
                shape = CircleShape
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_play_outline),
                    contentDescription = "play_button"
                )
            }
            Button(
                onClick = {
                    TODO("skip next")
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




