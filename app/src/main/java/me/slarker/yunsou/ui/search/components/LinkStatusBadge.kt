package me.slarker.yunsou.ui.search.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.slarker.yunsou.data.model.LinkState

@Composable
fun LinkStatusBadge(
    state: LinkState,
    modifier: Modifier = Modifier
) {
    val (icon, color, label) = when (state) {
        LinkState.OK -> Triple(
            Icons.Default.CheckCircle,
            Color(0xFF4CAF50),
            "有效"
        )
        LinkState.BAD -> Triple(
            Icons.Default.RemoveCircle,
            MaterialTheme.colorScheme.error,
            "失效"
        )
        LinkState.LOCKED -> Triple(
            Icons.Default.Lock,
            Color(0xFFFF9800),
            "需密码"
        )
        LinkState.UNSUPPORTED -> Triple(
            Icons.Default.Error,
            MaterialTheme.colorScheme.outline,
            "不支持"
        )
        LinkState.UNCERTAIN -> Triple(
            Icons.Default.Help,
            MaterialTheme.colorScheme.outline,
            "不确定"
        )
        LinkState.UNCHECKED -> Triple(
            Icons.Default.Help,
            MaterialTheme.colorScheme.outline,
            "未检测"
        )
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
