package com.paulallan.mybooks.data.api

object ApiConstants {
    const val BASE_URL = "https://openlibrary.org/"
    const val DEFAULT_PAGE_SIZE = 10
    
    // Endpoints
    const val WANT_TO_READ_ENDPOINT = "people/mekBot/books/want-to-read.json"
    const val CURRENTLY_READING_ENDPOINT = "people/mekBot/books/currently-reading.json"
    const val ALREADY_READ_ENDPOINT = "people/mekBot/books/already-read.json"
}
