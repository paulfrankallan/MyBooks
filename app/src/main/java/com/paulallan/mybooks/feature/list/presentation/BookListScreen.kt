@file:OptIn(ExperimentalMaterial3Api::class)

package com.paulallan.mybooks.feature.list.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.Coil
import coil.ImageLoader
import com.paulallan.mybooks.R
import com.paulallan.mybooks.app.di.ImageLoaderEntryPoint
import com.paulallan.mybooks.app.theme.MyBooksTheme
import com.paulallan.mybooks.domain.model.Book
import com.paulallan.mybooks.domain.model.BookListType
import com.paulallan.mybooks.feature.details.presentation.BookDetailsBottomSheet
import dagger.hilt.android.EntryPointAccessors

@Composable
fun BookListScreen(
    modifier: Modifier = Modifier,
    viewModel: BookListViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val imageLoader = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            ImageLoaderEntryPoint::class.java
        ).imageLoader()
    }

    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.triggerInitialLoadIfNeeded()
    }

    BookListContent(
        modifier = modifier,
        state = state,
        imageLoader = imageLoader,
        onBookClick = viewModel::selectBook,
        onDismissBottomSheet = viewModel::clearSelectedBook,
        onBookListTypeSelected = viewModel::changeBookListType,
        onLoadMore = viewModel::loadMoreBooks
    )
}

@Composable
private fun BookListContent(
    modifier: Modifier = Modifier,
    state: BookListState,
    imageLoader: ImageLoader,
    onBookClick: (Book) -> Unit,
    onDismissBottomSheet: () -> Unit,
    onBookListTypeSelected: (BookListType) -> Unit,
    onLoadMore: () -> Unit
) {
    Scaffold(
        containerColor = Color.Transparent,
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.books_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                BookListTypeSelector(
                    selectedType = state.bookListType,
                    onTypeSelected = onBookListTypeSelected,
                    modifier = Modifier.fillMaxWidth()
                )

                when {
                    state.isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    state.error != null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.error_message, state.error),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    else -> {
                        Box(modifier = Modifier.weight(1f)) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp)
                            ) {
                                items(
                                    items = state.books,
                                    key = { book -> book.id }
                                ) { book ->
                                    BookListItem(
                                        modifier = Modifier,
                                        book = book,
                                        imageLoader = imageLoader,
                                        onClick = { onBookClick(book) }
                                    )
                                }

                                if (state.canLoadMore) {
                                    item {
                                        LaunchedEffect(key1 = state.canLoadMore) {
                                            onLoadMore()
                                        }
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(1.dp)
                                        )
                                    }
                                }
                            }

                            if (state.isLoadingMore) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(48.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            state.selectedBook?.let { book ->
                BookDetailsBottomSheet(
                    modifier = Modifier,
                    book = book,
                    imageLoader = imageLoader,
                    onDismissBottomSheet = onDismissBottomSheet,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BookListScreenPreview() {
    MyBooksTheme {
        BookListContent(
            state = BookListState(
                bookListType = BookListType.WANT_TO_READ,
                books = listOf(
                    Book(
                        id = "1",
                        title = "The Great Gatsby",
                        authors = listOf("F. Scott Fitzgerald"),
                        coverId = null
                    ),
                    Book(
                        id = "2",
                        title = "1984",
                        authors = listOf("George Orwell"),
                        coverId = null
                    ),
                    Book(
                        id = "3",
                        title = "To Kill a Mockingbird",
                        authors = listOf("Harper Lee"),
                        coverId = null
                    )
                )
            ),
            onBookClick = {},
            onBookListTypeSelected = {},
            onDismissBottomSheet = {},
            onLoadMore = {},
            imageLoader = Coil.imageLoader(LocalContext.current),
            modifier = Modifier.fillMaxSize()
        )
    }
}
