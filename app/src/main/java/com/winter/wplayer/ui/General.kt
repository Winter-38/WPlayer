package com.winter.wplayer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.winter.wplayer.R
import com.winter.wplayer.room.Song

@Composable
fun SongItems(songs: List<Song>, modifier: Modifier = Modifier, action: (Int, Song) -> Unit) {
    LazyColumn(
        modifier = Modifier.background(
            color = MaterialTheme.colorScheme.surface
        )
    ) {
        itemsIndexed(songs) { index, song ->
            Row(
                modifier = Modifier
                    .height(60.dp)
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable {
                        action(index, song)
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