package com.nuvio.app.core.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.size.Precision
import coil3.size.Size
import kotlin.math.abs
import kotlin.math.roundToInt
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.home_view_all
import nuvio.composeapp.generated.resources.poster_logo_content_description
import org.jetbrains.compose.resources.stringResource

enum class NuvioPosterShape {
    Poster,
    Square,
    Landscape,
}

enum class NuvioViewAllPillSize {
    Default,
    Compact,
}

@Composable
fun <T> NuvioShelfSection(
    title: String,
    entries: List<T>,
    modifier: Modifier = Modifier,
    headerHorizontalPadding: Dp = 0.dp,
    rowContentPadding: PaddingValues = PaddingValues(0.dp),
    itemSpacing: Dp = 10.dp,
    showHeaderAccent: Boolean = true,
    onViewAllClick: (() -> Unit)? = null,
    viewAllPillSize: NuvioViewAllPillSize = NuvioViewAllPillSize.Default,
    key: ((T) -> Any)? = null,
    itemContent: @Composable (T) -> Unit,
) {
    val rowState = rememberLazyListState()
    val tokens = MaterialTheme.nuvio
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(tokens.spacing.controlGap + NuvioTokens.Space.s2),
    ) {
        if (title.isNotBlank()) {
            NuvioShelfSectionHeader(
                title = title,
                modifier = Modifier.padding(horizontal = headerHorizontalPadding),
                showAccent = showHeaderAccent,
                onViewAllClick = onViewAllClick,
                viewAllPillSize = viewAllPillSize,
            )
        }
        LazyRow(
            state = rowState,
            modifier = Modifier
                .fillMaxWidth()
                .shelfRowMouseDragScroll(rowState),
            contentPadding = rowContentPadding,
            horizontalArrangement = Arrangement.spacedBy(itemSpacing),
        ) {
            if (key != null) {
                items(
                    items = entries.withDuplicateSafeLazyKeys(key),
                    key = { entry -> entry.lazyKey },
                ) { keyedEntry ->
                    itemContent(keyedEntry.value)
                }
            } else {
                items(entries) { entry ->
                    itemContent(entry)
                }
            }
        }
    }
}

private fun Modifier.shelfRowMouseDragScroll(listState: LazyListState): Modifier =
    pointerInput(listState) {
        awaitEachGesture {
            val down = awaitFirstDown(pass = PointerEventPass.Initial)
            if (down.type != PointerType.Mouse) return@awaitEachGesture

            var totalDx = 0f
            var totalDy = 0f
            var dragging = false

            while (true) {
                val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                val change = event.changes.firstOrNull { it.id == down.id } ?: break

                if (!change.pressed) break

                val delta = change.position - change.previousPosition
                totalDx += delta.x
                totalDy += delta.y

                if (!dragging) {
                    val verticalDrag =
                        abs(totalDy) > viewConfiguration.touchSlop && abs(totalDy) > abs(totalDx)
                    val horizontalDrag =
                        abs(totalDx) > viewConfiguration.touchSlop && abs(totalDx) > abs(totalDy)
                    when {
                        verticalDrag -> break
                        horizontalDrag -> dragging = true
                        else -> continue
                    }
                }

                listState.dispatchRawDelta(-delta.x)
                change.consume()
            }
        }
    }

