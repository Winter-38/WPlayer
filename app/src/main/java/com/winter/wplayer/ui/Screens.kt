package com.winter.wplayer.ui

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFrom
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
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
    val isPlaying by musicPlayerViewModel.isPlaying.collectAsStateWithLifecycle()
    //播放列表&歌曲索引
    val songs by musicListViewModel.songLists.collectAsStateWithLifecycle()
    val songIndex by IndexHolder.songIndex.collectAsStateWithLifecycle()
    //歌曲封面&标题
    val coverUri = songs.getOrNull(songIndex)?.coverUri ?: R.drawable.ic_play_outline
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                //.border(2.dp, Color.Blue)
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
                    contentDescription = "Back to home screen"
                )
            }
            //封面--------------------------------
            AsyncImage(
                model = coverUri,
                contentDescription = "Big song cover",
                modifier = Modifier
                    .size(300.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .align(Alignment.BottomCenter),
                contentScale = ContentScale.Crop
            )
        }

        //无无无无无无-------------------------------
        Spacer(modifier = Modifier.weight(0.1f))

        Column(modifier = Modifier
            .fillMaxWidth()
            .weight(0.8f)
            //.border(2.dp, Color.Blue)
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
                    .weight(1f),
                    //.border(2.dp, Color.Blue),
                fontSize = 25.sp,
                textAlign = TextAlign.Center,
                text = songs.getOrNull(songIndex)?.title ?: "Unknown title"
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
                text = (songs.getOrNull(songIndex)?.artist ?: "Unknown artist")
            )

            //按钮组-------------------------------------------------------------------------------------按钮组

            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(4f),
                    //.border(width = 2.dp, color = Color.Blue),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Top
            ) {
                //跳到上一首----------------------
                Button(
                    onClick = {
                        controller?.apply {
                            if (hasPreviousMediaItem()) seekToPrevious()
                        }
                    },
                    shape = CircleShape,
                    modifier = Modifier
                        .size(80.dp)
                        .padding(8.dp)
                        .align(Alignment.CenterVertically),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_skipprevious_outline),
                        contentDescription = "Skip to previous song"
                    )
                }
                //暂停或开始----------------------
                IconButton(
                    onClick = {
                        controller?.apply {
                            if (this.isPlaying) pause() else play()
                            Log.d("test", "${isPlaying}")
                        }
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
                        contentDescription = if (controller?.isPlaying == true) "Pause Button" else "Play Button"
                    )
                }

                //跳到下一首----------------------
                Button(
                    onClick = {
                        controller?.apply {
                            if (hasNextMediaItem()) seekToNext()
                        }
                    },
                    shape = CircleShape,
                    modifier = Modifier
                        .size(80.dp)
                        .padding(8.dp)
                        .align(Alignment.CenterVertically),
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