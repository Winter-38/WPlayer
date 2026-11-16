package com.winter.wplayer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import coil3.compose.AsyncImage
import com.winter.wplayer.room.AppDataBase
import com.winter.wplayer.room.PlaylistRepository
import com.winter.wplayer.ui.PlayScreen
import com.winter.wplayer.ui.CreatePlayList
import com.winter.wplayer.ui.PlaylistManageScreen
import com.winter.wplayer.ui.theme.ListBackgroundColor
import com.winter.wplayer.ui.theme.WPlayerTheme


class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivityAction"
    }

    private val db by lazy {
        Room.databaseBuilder(applicationContext, AppDataBase::class.java, "music.db").build()
    }
    private val songDao by lazy { db.songDao() }
    private val playlistDao by lazy { db.playlistDao() }
    private val playlistSongDao by lazy { db.playlistSongDao() }

    private val repo by lazy {
        PlaylistRepository(db, songDao, playlistDao, playlistSongDao)
    }
    val musicListViewModel: MusicListViewModel by viewModels {
        MusicListViewModelFactory(application, repo)
    }
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
                RequestPermission { musicListViewModel.search("TITLE", "ASC") }
                NavHost(navController, "home") {
                    composable("home") {
                        Column(modifier = Modifier.fillMaxSize()) {
                            TopBar {
                                navController.navigate("playlist_manage_screen") { launchSingleTop = true }
                            }
                            PlayList(Modifier.weight(1f))
                            Surface(
                                modifier = Modifier
                                    .height(90.dp)
                                    .padding(2.dp),
                                shape = RoundedCornerShape(15.dp),
                                color = Color.White
                            ) {
                                PlayBar {
                                    navController.navigate("play_screen") { launchSingleTop = true }
                                }
                            }
                        }
                    }
                    composable("play_screen") {
                        PlayScreen{ navController.popBackStack() }
                    }
                    composable("playlist_manage_screen") {
                        PlaylistManageScreen(
                            musicListViewModel = musicListViewModel,
                            onNavToDetail = { navController.popBackStack() },
                            onRemove = { m, id->
                                m.removePlaylist(id)
                            }
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun TopBar(onNavToDetail: () -> Unit){
        var expended by remember { mutableStateOf(false) }
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
                            expended = true
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
                        expanded = expended,
                        onDismissRequest = { expended = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.sort)) },
                            onClick = {
                                expended = false
                                //sort by ...
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.create_playlist)) },
                            onClick = {
                                expended = false
                                showCreate = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.settings)) },
                            onClick = {
                                expended = false
                                onNavToDetail()
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
    fun PlayList(modifier: Modifier){
        val songs by musicListViewModel.songLists.collectAsStateWithLifecycle()
        val items = musicListViewModel.songLists.collectAsStateWithLifecycle().value.map { MediaItem.fromUri(it.uri) }

        //检测items和controller值变化，防止异步错误
        LaunchedEffect( items) {
            musicPlayerViewModel.initMedia(items)
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
                            musicPlayerViewModel.seekTo(index, 0L)
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    AsyncImage(
                        model = songs.get(index).coverUri,
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
    fun PlayBar(onNavToDetail: () -> Unit){
        val isPlaying by musicPlayerViewModel.isPlaying.collectAsStateWithLifecycle()
        val songs by musicListViewModel.songLists.collectAsStateWithLifecycle()
        val songIndex by IndexHolder.songIndex.collectAsStateWithLifecycle()
        val coverUri = songs.getOrNull(songIndex)?.coverUri ?: R.drawable.ic_play_outline
        val title = (songs.getOrNull(songIndex)?.title ?: stringResource(R.string.unknown_title)) +
                " - " + (songs.getOrNull(songIndex)?.artist ?: stringResource(R.string.unknown_artist))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavToDetail() }
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
}





