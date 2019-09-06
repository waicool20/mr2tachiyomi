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
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun main(args: Array<String>) {
    var input = Paths.get("mangarock.db")
    var output = Paths.get("output.json")

    val options = Options()
    options.addOption("i", "input", true, "input file to convert")
    options.addOption("o", "output", true, "output file")
    options.addOption("h", "help", false, "print help message")

    val parser = DefaultParser()
    try {
        val command = parser.parse(options, args)
        if (command.hasOption('h')) {
            printHelp(options)
            return
        }
        if (command.hasOption('i')) {
            input = Paths.get(command.getOptionValue('i'))
        }
        if (command.hasOption('o')) {
            output = Paths.get(command.getOptionValue('o'))
        }
    } catch (e: ParseException) {
        println("Failed to parse args: " + e.message)
        printHelp(options)
        System.exit(1)
    }

    if (Files.notExists(input)) error(String.format("File %s not found!", input))
    Database.connect("jdbc:sqlite:file:" + input.toString(), driver = "org.sqlite.JDBC")
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
                output,
                it.toByteArray(),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            )
        }
    }
}

fun printHelp(options: Options) {
    val formatter = HelpFormatter()
    formatter.printHelp("mr2tachiyomi", options)
}
