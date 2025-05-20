package com.paulallan.mybooks.domain.extentsion

import org.junit.Assert.assertEquals
import org.junit.Test

class BookMappingExtensionsTest {

    @Test
    fun `orUnknownKey returns original string when not null`() {
        // Arrange
        val input = "test_key"
        val hash = 12345

        // Act
        val result = input.orUnknownKey(hash)

        // Assert
        assertEquals(input, result)
    }

    @Test
    fun `orUnknownKey returns unknown key with hash when null`() {
        // Arrange
        val input: String? = null
        val hash = 12345

        // Act
        val result = input.orUnknownKey(hash)

        // Assert
        assertEquals("unknown_key_12345", result)
    }

    @Test
    fun `toCoverUrl returns correct URL format with LARGE size when not null`() {
        // Arrange
        val coverId: Long = 12345

        // Act
        val result = coverId.toCoverUrl(CoverSize.LARGE)

        // Assert
        assertEquals("https://covers.openlibrary.org/b/id/12345-L.jpg", result)
    }

    @Test
    fun `toCoverUrl returns correct URL format with MEDIUM size when not null`() {
        // Arrange
        val coverId: Long = 12345

        // Act
        val result = coverId.toCoverUrl(CoverSize.MEDIUM)

        // Assert
        assertEquals("https://covers.openlibrary.org/b/id/12345-M.jpg", result)
    }

    @Test
    fun `toCoverUrl uses LARGE size by default when not specified`() {
        // Arrange
        val coverId: Long = 12345

        // Act
        val result = coverId.toCoverUrl()

        // Assert
        assertEquals("https://covers.openlibrary.org/b/id/12345-L.jpg", result)
    }

    @Test
    fun `toCoverUrl handles null value`() {
        // Arrange
        val coverId: Long? = null

        // Act
        val result = coverId.toCoverUrl()

        // Assert
        assertEquals("https://covers.openlibrary.org/b/id/null-L.jpg", result)
    }
}
