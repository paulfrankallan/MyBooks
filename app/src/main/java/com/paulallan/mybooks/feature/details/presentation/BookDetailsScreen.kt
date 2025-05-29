package com.paulallan.mybooks.feature.details.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.Coil
import com.paulallan.mybooks.R
import com.paulallan.mybooks.domain.model.Book
import com.paulallan.mybooks.feature.shared.presentation.BookCoverImage
import com.paulallan.mybooks.feature.shared.presentation.CoverSize
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailsScreen(
    bookId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BookDetailsViewModel = koinViewModel { parametersOf(bookId) }
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        BookDetailsContent(
            state = state,
            viewModel = viewModel,
            modifier = modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun BookDetailsContent(
    state: BookDetailsState,
    modifier: Modifier = Modifier,
    viewModel: BookDetailsViewModel? = null
) {
    val context = LocalContext.current
    val imageLoader = Coil.imageLoader(context)

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            state.isLoading -> {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
            }
            state.error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = state.error,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (viewModel != null) {
                        Button(
                            onClick = { 
                                // Retry loading the book details
                                viewModel.processIntent(BookDetailsIntent.RetryLoadingBookDetails)
                            }
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
            state.book != null -> {
                BookDetailContent(
                    book = state.book,
                    imageLoader = imageLoader,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun BookDetailContent(
    book: Book,
    imageLoader: ImageLoader,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
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
                    imageLoader = imageLoader,
                    coverSize = CoverSize.LARGE
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = book.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.book_author_prefix, book.authors.joinToString(", ")),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.book_first_published, book.firstPublishedYear ?: ""),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BookDetailsScreenPreview() {
    val dummyBook = Book(
        id = "1",
        title = "The Great Gatsby",
        authors = listOf("F. Scott Fitzgerald"),
        coverId = null,
        firstPublishedYear = "1925"
    )

    BookDetailsContent(
        state = BookDetailsState(book = dummyBook),
        modifier = Modifier.fillMaxSize()
    )
}
