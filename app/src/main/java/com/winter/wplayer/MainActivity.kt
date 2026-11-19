package com.winter.wplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.LaunchedEffect
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.winter.wplayer.room.AppDataBase
import com.winter.wplayer.room.PlaylistRepository
import com.winter.wplayer.ui.HomeScreen
import com.winter.wplayer.ui.PlayScreen
import com.winter.wplayer.ui.PlaylistManageScreen
import com.winter.wplayer.ui.RequestPermission
import com.winter.wplayer.ui.theme.AppTheme


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
                NavHost(
                    navController = navController,
                    startDestination = "home",
                    enterTransition = {
                        fadeIn(tween(220, easing = FastOutSlowInEasing)) +
                            slideInHorizontally(tween(320, easing = FastOutSlowInEasing)) { it / 4 }
                    },
                    exitTransition = {
                        fadeOut(tween(160)) +
                            slideOutHorizontally(tween(320, easing = FastOutSlowInEasing)) { -it / 8 }
                    },
                    popEnterTransition = {
                        fadeIn(tween(220, easing = FastOutSlowInEasing)) +
                            slideInHorizontally(tween(320, easing = FastOutSlowInEasing)) { -it / 4 }
                    },
                    popExitTransition = {
                        fadeOut(tween(160)) +
                            slideOutHorizontally(tween(320, easing = FastOutSlowInEasing)) { it / 8 }
                    }
                ) {
                    composable(
                        route = "home",
                        exitTransition = {
                            if (targetState.destination.route == "play_screen") {
                                ExitTransition.None
                            } else {
                                fadeOut(tween(160)) +
                                    slideOutHorizontally(
                                        tween(320, easing = FastOutSlowInEasing)
                                    ) { -it / 8 }
                            }
                        },
                        popEnterTransition = {
                            if (initialState.destination.route == "play_screen") {
                                EnterTransition.None
                            } else {
                                fadeIn(tween(220, easing = FastOutSlowInEasing)) +
                                    slideInHorizontally(
                                        tween(320, easing = FastOutSlowInEasing)
                                    ) { -it / 4 }
                            }
                        }
                    ) {
                        HomeScreen(
                            musicListViewModel = musicListViewModel,
                            musicPlayerViewModel = musicPlayerViewModel,
                            onNavToSettings = {
                                navController.navigate("playlist_manage_screen") {
                                    launchSingleTop = true
                                }
                            },
                            onNavToPlayScreen = {
                                navController.navigate("play_screen") {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                    composable(
                        route = "play_screen",
                        enterTransition = {
                            slideInVertically(
                                tween(320, easing = FastOutSlowInEasing)
                            ) { it } + fadeIn(tween(220))
                        },
                        exitTransition = {
                            slideOutVertically(
                                tween(320, easing = FastOutSlowInEasing)
                            ) { it } + fadeOut(tween(160))
                        },
                        popExitTransition = {
                            slideOutVertically(
                                tween(320, easing = FastOutSlowInEasing)
                            ) { it } + fadeOut(tween(220))
                        }
                    ) {
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
