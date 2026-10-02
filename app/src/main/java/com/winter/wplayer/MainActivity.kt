package com.winter.wplayer

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import com.winter.wplayer.ui.theme.WPlayerTheme
import com.winter.core.ExoplayerManager
import com.winter.core.MusicViewModel


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WPlayerTheme {
                RequestPermission()
                PlayList()
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

@Composable
fun PlayList(){
    val context = LocalContext.current
    val app = context.applicationContext as Application
    val exoManager: ExoplayerManager = ExoplayerManager(app)
    val musicViewModel: MusicViewModel = MusicViewModel(app)
    val songs by musicViewModel.songs.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        musicViewModel.loadSongs()
    }

    LazyColumn {
        items(items = songs) { song ->
            Text(text = "${song.title} - ${song.artist}",
                 modifier = Modifier.clickable{
                     exoManager.play(MediaItem.fromUri(song.uri))
                 }
            )
        }
    }
}
