package com.paulallan.mybooks.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.paulallan.mybooks.feature.details.presentation.BookDetailsScreen
import com.paulallan.mybooks.feature.list.presentation.BookListScreen

/**
 * Navigation routes for the app
 */
object NavRoutes {
    const val BOOK_LIST = "book_list"
    const val BOOK_DETAILS = "book_details"
    
    // Route with arguments
    fun bookDetails(bookId: String) = "$BOOK_DETAILS/$bookId"
}

/**
 * Navigation arguments
 */
object NavArgs {
    const val BOOK_ID = "bookId"
}

/**
 * Main navigation graph for the app
 */
@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.BOOK_LIST
    ) {
        // Book List Screen
        composable(route = NavRoutes.BOOK_LIST) {
            BookListScreen(
                onBookClick = { bookId ->
                    navController.navigate(NavRoutes.bookDetails(bookId))
                }
            )
        }
        
        // Book Details Screen
        composable(
            route = "${NavRoutes.BOOK_DETAILS}/{${NavArgs.BOOK_ID}}",
            arguments = listOf(
                navArgument(NavArgs.BOOK_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString(NavArgs.BOOK_ID) ?: ""
            BookDetailsScreen(
                bookId = bookId,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}