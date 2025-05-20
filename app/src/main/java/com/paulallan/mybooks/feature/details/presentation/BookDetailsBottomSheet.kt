package com.paulallan.mybooks.feature.details.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import com.paulallan.mybooks.R
import com.paulallan.mybooks.domain.model.Book
import com.paulallan.mybooks.feature.shared.presentation.BookCoverImage

@ExperimentalMaterial3Api
@Composable
fun BookDetailsBottomSheet(
    imageLoader: ImageLoader,
    onDismissBottomSheet: () -> Unit,
    book: Book,
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    ModalBottomSheet(
        onDismissRequest = onDismissBottomSheet,
        sheetState = sheetState
    ) {
        BookDetailContent(
            book = book,
            imageLoader = imageLoader,
        )
    }
}

@Composable
private fun BookDetailContent(
    book: Book,
    imageLoader: ImageLoader,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                BookCoverImage(
                    book = book,
                    coverWidth = 500,
                    coverHeight = 700,
                    contentScale = ContentScale.Fit,
                    imageLoader = imageLoader
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = book.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.book_author_prefix, book.authors.joinToString(", ")),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.book_first_published, book.firstPublishedYear ?: ""),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@ExperimentalMaterial3Api
@Preview(showBackground = true)
@Composable
fun BookDetailsBottomSheetPreview() {
    val context = LocalContext.current
    val imageLoader = coil.Coil.imageLoader(context)
    val dummyBook = Book(
        id = "1",
        title = "The Great Gatsby",
        authors = listOf("F. Scott Fitzgerald"),
        coverUrl = "",
        firstPublishedYear = "1925"
    )
    BookDetailContent(
        book = dummyBook,
        imageLoader = imageLoader
    )
}