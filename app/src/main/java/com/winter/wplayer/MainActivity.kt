package com.winter.wplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.compose.AppTheme
import com.winter.wplayer.room.AppDataBase
import com.winter.wplayer.room.PlaylistRepository
import com.winter.wplayer.ui.PlayScreen
import com.winter.wplayer.ui.PlayBar
import com.winter.wplayer.ui.PlayList
import com.winter.wplayer.ui.PlaylistManageScreen
import com.winter.wplayer.ui.RequestPermission
import com.winter.wplayer.ui.TopBar


class MainActivity : ComponentActivity() {

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
            AppTheme {
                LaunchedEffect(Unit) {
                    musicPlayerViewModel.buildController()

                }
                val navController = rememberNavController()
                RequestPermission {
                    musicListViewModel.search()
                }
                NavHost(navController, "home") {
                    composable("home") {
                        Column(modifier = Modifier
                            .fillMaxSize()
                            .background(
                                color = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            TopBar(musicListViewModel) {
                                navController.navigate("playlist_manage_screen") {
                                    launchSingleTop = true
                                }
                            }
                            PlayList(
                                musicListViewModel = musicListViewModel,
                                musicPlayerViewModel = musicPlayerViewModel,
                                modifier = Modifier.weight(1f)
                            )
                            Surface(
                                modifier = Modifier
                                    .height(90.dp)
                                    .padding(2.dp),
                                shape = RoundedCornerShape(15.dp),
                                color = Color.White
                            ) {
                                PlayBar(
                                    musicListViewModel = musicListViewModel,
                                    musicPlayerViewModel = musicPlayerViewModel
                                ) {
                                    navController.navigate("play_screen") { launchSingleTop = true }
                                }
                            }
                        }
                    }
                    composable("play_screen") {
                        PlayScreen { navController.popBackStack() }
                    }
                    composable("playlist_manage_screen") {
                        PlaylistManageScreen(
                            musicListViewModel = musicListViewModel,
                            onNavToDetail = { navController.popBackStack() },
                            onRemove = { m, id ->
                                m.removePlaylist(id)
                            }
                        )
                    }
                }
            }
        }
    }
}





