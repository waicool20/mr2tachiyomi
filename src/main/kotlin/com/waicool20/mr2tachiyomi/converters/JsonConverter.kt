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

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.waicool20.mr2tachiyomi.models.Source
import com.waicool20.mr2tachiyomi.models.database.*
import com.waicool20.mr2tachiyomi.models.json.TachiyomiBackup
import com.waicool20.mr2tachiyomi.models.json.TachiyomiChapter
import com.waicool20.mr2tachiyomi.models.json.TachiyomiManga
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.nio.file.Path

object JsonConverter : Converter("JSON File", "json") {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun convert(database: Path, output: Path): Result {
        Database.connect("jdbc:sqlite:file:$database", driver = "org.sqlite.JDBC")
        return transaction {
            SchemaUtils.create(
                Favorites,
                MangaChapters,
                MangaChapterLocals
            )

            val (convertible, nonConvertible) = Favorite.all().partition {
                try {
                    it.source
                    true
                } catch (e: Source.UnsupportedSourceException) {
                    logger.warn("Cannot process manga ( $it ): ${e.message}")
                    false
                }
            }

            logger.info("-----------------")

            convertible.map { fav ->
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
                ).also { logger.info("Processed $fav") }
            }.let {
                jacksonObjectMapper().writerWithDefaultPrettyPrinter()
                    .writeValue(output.toFile(), TachiyomiBackup(it))
            }
            logger.info("-----------------")
            logger.info("Succesfully processed ${convertible.size} manga; Failed to process ${nonConvertible.size} manga")
            Result.ConversionComplete(convertible, nonConvertible)
        }
    }
}