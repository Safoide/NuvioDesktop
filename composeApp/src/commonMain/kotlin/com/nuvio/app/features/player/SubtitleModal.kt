package com.nuvio.app.features.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.compose_player_built_in
import nuvio.composeapp.generated.resources.compose_player_fetch_subtitles
import nuvio.composeapp.generated.resources.compose_player_none
import nuvio.composeapp.generated.resources.compose_player_style
import nuvio.composeapp.generated.resources.compose_player_subtitles
import org.jetbrains.compose.resources.stringResource

// Internal language bucket used only inside the Subtitles panel
private data class LanguageBucket(
    val code: String,       // e.g. "en", "es", "__none__"
    val label: String,      // display label
    val builtIn: List<SubtitleTrack>,
    val addons: List<AddonSubtitle>,
)

private const val LANG_NONE = "__none__"

@Composable
fun SubtitleModal(
    visible: Boolean,
    activeTab: SubtitleTab,
    subtitleTracks: List<SubtitleTrack>,
    selectedSubtitleIndex: Int,
    addonSubtitles: List<AddonSubtitle>,
    selectedAddonSubtitleId: String?,
    isLoadingAddonSubtitles: Boolean,
    subtitleStyle: SubtitleStyleState,
    subtitleDelayMs: Int,
    preferredSubtitleLanguage: String,
    secondaryPreferredSubtitleLanguage: String?,
    showOnlyPreferredLanguages: Boolean,
    selectedAddonSubtitle: AddonSubtitle?,
    subtitleAutoSyncState: SubtitleAutoSyncUiState,
    onTabSelected: (SubtitleTab) -> Unit,
    onBuiltInTrackSelected: (Int) -> Unit,
    onAddonSubtitleSelected: (AddonSubtitle) -> Unit,
    onFetchAddonSubtitles: () -> Unit,
    onStyleChanged: (SubtitleStyleState) -> Unit,
    onSubtitleDelayChanged: (Int) -> Unit,
    onSubtitleDelayReset: () -> Unit,
    onAutoSyncCapture: () -> Unit,
    onAutoSyncCueSelected: (SubtitleSyncCue) -> Unit,
    onAutoSyncReload: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(200)),
    ) {
        BoxWithConstraints(
            modifier = modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onDismiss,
                )
                .background(colorScheme.scrim.copy(alpha = 0.56f)),
            contentAlignment = Alignment.Center,
        ) {
            val maxH = maxHeight
            val isCompact = maxWidth < 360.dp || maxHeight < 640.dp

            // Wider modal when showing the Subtitles tab
            val modalMaxWidth = 600.dp

            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(tween(300)) { it / 3 } + fadeIn(tween(300)),
                exit = slideOutVertically(tween(250)) { it / 3 } + fadeOut(tween(250)),
            ) {
                Box(
                    modifier = Modifier
                        .widthIn(max = modalMaxWidth)
                        .fillMaxWidth(0.92f)
                        .heightIn(max = maxH * 0.95f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(colorScheme.surface)
                        .border(
                            1.dp,
                            colorScheme.outlineVariant.copy(alpha = 0.8f),
                            RoundedCornerShape(24.dp),
                        )
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = {},
                        ),
                ) {
                    Column {
                        // Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(Res.string.compose_player_subtitles),
                                color = colorScheme.onSurface,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }

                        SubtitleTabBar(
                            activeTab = activeTab,
                            onTabSelected = onTabSelected,
                        )

                        when (activeTab) {
                            SubtitleTab.Subtitles -> MergedSubtitlesPanel(
                                subtitleTracks = subtitleTracks,
                                selectedSubtitleIndex = selectedSubtitleIndex,
                                addonSubtitles = addonSubtitles,
                                selectedAddonSubtitleId = selectedAddonSubtitleId,
                                isLoadingAddonSubtitles = isLoadingAddonSubtitles,
                                preferredSubtitleLanguage = preferredSubtitleLanguage,
                                secondaryPreferredSubtitleLanguage = secondaryPreferredSubtitleLanguage,
                                showOnlyPreferredLanguages = showOnlyPreferredLanguages,
                                onBuiltInTrackSelected = onBuiltInTrackSelected,
                                onAddonSubtitleSelected = onAddonSubtitleSelected,
                                onFetchAddonSubtitles = onFetchAddonSubtitles,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f, fill = false),
                            )

                            SubtitleTab.Style -> Column(
                                modifier = Modifier
                                    .verticalScroll(rememberScrollState())
                                    .padding(horizontal = 20.dp)
                                    .padding(bottom = 20.dp),
                            ) {
                                SubtitleStylePanel(
                                    style = subtitleStyle,
                                    subtitleDelayMs = subtitleDelayMs,
                                    selectedAddonSubtitle = selectedAddonSubtitle,
                                    subtitleAutoSyncState = subtitleAutoSyncState,
                                    isCompact = isCompact,
                                    onStyleChanged = onStyleChanged,
                                    onSubtitleDelayChanged = onSubtitleDelayChanged,
                                    onSubtitleDelayReset = onSubtitleDelayReset,
                                    onAutoSyncCapture = onAutoSyncCapture,
                                    onAutoSyncCueSelected = onAutoSyncCueSelected,
                                    onAutoSyncReload = onAutoSyncReload,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Tab bar — now only 2 tabs
// ---------------------------------------------------------------------------

@Composable
private fun SubtitleTabBar(
    activeTab: SubtitleTab,
    onTabSelected: (SubtitleTab) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 70.dp)
            .padding(bottom = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SubtitleTab.entries.forEach { tab ->
            val isSelected = tab == activeTab
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) colorScheme.primaryContainer
                else colorScheme.surfaceVariant.copy(alpha = 0.92f),
                animationSpec = tween(250),
            )
            val radius by animateDpAsState(
                targetValue = if (isSelected) 10.dp else 40.dp,
                animationSpec = tween(250),
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(radius))
                    .background(bgColor)
                    .clickable { onTabSelected(tab) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = when (tab) {
                        SubtitleTab.Subtitles -> stringResource(Res.string.compose_player_subtitles)
                        SubtitleTab.Style -> stringResource(Res.string.compose_player_style)
                    },
                    color = if (isSelected) colorScheme.onPrimaryContainer
                    else colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Merged subtitles panel — language sidebar + subtitle list
// ---------------------------------------------------------------------------

@Composable
private fun MergedSubtitlesPanel(
    subtitleTracks: List<SubtitleTrack>,
    selectedSubtitleIndex: Int,
    addonSubtitles: List<AddonSubtitle>,
    selectedAddonSubtitleId: String?,
    isLoadingAddonSubtitles: Boolean,
    preferredSubtitleLanguage: String,
    secondaryPreferredSubtitleLanguage: String?,
    showOnlyPreferredLanguages: Boolean,
    onBuiltInTrackSelected: (Int) -> Unit,
    onAddonSubtitleSelected: (AddonSubtitle) -> Unit,
    onFetchAddonSubtitles: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme

    // Collect all distinct language codes so we can resolve their labels
    // inside this @Composable context (languageLabelForCode requires it).
    val filteredTracks = remember(subtitleTracks, showOnlyPreferredLanguages, preferredSubtitleLanguage, secondaryPreferredSubtitleLanguage) {
        if (!showOnlyPreferredLanguages) {
            subtitleTracks
        } else {
            val targets = listOfNotNull(
                preferredSubtitleLanguage.takeIf {
                    !it.equals(SubtitleLanguageOption.NONE, ignoreCase = true) &&
                            !it.equals(SubtitleLanguageOption.FORCED, ignoreCase = true)
                },
                secondaryPreferredSubtitleLanguage?.takeIf { it.isNotBlank() },
            )
            if (targets.isEmpty()) subtitleTracks
            else subtitleTracks.filter { track ->
                targets.any { target -> languageMatchesPreference(track.language, target) }
            }
        }
    }

    val langCodes = remember(filteredTracks, addonSubtitles) {
        linkedSetOf<String>().also { set ->
            subtitleTracks.forEach { set += it.language ?: "" }
            addonSubtitles.forEach { set += it.language ?: "" }
        }.filter { it.isNotBlank() }
    }
    // Resolve every label here while we're still @Composable.
    val labelMap: Map<String, String> = langCodes.associate { code ->
        code to languageLabelForCode(code)
    }

    // Build language buckets (pure function — no @Composable needed).
    val buckets = remember(filteredTracks, addonSubtitles, labelMap) {
        buildLanguageBuckets(filteredTracks, addonSubtitles, labelMap)
    }

    // Determine which language is currently active so we can pre-select it
    val activeLang = remember(
        selectedSubtitleIndex,
        selectedAddonSubtitleId,
        subtitleTracks,
        addonSubtitles,
    ) {
        when {
            selectedAddonSubtitleId != null ->
                addonSubtitles.firstOrNull { it.id == selectedAddonSubtitleId }?.language
                    ?: buckets.firstOrNull()?.code ?: ""
            selectedSubtitleIndex != -1 ->
                subtitleTracks.firstOrNull { it.index == selectedSubtitleIndex }?.language
                    ?: buckets.firstOrNull()?.code ?: ""
            else ->
                buckets.firstOrNull()?.code ?: ""
        }
    }

    var selectedLang by remember(activeLang) { mutableStateOf(activeLang) }

    val currentBucket = buckets.firstOrNull { it.code == selectedLang } ?: buckets.firstOrNull()

    Row(
        modifier = modifier.heightIn(min = 200.dp, max = 420.dp),
    ) {
        // ── Language sidebar ──────────────────────────────────────────────
        Column(
            modifier = Modifier
                .width(108.dp)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            buckets.forEach { bucket ->
                val isActive = bucket.code == selectedLang
                val bgColor by animateColorAsState(
                    targetValue = if (isActive) colorScheme.primaryContainer
                    else colorScheme.surfaceVariant.copy(alpha = 0f),
                    animationSpec = tween(200),
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(bgColor)
                        .clickable { selectedLang = bucket.code }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = bucket.label,
                        color = if (isActive) colorScheme.onPrimaryContainer
                        else colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 2,
                    )
                }
            }
        }

        // Vertical divider
        Divider(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
                .padding(vertical = 8.dp),
            color = colorScheme.outlineVariant.copy(alpha = 0.5f),
        )

        // ── Subtitle list for selected language ───────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(start = 8.dp, end = 12.dp, top = 4.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (currentBucket != null) {
                val isNoneSelected = selectedSubtitleIndex == -1 && selectedAddonSubtitleId == null
                SubtitleRow(
                    title = stringResource(Res.string.compose_player_none),
                    subtitle = null,
                    isSelected = isNoneSelected,
                    onClick = { onBuiltInTrackSelected(-1) },
                )

                // Built-in tracks first
                currentBucket.builtIn.forEach { track ->
                    val isSelected = track.index == selectedSubtitleIndex
                    SubtitleRow(
                        title = localizedTrackDisplayName(track.label, track.language, track.index),
                        subtitle = stringResource(Res.string.compose_player_built_in),
                        isSelected = isSelected,
                        onClick = { onBuiltInTrackSelected(track.index) },
                    )
                }

                // Addon subtitles — or fetch prompt
                if (isLoadingAddonSubtitles) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            color = colorScheme.primary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                } else if (currentBucket.addons.isEmpty() && currentBucket.builtIn.isEmpty()) {
                    // Bucket only exists because addons are not loaded yet
                    FetchSubtitlesPrompt(onFetch = onFetchAddonSubtitles)
                } else {
                    currentBucket.addons.forEach { sub ->
                        val isSelected = sub.id == selectedAddonSubtitleId
                        SubtitleRow(
                            title = sub.display,
                            subtitle = null,
                            isSelected = isSelected,
                            onClick = { onAddonSubtitleSelected(sub) },
                        )
                    }

                    // If there are no addons for this language yet, offer to fetch
                    if (currentBucket.addons.isEmpty()) {
                        FetchSubtitlesPrompt(onFetch = onFetchAddonSubtitles)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Shared row composable
// ---------------------------------------------------------------------------

@Composable
private fun SubtitleRow(
    title: String,
    subtitle: String?,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) colorScheme.primaryContainer
                else colorScheme.surfaceVariant.copy(alpha = 0.6f),
            )
            .clickable(onClick = onClick)
            .padding(vertical = if (subtitle != null) 8.dp else 10.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = if (isSelected) colorScheme.onPrimaryContainer else colorScheme.onSurface,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = if (isSelected) colorScheme.onPrimaryContainer.copy(alpha = 0.65f)
                    else colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        if (isSelected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = colorScheme.primary,
                modifier = Modifier
                    .padding(start = 6.dp)
                    .size(18.dp),
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Fetch prompt
// ---------------------------------------------------------------------------

@Composable
private fun FetchSubtitlesPrompt(onFetch: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onFetch)
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Rounded.CloudDownload,
                contentDescription = null,
                tint = colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp),
            )
            Text(
                text = stringResource(Res.string.compose_player_fetch_subtitles),
                color = colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Bucket builder — pure function, labels are pre-resolved by the @Composable caller
// ---------------------------------------------------------------------------

private fun buildLanguageBuckets(
    tracks: List<SubtitleTrack>,
    addons: List<AddonSubtitle>,
    labelMap: Map<String, String>,
): List<LanguageBucket> {
    val langCodes = linkedSetOf<String>()
    tracks.forEach { if (!it.language.isNullOrBlank()) langCodes += it.language!! }
    addons.forEach { if (!it.language.isNullOrBlank()) langCodes += it.language!! }

    val buckets = mutableListOf<LanguageBucket>()

    langCodes.forEach { code ->
        buckets += LanguageBucket(
            code = code,
            label = labelMap[code] ?: code,
            builtIn = tracks.filter { (it.language ?: "") == code },
            addons = addons.filter { (it.language ?: "") == code },
        )
    }

    // Tracks/addons without a language code go into a catch-all bucket
    val unknownTracks = tracks.filter { it.language.isNullOrBlank() }
    val unknownAddons = addons.filter { it.language.isNullOrBlank() }
    if (unknownTracks.isNotEmpty() || unknownAddons.isNotEmpty()) {
        buckets += LanguageBucket(
            code = "__unknown__",
            label = "?",
            builtIn = unknownTracks,
            addons = unknownAddons,
        )
    }

    return buckets
}