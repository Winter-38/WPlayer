package com.winter.wplayer

import android.Manifest
import android.app.Application
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.VerticalAlignmentLine
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import coil3.compose.AsyncImage
import com.winter.wplayer.ui.theme.ListBackgroundColor
import com.winter.wplayer.ui.theme.ListBorderColor
import com.winter.wplayer.ui.theme.WPlayerTheme


class MainActivity : ComponentActivity() {

    companion object{
        private const val TAG = "MainActivityAction"
    }


    val musicListViewModel by lazy { MusicListViewModel(this.applicationContext as Application) }
    val musicPlayerViewModel by lazy { MusicPlayerViewModel(this.applicationContext as Application) }


    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            WPlayerTheme {
                RequestPermission()
                Column(modifier = Modifier.fillMaxSize()) {
                    PlayList(Modifier.weight(15f))
                    Surface(
                        modifier = Modifier
                            .weight(1.2f)
                            .padding(2.dp),
                        shape = RoundedCornerShape(15.dp),
                        color = Color.White,
                        border = BorderStroke(3.dp, ListBorderColor)
                    ) {
                        PlayBar()
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

        LaunchedEffect(Unit) {
            musicPlayerViewModel.buildController()
        }

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
                        .border(
                            width = 3.dp,
                            color = ListBorderColor,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    AsyncImage(
                        model = songs.get(index).coverUri,
                        contentDescription = "cover of each songs",
                        modifier = Modifier.weight(3f)
                    )

                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        modifier = Modifier
                            .weight(24f)
                            .clickable {
                                if (controller != null) {
                                    controller?.apply {
                                        seekTo(index, 0L)
                                        play()
                                    }
                                }
                            },
                        maxLines = 1,
                        text = "${song.title} - ${song.artist}"
                    )
                }
                Spacer(modifier = Modifier.padding(2.dp))
            }
        }
    }
    @OptIn(UnstableApi::class)
    @Composable
    fun PlayBar(){
        val controller by musicPlayerViewModel.controller.collectAsStateWithLifecycle()
        val songs by musicListViewModel.songLists.collectAsStateWithLifecycle()
        val songIndex by musicPlayerViewModel.songIndex.collectAsStateWithLifecycle()
        val coverUri = songs.getOrNull(songIndex)?.coverUri ?: R.drawable.ic_play_outline
        val title = songs.getOrNull(songIndex)?.title ?: "unknown title"

        Row(verticalAlignment = Alignment.CenterVertically){
            Spacer(modifier = Modifier.weight(1f))
            AsyncImage(
                model = coverUri,
                contentDescription = "cover of the playing song",
                modifier = Modifier
                    .weight(4f)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = title,
                maxLines = 1,
                modifier = Modifier
                    .width(40.dp)
                    .basicMarquee(
                        iterations = Int.MAX_VALUE,
                        repeatDelayMillis = 500,
                        initialDelayMillis = 500,
                        velocity = 30.dp
                    )
                    .weight(9f)
            )
            Row {
                //跳到上一首
                Button(
                    onClick = {
                        controller?.apply {
                            if (hasPreviousMediaItem()) seekToPrevious()
                        }
                    },
                    shape = CircleShape,
                    modifier = Modifier.width(50.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_skipprevious_outline),
                        contentDescription = "skip to previous song"
                    )
                }

                //(stop/play)
                Button(
                    onClick = {
                        controller?.apply {
                            if (isPlaying) pause()
                            else play()
                        }
                    },
                    modifier = Modifier.size(50.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_play_outline),
                        contentDescription = "play_button"
                    )
                }

                //跳到下一首
                Button(
                    onClick = {
                        controller?.apply {
                            if (hasNextMediaItem()) seekToNext()
                        }
                    },
                    shape = CircleShape,
                    modifier = Modifier.width(50.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_skipnext_outline),
                        contentDescription = "skip to next song"
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
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





