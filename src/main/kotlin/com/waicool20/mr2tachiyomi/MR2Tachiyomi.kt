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

package com.waicool20.mr2tachiyomi

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.waicool20.mr2tachiyomi.models.Source
import com.waicool20.mr2tachiyomi.models.database.*
import com.waicool20.mr2tachiyomi.models.json.TachiyomiBackup
import com.waicool20.mr2tachiyomi.models.json.TachiyomiChapter
import com.waicool20.mr2tachiyomi.models.json.TachiyomiManga
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

fun main() {
    if (Files.notExists(Paths.get("mangarock.db"))) error("File mangarock.db not found!")
    Database.connect("jdbc:sqlite:file:mangarock.db", driver = "org.sqlite.JDBC")
    transaction {
        SchemaUtils.create(
            Favorites,
            MangaChapters,
            MangaChapterLocals
        )

        Favorite.all().mapNotNull { fav ->
            try {
                TachiyomiManga(
                    fav.source.getMangaUrl(),
                    fav.mangaName,
                    fav.source.TachiyomiId,
                    chapters = MangaChapter.find { MangaChapters.mangaId eq fav.id.value }
                        .map {
                            TachiyomiChapter(
                                fav.source.getChapterUrl(it),
                                it.local?.read ?: 0
                            )
                        }
                )
            } catch (e: Source.UnsupportedSourceException) {
                println("Could not process manga [${fav.mangaName}]: ${e.message}")
                null
            }
        }.also {
            println("Processed ${it.size} manga")
        }.let {
            jacksonObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(
                TachiyomiBackup(
                    it
                )
            )
        }.let {
            Files.write(
                Paths.get("output.json"),
                it.toByteArray(),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            )
        }
    }
}