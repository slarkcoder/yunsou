package me.slarker.yunsou

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import me.slarker.yunsou.ui.search.SearchScreen
import me.slarker.yunsou.ui.search.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: SearchViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            SearchScreen(
                uiState = uiState,
                onQueryChange = viewModel::onQueryChange,
                onCloudTypeToggle = viewModel::onCloudTypeToggle,
                onSearch = viewModel::onSearch,
                onTabChange = viewModel::onTabChange,
                onBaseUrlChange = viewModel::onBaseUrlChange,
                onSearchHistoryItemClick = viewModel::onSearchHistoryItemClick,
                onClearCache = viewModel::clearCache,
                onCheckServerStatus = viewModel::checkServerStatus,
                onCheckUpdate = viewModel::checkUpdate,
                onThemeToggle = viewModel::onThemeToggle
            )
        }
    }
}
