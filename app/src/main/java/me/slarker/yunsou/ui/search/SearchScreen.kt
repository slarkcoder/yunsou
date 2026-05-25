package me.slarker.yunsou.ui.search

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.sqrt
import me.slarker.yunsou.data.model.CloudType
import me.slarker.yunsou.data.model.MergedGroup
import me.slarker.yunsou.ui.search.components.ResourceCard
import me.slarker.yunsou.ui.search.components.SearchBar
import me.slarker.yunsou.ui.settings.SettingsScreen
import me.slarker.yunsou.ui.theme.PanSouTheme

@Composable
fun SearchScreen(
    uiState: SearchUiState,
    onQueryChange: (String) -> Unit,
    onCloudTypeToggle: (CloudType) -> Unit,
    onSearch: () -> Unit,
    onTabChange: (Int) -> Unit,
    onBaseUrlChange: (String) -> Unit,
    onSearchHistoryItemClick: (String) -> Unit,
    onClearCache: () -> Unit,
    onCheckServerStatus: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isDarkTheme by remember { mutableStateOf(true) }
    var revealCenter by remember { mutableStateOf<Offset?>(null) }
    var isRevealing by remember { mutableStateOf(false) }
    var darkBg by remember { mutableStateOf(Color(0xFF121212)) }
    var lightBg by remember { mutableStateOf(Color.White) }
    var overlayColor by remember { mutableStateOf(Color(0xFF121212)) }
    val revealProgress = remember { Animatable(0f) }

    // 切换到设置页时自动检测服务器状态
    LaunchedEffect(uiState.currentTab) {
        if (uiState.currentTab == 1) {
            onCheckServerStatus()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PanSouTheme(darkTheme = isDarkTheme) {
            val currentBg = MaterialTheme.colorScheme.background
            if (!isRevealing) {
                if (isDarkTheme) darkBg = currentBg else lightBg = currentBg
                overlayColor = currentBg
            }
            Scaffold(
                topBar = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 4.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (uiState.currentTab == 0) "云搜" else "设置",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(
                            onClick = {
                                if (isRevealing) return@IconButton
                                isRevealing = true
                                overlayColor = if (isDarkTheme) darkBg else lightBg
                                isDarkTheme = !isDarkTheme
                                scope.launch {
                                    revealProgress.snapTo(0f)
                                    revealProgress.animateTo(
                                        1f,
                                        animationSpec = tween(500, easing = FastOutSlowInEasing)
                                    )
                                    isRevealing = false
                                }
                            },
                            modifier = Modifier.onGloballyPositioned { coords ->
                                val pos = coords.positionInWindow()
                                val size = coords.size
                                revealCenter = Offset(
                                    pos.x + size.width / 2f,
                                    pos.y + size.height / 2f
                                )
                            }
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "切换主题"
                            )
                        }
                    }
                },
                bottomBar = {
                    NavigationBar {
                        NavigationBarItem(
                            selected = uiState.currentTab == 0,
                            onClick = { onTabChange(0) },
                            icon = { Icon(Icons.Default.Search, contentDescription = null) },
                            label = { Text("搜索") }
                        )
                        NavigationBarItem(
                            selected = uiState.currentTab == 1,
                            onClick = { onTabChange(1) },
                            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                            label = { Text("设置") }
                        )
                    }
                }
            ) { paddingValues ->
                when (uiState.currentTab) {
                    0 -> SearchTab(
                        uiState = uiState,
                        context = context,
                        onQueryChange = onQueryChange,
                        onSearch = onSearch,
                        onHistoryClick = onSearchHistoryItemClick,
                        modifier = Modifier.padding(paddingValues)
                    )
                    1 -> SettingsTab(
                        selectedTypes = uiState.selectedCloudTypes,
                        onToggle = onCloudTypeToggle,
                        baseUrl = uiState.baseUrl,
                        onBaseUrlChange = onBaseUrlChange,
                        onClearCache = onClearCache,
                        serverStatus = uiState.serverStatus,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }

        // 水滴扩散动画：覆盖层用旧背景色，圆形洞口逐渐扩大露出新主题
        if (isRevealing && revealCenter != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        val cx = revealCenter!!.x
                        val cy = revealCenter!!.y
                        val w = size.width
                        val h = size.height
                        val maxRadius = maxOf(
                            sqrt(cx * cx + cy * cy),
                            sqrt((w - cx) * (w - cx) + cy * cy),
                            sqrt(cx * cx + (h - cy) * (h - cy)),
                            sqrt((w - cx) * (w - cx) + (h - cy) * (h - cy))
                        )
                        val r = revealProgress.value * maxRadius
                        val circlePath = Path().apply {
                            addOval(Rect(cx - r, cy - r, cx + r, cy + r))
                        }
                        clipPath(circlePath, clipOp = ClipOp.Difference) {
                            drawRect(overlayColor)
                        }
                    }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchTab(
    uiState: SearchUiState,
    context: Context,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onHistoryClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedSegmentIndex = remember { mutableStateOf(0) }
    val density = LocalDensity.current
    var searchBarHeightDp by remember { mutableStateOf(0.dp) }
    var historyHeightDp by remember { mutableStateOf(0.dp) }
    val focusRequester = remember { FocusRequester() }
    var isSearchFocused by remember { mutableStateOf(false) }

    val historyVisible = isSearchFocused && uiState.query.isBlank() && uiState.searchHistory.isNotEmpty()
    val historyExtra = if (historyVisible) historyHeightDp + 8.dp else 0.dp

    LaunchedEffect(Unit) {
        if (!uiState.hasSearched && uiState.query.isBlank()) {
            focusRequester.requestFocus()
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val maxH = constraints.maxHeight / density.density
        val groupHeightDp = searchBarHeightDp + historyExtra
        val spacerTarget = if (uiState.hasSearched) 0.dp
        else ((maxH - groupHeightDp.value) / 2f).coerceAtLeast(0f).dp
        val topSpacer by animateDpAsState(
            targetValue = spacerTarget,
            animationSpec = tween(500, easing = FastOutSlowInEasing),
            label = "searchBarPosition"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(topSpacer))

            SearchBar(
                query = uiState.query,
                onQueryChange = onQueryChange,
                onSearch = onSearch,
                focusRequester = focusRequester,
                onFocusChange = { isSearchFocused = it },
                modifier = Modifier.onSizeChanged { size ->
                    searchBarHeightDp = with(density) { size.height.toDp() }
                }
            )

            AnimatedVisibility(
                visible = historyVisible,
                enter = fadeIn(animationSpec = tween(200)),
                exit = fadeOut(animationSpec = tween(200))
            ) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onSizeChanged { size ->
                            historyHeightDp = with(density) { size.height.toDp() }
                        }
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.searchHistory.forEach { keyword ->
                        FilterChip(
                            selected = false,
                            onClick = { onHistoryClick(keyword) },
                            label = { Text(keyword) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }

            if (uiState.hasSearched) {
                Spacer(modifier = Modifier.height(8.dp))

            if (uiState.isChecking) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(4.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    uiState.error != null -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.ErrorOutline,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = uiState.error,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = onSearch) { Text("重试") }
                        }
                    }

                    uiState.hasSearched && uiState.mergedGroups.isEmpty() && !uiState.isChecking -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "没有找到有效资源",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    uiState.mergedGroups.isNotEmpty() -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            if (uiState.mergedGroups.size > 1) {
                                SegmentTabs(
                                    groups = uiState.mergedGroups,
                                    selectedIndex = selectedSegmentIndex.value,
                                    onSelect = { selectedSegmentIndex.value = it },
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                            val displayGroups = if (uiState.mergedGroups.size > 1) {
                                val idx = selectedSegmentIndex.value.coerceIn(0, uiState.mergedGroups.lastIndex)
                                listOf(uiState.mergedGroups[idx])
                            } else {
                                uiState.mergedGroups
                            }
                            val displayTotal = displayGroups.sumOf { it.items.size }
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                item {
                                    Row(
                                        modifier = Modifier.padding(bottom = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "共 ${displayTotal} 条有效结果",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                        if (!uiState.isChecking) {
                                            Text(
                                                text = "，已自动移除无效链接",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }
                                }
                                displayGroups.flatMap { group ->
                                    group.items.map { it to group.cloudType }
                                }.forEach { (item, cloudType) ->
                                    item {
                                        ResourceCard(
                                            item = item,
                                            linkState = uiState.linkCheckResults[item.url],
                                            onClick = {
                                                val uri = Uri.parse(item.url)
                                                val pm = context.packageManager
                                                // 1. Try cloud app that can handle the URL directly
                                                val deepLinkIntent = cloudType.packageNames
                                                    .firstNotNullOfOrNull { pkg ->
                                                        Intent(Intent.ACTION_VIEW, uri).apply { setPackage(pkg) }
                                                            .takeIf { it.resolveActivity(pm) != null }
                                                    }
                                                // 2. Fall back to launching the cloud app's main activity
                                                val launchIntent = cloudType.packageNames
                                                    .firstNotNullOfOrNull { pkg ->
                                                        pm.getLaunchIntentForPackage(pkg)
                                                    }
                                                // 3. Browser fallback
                                                val browserIntent = Intent(Intent.ACTION_VIEW, uri)
                                                val intent = deepLinkIntent ?: launchIntent ?: browserIntent
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                try {
                                                    context.startActivity(intent)
                                                } catch (_: Exception) {}
                                            }
                                        )
                                    }
                                }
                                item { Spacer(modifier = Modifier.height(16.dp)) }
                            }
                        }
                    }
                }
            }
            }
        }
    }
}

@Composable
private fun SettingsTab(
    selectedTypes: Set<CloudType>,
    onToggle: (CloudType) -> Unit,
    baseUrl: String,
    onBaseUrlChange: (String) -> Unit,
    onClearCache: () -> Unit,
    serverStatus: ServerStatus,
    modifier: Modifier = Modifier
) {
    SettingsScreen(
        selectedTypes = selectedTypes,
        onToggle = onToggle,
        baseUrl = baseUrl,
        onBaseUrlChange = onBaseUrlChange,
        onClearCache = onClearCache,
        serverStatus = serverStatus,
        modifier = modifier
    )
}

@Composable
private fun SegmentTabs(
    groups: List<MergedGroup>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        modifier = modifier,
        edgePadding = 0.dp,
        divider = {},
        indicator = { _ -> }
    ) {
        groups.forEachIndexed { index, group ->
            val selected = selectedIndex == index
            Tab(
                selected = selected,
                onClick = { onSelect(index) },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.outline
            ) {
                Text(
                    text = "${group.cloudType.displayName} (${group.items.size})",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                )
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String, message: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("", text))
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}
