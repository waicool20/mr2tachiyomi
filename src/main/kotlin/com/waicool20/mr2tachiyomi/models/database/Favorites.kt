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

import com.waicool20.mr2tachiyomi.models.Source
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object Favorites : IntIdTable(columnName = "manga_id") {
    val author = varchar("author", 1024).nullable()
    val lastRead = long("last_read")
    val lastSync = long("last_sync")
    val mangaAliases = varchar("manga_aliases", 1024)
    val mangaName = varchar("manga_name", 1024)
    val mangaOid = varchar("manga_oid", 1024)
    val sourceId = integer("source_id")
    val newChapters = blob("new_chapters")
    val numberOfNewChapters = integer("num_new_chapters")
    val thumbnailUrl = varchar("thumbnailUrl", 1024)
}

class Favorite(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Favorite>(Favorites)

    val author by lazy { Favorites.author.lookup() ?: "Unknown Author" }
    val lastRead by Favorites.lastRead
    val lastSync by Favorites.lastSync
    val mangaAliases by Favorites.mangaAliases
    val mangaName by Favorites.mangaName
    val mangaOid by Favorites.mangaOid
    val sourceId by Favorites.sourceId
    val newChapters by Favorites.newChapters
    val numberOfNewChapters by Favorites.numberOfNewChapters
    val thumbnailUrl by Favorites.thumbnailUrl

    val source by lazy { Source.forFavorite(this) }

    override fun toString(): String {
        return "[$mangaOid] $mangaName by $author"
    }
}