@Composable
fun NuvioPosterCard(
    title: String,
    imageUrl: String?,
    modifier: Modifier = Modifier,
    shape: NuvioPosterShape = NuvioPosterShape.Poster,
    detailLine: String? = null,
    showTitleBelow: Boolean = true,
    bottomLeftLogoUrl: String? = null,
    bottomLeftText: String? = null,
    isWatched: Boolean = false,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
) {
    val posterCardStyle = rememberPosterCardStyleUiState()
    val tokens = MaterialTheme.nuvio
    val cardWidth = shape.cardWidth(basePosterWidthDp = posterCardStyle.widthDp)
    val cardShape = RoundedCornerShape(posterCardStyle.cornerRadiusDp.dp)
    val resolvedImageUrl = remember(imageUrl) { imageUrl?.upgradeTmdbImageQuality() }
    val resolvedBottomLeftLogoUrl = remember(bottomLeftLogoUrl) { bottomLeftLogoUrl?.upgradeTmdbImageQuality() }
    val catalogLogoOverlaySize = catalogLogoOverlaySize(
        basePosterWidthDp = posterCardStyle.widthDp,
        shape = shape,
    )
    val bottomLeftLogoRequest = rememberSizedImageRequest(
        imageUrl = resolvedBottomLeftLogoUrl,
        width = catalogLogoOverlaySize.width,
        height = catalogLogoOverlaySize.height,
        memoryCacheKeyPrefix = "poster-logo",
    )
    val shouldShowTitleBelow = showTitleBelow && !posterCardStyle.hideLabelsEnabled

    Column(
        modifier = modifier.width(cardWidth),
        verticalArrangement = Arrangement.spacedBy(NuvioTokens.Space.s6),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(shape.aspectRatio)
                .clip(cardShape)
                .background(tokens.colors.surface)
                .posterCardClickable(onClick = onClick, onLongClick = onLongClick),
            contentAlignment = Alignment.Center,
        ) {
            if (resolvedImageUrl != null) {
                // The actual draw size of the poster image isn't always
                // `cardWidth`: in a `LazyVerticalGrid` (collection /
                // folder grids) the cell imposes
                // `Constraints.fixedWidth(cellSize)` which can be larger
                // than `cardWidth`, see Compose
                // `LazyGridMeasuredLineProvider.childConstraints`.
                // `BoxWithConstraints` reports the resolved layout
                // dimensions, so the Coil request sizes match the pixels
                // Skia is going to blit. WIC then produces a bitmap at
                // the exact cell size and Skia's draw-time blit is 1:1.
                BoxWithConstraints(modifier = Modifier.matchParentSize()) {
                    PosterCardImage(
                        imageUrl = resolvedImageUrl,
                        title = title,
                        cellWidth = maxWidth,
                        cellHeight = maxHeight,
                    )
                }
            } else {
                Text(
                    text = title,
                    modifier = Modifier.padding(horizontal = NuvioTokens.Space.s14),
                    style = MaterialTheme.typography.titleMedium,
                    color = tokens.colors.textMuted,
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (!resolvedBottomLeftLogoUrl.isNullOrBlank() || !bottomLeftText.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = NuvioTokens.Space.s10, vertical = NuvioTokens.Space.s10),
                ) {
                    if (!resolvedBottomLeftLogoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = bottomLeftLogoRequest ?: resolvedBottomLeftLogoUrl,
                            contentDescription = stringResource(Res.string.poster_logo_content_description, title),
                            modifier = Modifier
                                .width(catalogLogoOverlaySize.width)
                                .height(catalogLogoOverlaySize.height),
                            contentScale = ContentScale.Fit,
                            filterQuality = NuvioImageFilterQuality,
                        )
                    } else {
                        Text(
                            text = bottomLeftText.orEmpty(),
                            style = MaterialTheme.typography.labelMedium,
                            color = tokens.colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = catalogLogoOverlaySize.textMaxWidth),
                        )
                    }
                }
            }

            NuvioPosterWatchedOverlay(isWatched = isWatched)
        }
        if (shouldShowTitleBelow) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = tokens.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!detailLine.isNullOrBlank()) {
                Text(
                    text = detailLine,
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.colors.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            } else {
                Box(modifier = Modifier.height(NuvioTokens.Space.none))
            }
        } else {
            Box(modifier = Modifier.height(NuvioTokens.Space.none))
        }
    }
}

@Composable
private fun PosterCardImage(
    imageUrl: String,
    title: String,
    cellWidth: Dp,
    cellHeight: Dp,
) {
    val platformContext = LocalPlatformContext.current
    val density = LocalDensity.current
    val imageRequest = remember(platformContext, imageUrl, cellWidth, cellHeight, density) {
        val widthPx = with(density) { cellWidth.roundToPx() }.coerceAtLeast(1)
        val heightPx = with(density) { cellHeight.roundToPx() }.coerceAtLeast(1)
        val decodeWidthPx = nuvioQualityDecodeDimensionPx(widthPx)
        val decodeHeightPx = nuvioQualityDecodeDimensionPx(heightPx)
        ImageRequest.Builder(platformContext)
            .data(imageUrl)
            .size(Size(decodeWidthPx, decodeHeightPx))
            .precision(Precision.EXACT)
            .memoryCacheKey("poster-card:$decodeWidthPx:$decodeHeightPx:${imageUrl.hashCode()}")
            .diskCacheKey(imageUrl)
            .build()
    }
    AsyncImage(
        model = imageRequest,
        contentDescription = title,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop,
        filterQuality = NuvioImageFilterQuality,
    )
}

@Composable
private fun NuvioShelfSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    showAccent: Boolean = true,
    onViewAllClick: (() -> Unit)? = null,
    viewAllPillSize: NuvioViewAllPillSize = NuvioViewAllPillSize.Default,
) {
    val tokens = MaterialTheme.nuvio
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = tokens.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (showAccent) {
                Box(
                    modifier = Modifier
                        .padding(top = NuvioTokens.Space.s6)
                        .width(NuvioTokens.Space.s64 - NuvioTokens.Space.s4)
                        .height(NuvioTokens.Space.s4)
                        .background(
                            color = tokens.colors.accent,
                            shape = tokens.shapes.chip,
                    ),
                )
            }
        }
        val viewAllPlaceholderModifier = if (onViewAllClick == null) {
            Modifier
                .alpha(0f)
                .clearAndSetSemantics { }
        } else {
            Modifier
        }
        NuvioViewAllPill(
            onClick = onViewAllClick,
            size = viewAllPillSize,
            modifier = viewAllPlaceholderModifier,
        )
    }
}

