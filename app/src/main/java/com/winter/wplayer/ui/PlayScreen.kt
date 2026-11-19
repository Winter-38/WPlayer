package com.winter.wplayer.ui

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import com.winter.wplayer.IndexHolder
import com.winter.wplayer.MusicListViewModel
import com.winter.wplayer.MusicPlayerViewModel
import com.winter.wplayer.R
import com.winter.wplayer.ui.theme.Dimens
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayScreen(onBack: () -> Unit) {
    val activity = LocalActivity.current as? ComponentActivity
        ?: error("PlayScreen should be hosted in a Component Activity")
    val musicListViewModel: MusicListViewModel = viewModel(viewModelStoreOwner = activity)
    val musicPlayerViewModel: MusicPlayerViewModel = viewModel(viewModelStoreOwner = activity)

    val songs by musicListViewModel.songLists.collectAsStateWithLifecycle()
    val currentMediaId by IndexHolder.currentMediaId.collectAsStateWithLifecycle()
    val currentSong = remember(songs, currentMediaId) {
        songs.firstOrNull { it.songId.toString() == currentMediaId }
    }

    val controller by musicPlayerViewModel.controller.collectAsStateWithLifecycle()
    val isPlaying by musicPlayerViewModel.isPlaying.collectAsStateWithLifecycle()
    val isShuffleEnabled by musicPlayerViewModel.isShuffleEnabled.collectAsStateWithLifecycle()
    val repeatMode by musicPlayerViewModel.repeatMode.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.now_playing)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_left),
                            contentDescription = stringResource(R.string.back_button)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                BoxWithConstraints {
                    val coverSize = minOf(maxWidth, maxHeight)
                    SongCover(
                        coverUri = currentSong?.coverUri,
                        contentDescription = stringResource(R.string.song_cover),
                        size = coverSize,
                        shape = MaterialTheme.shapes.extraLarge
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    text = currentSong?.title ?: stringResource(R.string.unknown_title),
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = currentSong?.artist ?: stringResource(R.string.unknown_artist),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                PlaybackProgress(controller)
                Spacer(Modifier.height(8.dp))
                PlayerControls(
                    isPlaying = isPlaying == true,
                    isShuffleEnabled = isShuffleEnabled == true,
                    repeatMode = repeatMode,
                    onTogglePlay = {
                        if (isPlaying == true) musicPlayerViewModel.pause()
                        else musicPlayerViewModel.play()
                    },
                    onPrevious = { musicPlayerViewModel.seekToPrevious() },
                    onNext = { musicPlayerViewModel.seekToNext() },
                    onRepeat = { musicPlayerViewModel.switchRepeatMode() },
                    onShuffle = { musicPlayerViewModel.switchShuffledMode() }
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

/**
 * Polls the controller for position/duration so no data-layer change is needed.
 * Runs on the main dispatcher, which is what MediaController expects.
 */
@Composable
private fun PlaybackProgress(controller: MediaController?) {
    var position by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var dragValue by remember { mutableFloatStateOf(-1f) }

    LaunchedEffect(controller) {
        if (controller == null) return@LaunchedEffect
        while (isActive) {
            position = controller.currentPosition.coerceAtLeast(0L)
            duration = controller.duration.coerceAtLeast(0L)
            delay(500)
        }
    }

    val progress = if (duration > 0L) {
        (position.toFloat() / duration).coerceIn(0f, 1f)
    } else {
        0f
    }
    val sliderValue = if (dragValue >= 0f) dragValue else progress
    val shownPosition = if (dragValue >= 0f) (dragValue * duration).toLong() else position

    Column(Modifier.fillMaxWidth()) {
        Slider(
            value = sliderValue,
            onValueChange = { dragValue = it },
            onValueChangeFinished = {
                if (duration > 0L && dragValue >= 0f) {
                    controller?.seekTo((dragValue * duration).toLong())
                }
                dragValue = -1f
            },
            enabled = duration > 0L,
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatDuration(shownPosition),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatDuration(duration),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PlayerControls(
    isPlaying: Boolean,
    isShuffleEnabled: Boolean,
    repeatMode: Int,
    onTogglePlay: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onRepeat: () -> Unit,
    onShuffle: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // repeat
        IconButton(
            onClick = onRepeat,
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = if (repeatMode != Player.REPEAT_MODE_OFF) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        ) {
            Icon(
                painter = when (repeatMode) {
                    Player.REPEAT_MODE_OFF -> painterResource(R.drawable.ic_repeat_off)
                    Player.REPEAT_MODE_ALL -> painterResource(R.drawable.ic_repeat_all)
                    else -> painterResource(R.drawable.ic_repeat_one)
                },
                contentDescription = when (repeatMode) {
                    Player.REPEAT_MODE_OFF -> stringResource(R.string.repeat_off)
                    Player.REPEAT_MODE_ALL -> stringResource(R.string.repeat_all)
                    else -> stringResource(R.string.repeat_one)
                }
            )
        }

        // previous
        IconButton(
            onClick = onPrevious,
            modifier = Modifier.size(Dimens.touchTargetLarge)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_skipprevious_outline),
                contentDescription = stringResource(R.string.previous_button)
            )
        }

        // play / pause
        FilledIconButton(
            onClick = onTogglePlay,
            modifier = Modifier.size(Dimens.playButton)
        ) {
            Icon(
                painter = if (isPlaying) painterResource(R.drawable.ic_pause)
                else painterResource(R.drawable.ic_play_outline),
                contentDescription = stringResource(
                    if (isPlaying) R.string.pause_button else R.string.play_button
                ),
                modifier = Modifier.size(32.dp)
            )
        }

        // next
        IconButton(
            onClick = onNext,
            modifier = Modifier.size(Dimens.touchTargetLarge)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_skipnext_outline),
                contentDescription = stringResource(R.string.next_button)
            )
        }

        // shuffle
        IconButton(
            onClick = onShuffle,
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = if (isShuffleEnabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        ) {
            Icon(
                painter = if (isShuffleEnabled) painterResource(R.drawable.ic_shuffle)
                else painterResource(R.drawable.ic_shuffle_disabled),
                contentDescription = if (isShuffleEnabled) {
                    stringResource(R.string.shuffle_enabled_button)
                } else {
                    stringResource(R.string.shuffle_disabled_button)
                }
            )
        }
    }
}

private fun formatDuration(millis: Long): String {
    if (millis <= 0L) return "--:--"
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
