/*
 * GPLv3 License
 *
 *  Copyright (c) mr2tachiyomi by waicool20
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.waicool20.mr2tachiyomi.models.csv

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.waicool20.mr2tachiyomi.models.database.MangaChapter

@JsonPropertyOrder("name", "author", "read_chapters", "unread_chapters")
data class CsvManga(
    val name: String,
    val author: String,
    @JsonIgnore val _readChapters: List<MangaChapter>,
    @JsonIgnore val _unreadChapters: List<MangaChapter>
) {
    @JsonProperty("read_chapters")
    val readChapters = _readChapters.sortedBy { it.chapterOrder }.joinToString("\n") { it.title }

    @JsonProperty("unread_chapters")
    val unreadChapters = _unreadChapters.sortedBy { it.chapterOrder }.joinToString("\n") { it.title }
}