package com.paulallan.mybooks.domain.model

import android.content.Context
import com.paulallan.mybooks.R

enum class BookListType(private val stringResId: Int) {
    WANT_TO_READ(R.string.book_list_type_want_to_read),
    CURRENTLY_READING(R.string.book_list_type_currently_reading),
    ALREADY_READ(R.string.book_list_type_already_read);

    fun getDisplayName(context: Context): String {
        return context.getString(stringResId)
    }
}
