package com.winter.wplayer.ui

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
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
    val musicPlayerViewModel: MusicPlayerViewModel = viewModel(viewModelStoreOwner = activity)

    //播放器操控
    val controller by musicPlayerViewModel.controller.collectAsStateWithLifecycle()
    //播放列表&歌曲索引
    val songs by musicListViewModel.songLists.collectAsStateWithLifecycle()
    val songIndex by IndexHolder.songIndex.collectAsStateWithLifecycle()
    //歌曲封面&标题
    val coverUri = songs.getOrNull(songIndex)?.coverUri ?: R.drawable.ic_play_outline
    val title = (songs.getOrNull(songIndex)?.title ?: "Unknown title") + " - " + (songs.getOrNull(songIndex)?.artist ?: "Unknown artist")

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.weight(1.5f).fillMaxWidth()
        ) {
            Button(
                onClick = { onNavToDetail() },
                shape = CircleShape,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 30.dp).size(60.dp),
                contentPadding = PaddingValues(6.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_left),
                    contentDescription = "Back to home screen"
                )
            }
            AsyncImage(
                model = coverUri,
                contentDescription = "Big song cover",
                modifier = Modifier
                    .size(300.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .align(Alignment.Center),
                contentScale = ContentScale.Crop
            )
        }

        Column (modifier = Modifier.weight(1f)) {
            Text(
                maxLines = 1,
                modifier = Modifier
                    .basicMarquee(
                        iterations = Int.MAX_VALUE,
                        repeatDelayMillis = 500,
                        initialDelayMillis = 500,
                        velocity = 30.dp
                    )
                    .weight(1f),
                fontSize = 25.sp,
                text = title
            )

            Spacer(modifier = Modifier.weight(0.1f))


            Row (
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .weight(2.5f)
            ){
                //跳到上一首
                Button(
                    onClick = {
                        controller?.apply {
                            if (hasPreviousMediaItem()) seekToPrevious()
                        }
                    },
                    shape = CircleShape,
                    modifier = Modifier.size(80.dp).padding(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_skipprevious_outline),
                        contentDescription = "Skip to previous song"
                    )
                }

                Button(
                    onClick = {
                        controller?.apply {
                            if (isPlaying) pause()
                            else play()
                        }
                        Log.d("play-screen", "controller ${controller.toString()}")
                    },
                    modifier = Modifier.size(80.dp).padding(8.dp),
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
                    modifier = Modifier.size(80.dp).padding(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_skipnext_outline),
                        contentDescription = "Skip to next song"
                    )
                }
            }
        }
    }
}