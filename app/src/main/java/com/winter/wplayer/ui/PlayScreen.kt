package com.winter.wplayer.ui

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Player
import coil3.compose.AsyncImage
import com.winter.wplayer.IndexHolder
import com.winter.wplayer.MusicListViewModel
import com.winter.wplayer.MusicPlayerViewModel
import com.winter.wplayer.R

@Composable
fun PlayScreen(onNavToDetail: () -> Unit) {
    //获取activity
    val activity = LocalActivity.current as? ComponentActivity ?: error("PlayScreen should be hosted in a Component Activity")
    //引用activity viewmodel
    val musicListViewModel: MusicListViewModel = viewModel(viewModelStoreOwner = activity)
    //播放列表&歌曲索引
    val songs by musicListViewModel.songLists.collectAsStateWithLifecycle()
    val currentMediaId by IndexHolder.currentMediaId.collectAsStateWithLifecycle()
    val currentSong = remember(songs, currentMediaId) {
        songs.firstOrNull{ it.songId.toString() == currentMediaId }
    }
    //歌曲封面&标题
    val coverUri = currentSong?.coverUri ?: R.drawable.ic_play_outline
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            //返回键-------------------------------
            Button(
                onClick = { onNavToDetail() },
                shape = CircleShape,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 30.dp).size(60.dp),
                contentPadding = PaddingValues(6.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_left),
                    contentDescription = stringResource(R.string.back_button)
                )
            }
            //封面--------------------------------
            AsyncImage(
                model = coverUri,
                contentDescription = stringResource(R.string.song_cover),
                error = painterResource(R.drawable.ic_play_outline),
                fallback = painterResource(R.drawable.ic_play_outline),
                modifier = Modifier
                    .size(300.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .align(Alignment.BottomCenter),
                contentScale = ContentScale.Crop
            )
        }

        //无无无无无无-------------------------------
        Spacer(modifier = Modifier.weight(0.09f))

        Column(modifier = Modifier
            .fillMaxWidth()
            .weight(0.8f)
        ) {
            //标题---------------------------------------------------------------------------------------标题
            Text(
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .basicMarquee(
                        iterations = Int.MAX_VALUE,
                        repeatDelayMillis = 500,
                        initialDelayMillis = 500,
                        velocity = 30.dp
                    )
                    .weight(0.9f),
                fontSize = 25.sp,
                textAlign = TextAlign.Center,
                text = currentSong?.title ?: stringResource(R.string.unknown_title)
            )

            Text(
                maxLines = 1,
                modifier = Modifier
                    .weight(0.5f)
                    .fillMaxWidth()
                    .basicMarquee(
                        iterations = Int.MAX_VALUE,
                        repeatDelayMillis = 500,
                        initialDelayMillis = 500,
                        velocity = 30.dp
                    ),
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                text = currentSong?.artist ?: stringResource(R.string.unknown_artist)
            )

            //按钮组-------------------------------------------------------------------------------------按钮组
            ButtonBar()
        }
    }
}

@Composable
fun ButtonBar() {
    val activity = LocalActivity.current as? ComponentActivity ?: error("PlayScreen should be hosted in a Component Activity")
    //Music Viewmodel
    val musicPlayerViewModel: MusicPlayerViewModel = viewModel(viewModelStoreOwner = activity)

    //播放器操控
    val controller by musicPlayerViewModel.controller.collectAsStateWithLifecycle()

    val isPlaying by musicPlayerViewModel.isPlaying.collectAsStateWithLifecycle()
    val isShuffleEnabled by musicPlayerViewModel.isShuffleEnabled.collectAsStateWithLifecycle()
    val repeatMode by musicPlayerViewModel.repeatMode.collectAsStateWithLifecycle()
    Row (
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Top
    ) {
        //切换循环模式-------------------
        IconButton(
            onClick = {
                musicPlayerViewModel.switchRepeatMode()
            },
            modifier =  Modifier
                .size(80.dp)
                .padding(8.dp)
                .align(Alignment.CenterVertically)
        ) {
            Icon (
                painter = when (repeatMode) {
                    Player.REPEAT_MODE_OFF -> painterResource(R.drawable.ic_repeat_off)
                    Player.REPEAT_MODE_ALL -> painterResource(R.drawable.ic_repeat_all)
                    else -> painterResource(R.drawable.ic_repeat_one) //Player.REPEAT_MODE_ONE
                },
                contentDescription = when (repeatMode) {
                    Player.REPEAT_MODE_OFF -> stringResource(R.string.repeat_off)
                    Player.REPEAT_MODE_ALL -> stringResource(R.string.repeat_all)
                    else -> stringResource(R.string.repeat_one)
                }
            )
        }

        //跳到上一首----------------------
        IconButton(
            onClick = {
                musicPlayerViewModel.seekToPrevious()
            },
            shape = CircleShape,
            modifier = Modifier
                .size(80.dp)
                .padding(8.dp)
                .align(Alignment.CenterVertically)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_skipprevious_outline),
                contentDescription = stringResource(R.string.previous_button)
            )
        }
        //暂停或开始----------------------
        IconButton(
            onClick = {
                if (isPlaying == true) musicPlayerViewModel.pause() else musicPlayerViewModel.play()
            },
            modifier = Modifier
                .size(90.dp)
                .padding(8.dp)
                .align(Alignment.CenterVertically),
            shape = CircleShape
        ) {
            Icon(
                painter = if (isPlaying == true) painterResource(R.drawable.ic_pause)
                else painterResource(R.drawable.ic_play_outline),
                contentDescription = if (controller?.isPlaying == true) stringResource(R.string.pause_button) else stringResource(R.string.play_button)
            )
        }

        //跳到下一首----------------------
        IconButton(
            onClick = {
                musicPlayerViewModel.seekToNext()
            },
            shape = CircleShape,
            modifier = Modifier
                .size(80.dp)
                .padding(8.dp)
                .align(Alignment.CenterVertically)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_skipnext_outline),
                contentDescription = stringResource(R.string.next_button)
            )
        }

        //启用或者禁用随机------------------
        IconButton(
            onClick = {
                musicPlayerViewModel.switchShuffledMode()
            },
            modifier =  Modifier
                .size(80.dp)
                .padding(8.dp)
                .align(Alignment.CenterVertically)
        ) {
            Icon(
                painter = if (isShuffleEnabled == true) painterResource(R.drawable.ic_shuffle)
                else painterResource(R.drawable.ic_shuffle_disabled),
                contentDescription = if (isShuffleEnabled == true) stringResource(R.string.shuffle_enabled_button)
                else stringResource(R.string.shuffle_disabled_button)
            )
        }
        Spacer(Modifier.fillMaxWidth().height(180.dp))
    }
}