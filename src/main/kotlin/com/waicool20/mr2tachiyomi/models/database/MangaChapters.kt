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

package com.waicool20.mr2tachiyomi.models.database

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object MangaChapters : IntIdTable("MangaChapter", "_id") {
    val mangaId = integer("manga_id")
    val numberOfPages = integer("num_pages")
    val oid = varchar("oid", 1024)
    val chapterOrder = integer("chapter_order")
    val title = varchar("title", 1024)

    val local = optReference("_id", MangaChapterLocals)
}

class MangaChapter(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<MangaChapter>(MangaChapters)

    val mangaId by MangaChapters.mangaId
    val numberOfPages by MangaChapters.numberOfPages
    val oid by MangaChapters.oid
    val chapterOrder by MangaChapters.chapterOrder
    val title by MangaChapters.title

    val local by MangaChapterLocal optionalReferencedOn MangaChapters.local
}