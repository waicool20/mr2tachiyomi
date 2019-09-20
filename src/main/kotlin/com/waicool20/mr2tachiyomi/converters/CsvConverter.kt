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

package com.waicool20.mr2tachiyomi.converters

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.waicool20.mr2tachiyomi.models.csv.CsvManga
import com.waicool20.mr2tachiyomi.models.database.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.nio.file.Path

object CsvConverter : Converter("CSV File", "csv", "txt") {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun convert(database: Path, output: Path): Result {
        Database.connect("jdbc:sqlite:file:$database", driver = "org.sqlite.JDBC")
        return transaction {
            SchemaUtils.create(
                Favorites,
                MangaChapters,
                MangaChapterLocals
            )
            val favs = Favorite.all().toList()
            favs.map { fav ->
                val (read, unread) = MangaChapter.find { MangaChapters.mangaId eq fav.id.value }
                    .partition { it.local?.read == 1 }
                CsvManga(fav.mangaName, fav.author, read, unread)
                    .also { logger.info("Processed $fav") }
            }.let {
                CsvMapper().apply {
                    writer(schemaFor(CsvManga::class.java).withHeader()).writeValue(output.toFile(), it)
                }
            }
            logger.info("-----------------")
            logger.info("Succesfully processed ${favs.size} manga")
            Result.ConversionComplete(favs, emptyList())
        }
    }
}