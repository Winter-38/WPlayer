package com.winter.wplayer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.component1
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import com.winter.wplayer.ui.PlayScreen
import com.winter.wplayer.ui.theme.ListBackgroundColor
import com.winter.wplayer.ui.theme.ListBorderColor
import com.winter.wplayer.ui.theme.WPlayerTheme


class MainActivity : ComponentActivity() {

    companion object{
        private const val TAG = "MainActivityAction"
    }

    val musicListViewModel: MusicListViewModel by viewModels()
    val musicPlayerViewModel: MusicPlayerViewModel by viewModels()


    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            WPlayerTheme {
                LaunchedEffect(Unit) {
                    musicPlayerViewModel.buildController()
                }
                val navController = rememberNavController()
                RequestPermission()
                NavHost(navController, "home") {
                    composable("home") {
                        Column(modifier = Modifier.fillMaxSize()) {
                            PlayList(Modifier.weight(1f))
                            Surface(
                                modifier = Modifier
                                    .weight(0.09f)
                                    .padding(2.dp),
                                shape = RoundedCornerShape(15.dp),
                                color = Color.White
                            ) {
                                PlayBar {
                                    navController.navigate("play-screen") { launchSingleTop = true }
                                }
                            }
                        }
                    }
                    composable("play-screen") {
                        PlayScreen({ navController.popBackStack() } )
                    }
                }
            }
        }
    }

    @OptIn(UnstableApi::class)
    @Composable
    fun PlayList(modifier: Modifier){
        val songs by musicListViewModel.songLists.collectAsStateWithLifecycle()
        val items = musicListViewModel.songLists.collectAsStateWithLifecycle().value.map { MediaItem.fromUri(it.uri) }
        val controller by musicPlayerViewModel.controller.collectAsStateWithLifecycle()

        //检测items和controller值变化，防止异步错误
        LaunchedEffect( items, controller) {
            //检测controller不为null并且做播放前准备，添加isPlaying判断防止重进app打断播放
            if (controller != null && !(controller?.isPlaying ?: true)) {
                controller?.setMediaItems(items, 0, 0) //默认为第一首歌曲
                controller?.prepare()
            }
        }

        LazyColumn(modifier) {
            itemsIndexed(songs) { index, song ->
                Row(
                    modifier = Modifier
                        .height(60.dp)
                        .fillMaxWidth()
                        .background(
                            color = ListBackgroundColor,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            if (controller != null) {
                                controller?.apply {
                                    seekTo(index, 0L)
                                    play()
                                }
                            }
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    AsyncImage(
                        model = songs.get(index).coverUri,
                        contentDescription = "cover of each songs",
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
    fun PlayBar(onNavToDetail: () -> Unit){
        val controller by musicPlayerViewModel.controller.collectAsStateWithLifecycle()
        val songs by musicListViewModel.songLists.collectAsStateWithLifecycle()
        val songIndex by IndexHolder.songIndex.collectAsStateWithLifecycle()
        val coverUri = songs.getOrNull(songIndex)?.coverUri ?: R.drawable.ic_play_outline
        val title = (songs.getOrNull(songIndex)?.title ?: "unknown title") + " - " + (songs.getOrNull(songIndex)?.artist ?: "unknown artist")

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavToDetail() }
        ) {
            Spacer(modifier = Modifier.size(16.dp))
            AsyncImage(
                model = coverUri,
                contentDescription = "cover of the playing song",
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
            Button(
                onClick = {
                    controller?.apply {
                        if (isPlaying) pause()
                        else play()
                    }
                },
                modifier = Modifier.weight(0.2f),
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_play_outline),
                    contentDescription = "play_button"
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
                title = { Text("需要权限") },
                text = { Text("此应用需要该权限才能继续运行") },
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
                        Text("确认")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("取消")
                    }
                }
            )
        }
    }
}





