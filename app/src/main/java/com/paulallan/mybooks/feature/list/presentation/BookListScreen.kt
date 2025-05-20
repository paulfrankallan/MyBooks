@file:OptIn(ExperimentalMaterial3Api::class)

package com.paulallan.mybooks.feature.list.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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

    BookListContent(
        state = state,
        onBookClick = viewModel::selectBook,
        onBookListTypeSelected = viewModel::changeBookListType,
        onDismissBottomSheet = viewModel::clearSelectedBook,
        imageLoader = imageLoader,
        modifier = modifier
    )
}

@Composable
fun BookListContent(
    state: BookListState,
    onBookClick: (Book) -> Unit,
    onBookListTypeSelected: (BookListType) -> Unit,
    onDismissBottomSheet: () -> Unit,
    imageLoader: ImageLoader,
    modifier: Modifier = Modifier
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
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    items(
                        state.books,
                    ) { book ->
                        BookListItem(
                            book = book,
                            imageLoader = imageLoader,
                            onClick = { onBookClick(book) }
                        )
                    }
                }
            }

            state.selectedBook?.let { book ->
                BookDetailsBottomSheet(
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
                        coverUrl = ""
                    ),
                    Book(
                        id = "2",
                        title = "1984",
                        authors = listOf("George Orwell"),
                        coverUrl = ""
                    ),
                    Book(
                        id = "3",
                        title = "To Kill a Mockingbird",
                        authors = listOf("Harper Lee"),
                        coverUrl = ""
                    )
                )
            ),
            onBookClick = {},
            onBookListTypeSelected = {},
            onDismissBottomSheet = {},
            imageLoader = Coil.imageLoader(LocalContext.current),
            modifier = Modifier.fillMaxSize()
        )
    }
}