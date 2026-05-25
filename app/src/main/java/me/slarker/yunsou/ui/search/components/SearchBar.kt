package me.slarker.yunsou.ui.search.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.ImeAction

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null,
    onFocusChange: ((Boolean) -> Unit)? = null
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("输入搜索关键词") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        trailingIcon = {
            IconButton(onClick = onSearch) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "搜索",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .then(focusRequester?.let { Modifier.focusRequester(it) } ?: Modifier)
            .then(onFocusChange?.let { Modifier.onFocusChanged { fs -> it(fs.isFocused) } } ?: Modifier),
        shape = MaterialTheme.shapes.large
    )
}
