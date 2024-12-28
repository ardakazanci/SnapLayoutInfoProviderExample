package com.ardakazanci.snaplayoutinfoproviderexample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ardakazanci.snaplayoutinfoproviderexample.ui.theme.SnapLayoutInfoProviderExampleTheme
import kotlin.math.absoluteValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SnapLayoutInfoProviderExampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SnapAlignedLazyRow(
                        modifier = Modifier.padding(innerPadding),
                        List(10) { index ->
                            Item(id = index, title = "Item $index")
                        }
                    )
                }
            }
        }
    }
}

data class Item(val id: Int, val title: String)


@Composable
fun SnapAlignedLazyRow(modifier: Modifier = Modifier, items: List<Item>) {
    val itemWidth = 200.dp
    val lazyListState = rememberLazyListState()
    val itemWidthPx = with(LocalDensity.current) { itemWidth.toPx() }
    val flingBehavior = rememberSnapFlingBehavior(
        snapLayoutInfoProvider = rememberSnapLayoutInfoProvider(itemWidthPx, lazyListState)
    )

    LazyRow(
        modifier = modifier,
        state = lazyListState,
        flingBehavior = flingBehavior
    ) {
        items(items.size) { index ->
            val offset by remember {
                derivedStateOf {
                    calculateItemOffset(
                        index,
                        lazyListState,
                        itemWidthPx
                    )
                }
            }
            val scale = 1f + (0.1f * (1f - offset.coerceIn(0f, 1f)))

            Card(
                modifier = Modifier
                    .width(itemWidth)
                    .padding(8.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        alpha = scale
                    },
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.elevatedCardElevation(4.dp)
            ) {
                Column {
                    Image(
                        painter = painterResource(id = R.drawable.image_5),
                        contentDescription = "Card Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp
                                )
                            ),
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        text = items.getOrNull(index)?.title ?: "Unknown Item",
                        modifier = Modifier.padding(16.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
fun rememberSnapLayoutInfoProvider(
    itemWidthPx: Float,
    state: LazyListState
): SnapLayoutInfoProvider {
    return remember(state) {
        object : SnapLayoutInfoProvider {
            override fun calculateApproachOffset(velocity: Float, decayOffset: Float): Float {
                return decayOffset
            }

            override fun calculateSnapOffset(velocity: Float): Float {
                val direction = if (velocity > 0) 1 else -1
                val snappedOffset = itemWidthPx * direction
                val additionalOffset = snappedOffset % itemWidthPx
                return snappedOffset - additionalOffset
            }
        }
    }
}

fun calculateItemOffset(index: Int, state: LazyListState, itemWidthPx: Float): Float {
    val visibleItems = state.layoutInfo.visibleItemsInfo
    val currentItem = visibleItems.find { it.index == index }
    return if (currentItem != null) {
        val center = currentItem.offset + (itemWidthPx / 2)
        val viewportCenter = state.layoutInfo.viewportEndOffset / 2f
        val distance = (viewportCenter - center).absoluteValue
        (distance / viewportCenter).coerceIn(0f, 1f)
    } else {
        1f
    }
}