@Composable
private fun NuvioViewAllPill(
    onClick: (() -> Unit)?,
    size: NuvioViewAllPillSize,
    modifier: Modifier = Modifier,
) {
    val tokens = MaterialTheme.nuvio
    val colorScheme = MaterialTheme.colorScheme
    val isAmoled = colorScheme.background == androidx.compose.ui.graphics.Color.Black && colorScheme.surface == androidx.compose.ui.graphics.Color(0xFF050505)
    val horizontalPadding = if (size == NuvioViewAllPillSize.Compact) NuvioTokens.Space.s12 else NuvioTokens.Space.s18
    val verticalPadding = if (size == NuvioViewAllPillSize.Compact) NuvioTokens.Space.s8 + NuvioTokens.Space.s1 else NuvioTokens.Space.s14
    val textStyle = if (size == NuvioViewAllPillSize.Compact) {
        MaterialTheme.typography.labelLarge
    } else {
        MaterialTheme.typography.titleMedium
    }
    val iconSpacing = if (size == NuvioViewAllPillSize.Compact) NuvioTokens.Space.s2 else NuvioTokens.Space.s4

    Row(
        modifier = modifier
            .background(
                color = if (isAmoled) androidx.compose.ui.graphics.Color(0xFF0D0D0D)  else tokens.colors.surface,
                shape = RoundedCornerShape(NuvioTokens.Radius.xl),
            )
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        horizontalArrangement = Arrangement.spacedBy(iconSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.home_view_all),
            style = textStyle,
            color = tokens.colors.textPrimary,
        )
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = tokens.colors.textMuted,
            modifier = Modifier.height(if (size == NuvioViewAllPillSize.Compact) NuvioTokens.Icon.sm else tokens.icons.md),
        )
    }
}

private val NuvioPosterShape.aspectRatio: Float
    get() = when (this) {
        NuvioPosterShape.Poster -> 0.675f
        NuvioPosterShape.Square -> 1f
        NuvioPosterShape.Landscape -> PosterLandscapeAspectRatio
    }

private data class CatalogLogoOverlaySize(
    val width: Dp,
    val height: Dp,
    val textMaxWidth: Dp,
)

private fun catalogLogoOverlaySize(
    basePosterWidthDp: Int,
    shape: NuvioPosterShape,
): CatalogLogoOverlaySize {
    fun scaledDp(value: Float, min: Int, max: Int): Dp =
        value.roundToInt().coerceIn(min, max).dp

    return if (shape == NuvioPosterShape.Landscape) {
        val landscapeWidth = landscapePosterWidth(basePosterWidthDp).value
        val landscapeHeight = landscapeWidth / PosterLandscapeAspectRatio
        CatalogLogoOverlaySize(
            width = scaledDp(landscapeWidth * 0.58f, min = 112, max = 300),
            height = scaledDp(landscapeHeight * 0.46f, min = 42, max = 110),
            textMaxWidth = scaledDp(landscapeWidth * 0.72f, min = 140, max = 340),
        )
    } else {
        val logoWidth = scaledDp(basePosterWidthDp * 0.68f, min = 72, max = 140)
        CatalogLogoOverlaySize(
            width = logoWidth,
            height = scaledDp(logoWidth.value * 0.25f, min = 18, max = 36),
            textMaxWidth = scaledDp(basePosterWidthDp * 0.86f, min = 92, max = 170),
        )
    }
}

private fun NuvioPosterShape.cardWidth(basePosterWidthDp: Int): Dp =
    when (this) {
        NuvioPosterShape.Poster -> basePosterWidthDp.dp
        NuvioPosterShape.Square -> basePosterWidthDp.dp
        NuvioPosterShape.Landscape -> landscapePosterWidth(basePosterWidthDp)
    }

@OptIn(ExperimentalFoundationApi::class)
internal fun Modifier.posterCardClickable(
    onClick: (() -> Unit)?,
    onLongClick: (() -> Unit)?,
): Modifier {
    val withPrimaryGestures =
        if (onClick != null || onLongClick != null) {
            this.combinedClickable(
                onClick = { onClick?.invoke() },
                onLongClick = onLongClick,
            )
        } else {
            this
        }
    return withPrimaryGestures.desktopContextMenuPointer(onLongClick)
}

internal expect fun String.upgradeTmdbImageQuality(): String
