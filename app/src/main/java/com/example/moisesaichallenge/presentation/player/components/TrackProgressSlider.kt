package com.example.moisesaichallenge.presentation.player.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.example.moisesaichallenge.ui.theme.MoisesaiChallengeTheme
import com.example.moisesaichallenge.ui.theme.White25
import com.example.moisesaichallenge.ui.theme.White60

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackProgressSlider(
    positionMs: Long,
    durationMs: Long,
    onSeekTo: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Slider(
            value = if (durationMs > 0) positionMs.toFloat() / durationMs else 0f,
            onValueChange = { fraction -> onSeekTo((fraction * durationMs).toLong()) },
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                activeTrackColor = White60,
                inactiveTrackColor = White25,
                thumbColor = Color.White
            ),
            thumb = {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .graphicsLayer(transformOrigin = TransformOrigin.Center)
                )
            },
            track = { sliderState ->
                val fraction = (sliderState.value - sliderState.valueRange.start) /
                        (sliderState.valueRange.endInclusive - sliderState.valueRange.start)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(White25, shape = CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction)
                            .height(4.dp)
                            .background(White60, shape = CircleShape)
                    )
                }
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatDuration(positionMs),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "-${formatDuration((durationMs - positionMs).coerceAtLeast(0))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

internal fun formatDuration(ms: Long): String {
    val total = ms / 1000
    return "%d:%02d".format(total / 60, total % 60)
}

// region Previews

@PreviewLightDark
@Composable
private fun TrackProgressSliderMidwayPreview() {
    MoisesaiChallengeTheme {
        Surface {
            TrackProgressSlider(
                positionMs = 86_000L,
                durationMs = 354_000L,
                onSeekTo = {},
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun TrackProgressSliderStartPreview() {
    MoisesaiChallengeTheme {
        Surface {
            TrackProgressSlider(
                positionMs = 0L,
                durationMs = 354_000L,
                onSeekTo = {},
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}
