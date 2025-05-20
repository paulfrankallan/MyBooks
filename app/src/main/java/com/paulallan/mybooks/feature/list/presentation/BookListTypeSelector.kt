package com.paulallan.mybooks.feature.list.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.paulallan.mybooks.domain.model.BookListType

@Composable
fun BookListTypeSelector(
    selectedType: BookListType,
    onTypeSelected: (BookListType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(top = 16.dp)
            .padding(bottom = 12.dp)
            .padding(horizontal = 12.dp)
            .fillMaxWidth()
            .background(Transparent),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        BookListType.entries.forEachIndexed { index, type ->
            val isSelected = type == selectedType
            Button(
                onClick = { onTypeSelected(type) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary
                ),
                border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                shape = when (index) {
                    0 -> RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)
                    BookListType.entries.lastIndex -> RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
                    else -> RoundedCornerShape(0.dp)
                },
                elevation = null,
                modifier = Modifier
                    .weight(1f)
            ) {
                Text(
                    text = type.getDisplayName(LocalContext.current),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2
                )
            }
            if (index < BookListType.entries.lastIndex) {
                Spacer(modifier = Modifier.width(0.dp))
            }
        }
    }
}
