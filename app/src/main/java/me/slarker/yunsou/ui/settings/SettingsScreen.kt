package me.slarker.yunsou.ui.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.slarker.yunsou.data.model.CloudType
import me.slarker.yunsou.ui.search.ServerStatus
import me.slarker.yunsou.ui.search.UpdateStatus

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    selectedTypes: Set<CloudType>,
    onToggle: (CloudType) -> Unit,
    baseUrl: String,
    onBaseUrlChange: (String) -> Unit,
    onClearCache: () -> Unit,
    serverStatus: ServerStatus,
    updateStatus: UpdateStatus,
    latestVersion: String?,
    releaseUrl: String?,
    releaseNotes: String?,
    onCheckUpdate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp)
    ) {
        val context = LocalContext.current

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Text(
                text = "API 服务器",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(8.dp))
            when (serverStatus) {
                ServerStatus.CHECKING -> {
                    Text(
                        text = "● 检测中...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                ServerStatus.ONLINE -> {
                    Text(
                        text = "● 已连接",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF4CAF50)
                    )
                }
                ServerStatus.OFFLINE -> {
                    Text(
                        text = "● 无法连接",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                ServerStatus.UNKNOWN -> {
                    Text(
                        text = "● 未检测",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
        Text(
            text = "自部署地址，默认为 https://pan.slarker.me/",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = baseUrl,
            onValueChange = onBaseUrlChange,
            placeholder = { Text("https://pan.slarker.me/") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "搜索范围",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "选择要搜索的网盘类型，默认只搜索夸克网盘",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CloudType.entries.forEach { type ->
                FilterChip(
                    selected = type in selectedTypes,
                    onClick = { onToggle(type) },
                    label = { Text(type.displayName) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "缓存",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "搜索结果缓存有效期 7 天",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedButton(onClick = {
            onClearCache()
            Toast.makeText(context, "缓存已清理", Toast.LENGTH_SHORT).show()
        }) {
            Text("清理缓存")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "版本更新",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            when (updateStatus) {
                UpdateStatus.UNCHECKED -> {
                    OutlinedButton(onClick = onCheckUpdate) {
                        Text("检测更新")
                    }
                }
                UpdateStatus.CHECKING -> {
                    Text(
                        text = "● 检测中...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                UpdateStatus.AVAILABLE -> {
                    Text(
                        text = "● 发现新版本 $latestVersion",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF4CAF50)
                    )
                }
                UpdateStatus.UNAVAILABLE -> {
                    Text(
                        text = "● 已是最新版本",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                UpdateStatus.ERROR -> {
                    Text(
                        text = "● 检测失败",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        if (!releaseNotes.isNullOrBlank() && updateStatus == UpdateStatus.AVAILABLE) {
            Text(
                text = releaseNotes,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(bottom = 4.dp),
                maxLines = 5
            )
        }
        if (updateStatus == UpdateStatus.AVAILABLE && releaseUrl != null) {
            OutlinedButton(onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(releaseUrl))
                context.startActivity(intent)
            }) {
                Text("前往下载")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        val versionName = remember {
            try {
                context.packageManager.getPackageInfo(context.packageName, 0).versionName
            } catch (_: Exception) {
                "1.0.0"
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.Center) {
                Text(
                    text = "本应用基于 ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "PanSou",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/fish2018/pansou"))
                        context.startActivity(intent)
                    }
                )
                Text(
                    text = " API 开发",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.Center) {
                Text(
                    text = "Slark",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://slarker.me/"))
                        context.startActivity(intent)
                    }
                )
                Text(
                    text = " / 版本 $versionName",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
