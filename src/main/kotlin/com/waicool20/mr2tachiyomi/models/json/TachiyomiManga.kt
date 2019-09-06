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

package com.waicool20.mr2tachiyomi.models.json

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonPropertyOrder("manga", "chapters")
data class TachiyomiManga(
    @JsonIgnore val url: String,
    @JsonIgnore val title: String,
    @JsonIgnore val source: Long,
    @JsonIgnore val viewer: Int = 0,
    @JsonIgnore val chapterFlags: Int = 0,
    val chapters: List<TachiyomiChapter>
) {
    @JsonProperty("manga")
    val details = listOf(url, title, source, viewer, chapterFlags)
}