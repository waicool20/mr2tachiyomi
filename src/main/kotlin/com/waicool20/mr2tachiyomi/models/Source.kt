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

package com.waicool20.mr2tachiyomi.models

import com.waicool20.mr2tachiyomi.models.database.Favorite
import com.waicool20.mr2tachiyomi.models.database.MangaChapter

sealed class Source(val favorite: Favorite) {
    class UnsupportedSourceException(sourceId: Int) : Exception("Unsupported source: $sourceId")
    companion object {
        fun forFavorite(favorite: Favorite): Source = when (val id = favorite.sourceId) {
            1 -> MangaEden(favorite)
            4 -> MangaReader(favorite)
            71 -> MangaRock(favorite)
            else -> throw UnsupportedSourceException(id)
        }
    }

    abstract val TachiyomiId: Long
    abstract fun getMangaUrl(): String
    abstract fun getChapterUrl(mangaChapter: MangaChapter): String

    class MangaRock(favorite: Favorite) : Source(favorite) {
        override val TachiyomiId = 1554176584893433663
        override fun getMangaUrl(): String {
            return "/manga/${favorite.mangaOid}"
        }

        override fun getChapterUrl(mangaChapter: MangaChapter): String {
            return "/pagesv2?oid\u003d${mangaChapter.oid}"
        }
    }

    class MangaReader(favorite: Favorite) : Source(favorite) {
        override val TachiyomiId = 789561949979941461
        override fun getMangaUrl(): String {
            return "/${favorite.adjustedMangaName}"
        }

        override fun getChapterUrl(mangaChapter: MangaChapter): String {
            return "${getMangaUrl()}/${mangaChapter.adjustedChapterName}"
        }

        private val Favorite.adjustedMangaName
            get() = mangaName.toLowerCase().replace(" ", "-")
        private val MangaChapter.adjustedChapterName
            get() = title.dropWhile { !it.isDigit() }.takeWhile { it.isDigit() }
    }

    class MangaEden(favorite: Favorite) : Source(favorite) {
        override val TachiyomiId = 6894303465364688269
        override fun getMangaUrl(): String {
            return "/en/en-manga/${favorite.adjustedMangaName}"
        }

        override fun getChapterUrl(mangaChapter: MangaChapter): String {
            return "${getMangaUrl()}/${mangaChapter.adjustedChapterName}/1/"
        }

        private val Favorite.adjustedMangaName
            get() = mangaName.toLowerCase().replace(" ", "-")
        private val MangaChapter.adjustedChapterName
            get() = title.dropWhile { !it.isDigit() }.takeWhile { it.isDigit() }
    }
}