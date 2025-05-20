package com.paulallan.mybooks.domain.model

data class Book(
    val id: String,
    val title: String,
    val authors: List<String>,
    val firstPublishedYear: String? = null,
    val coverId: Long?,
)
