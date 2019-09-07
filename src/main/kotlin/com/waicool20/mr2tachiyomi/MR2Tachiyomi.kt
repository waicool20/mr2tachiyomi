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
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.Exception
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import kotlin.system.exitProcess

private val options = Options().apply {
    addOption("i", "input", true, "input file to convert")
    addOption("o", "output", true, "output file")
    addOption("h", "help", false, "print help message")
}

fun main(args: Array<String>) {
    val input: Path
    val output: Path

    try {
        val command = DefaultParser().parse(options, args)
        if (command.hasOption('h')) {
            printHelp(options)
            return
        }

        input = Paths.get(command.getOptionValue('i') ?: "mangarock.db")
        output = Paths.get(command.getOptionValue('o') ?: "output.json")
    } catch (e: ParseException) {
        println("Failed to parse args: " + e.message)
        printHelp(options)
        exitProcess(1)
    }
    MR2Tachiyomi.convertToTachiyomiJson(input, output)
}

fun printHelp(options: Options) {
    val formatter = HelpFormatter()
    formatter.printHelp("mr2tachiyomi", options)
}

object MR2Tachiyomi {
    fun convertToTachiyomiJson(input: Path, output: Path): Boolean = try {
        if (Files.notExists(input)) error("File $input not found!")
        Database.connect("jdbc:sqlite:file:$input", driver = "org.sqlite.JDBC")
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
                    ).also { println("Processed $fav") }
                } catch (e: Source.UnsupportedSourceException) {
                    println("Could not process manga ( $fav ): ${e.message}")
                    null
                }
            }.also {
                println("Processed ${it.size} manga")
            }.let {
                jacksonObjectMapper().writerWithDefaultPrettyPrinter()
                    .writeValueAsString(TachiyomiBackup(it))
            }.let {
                Files.write(
                    output,
                    it.toByteArray(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
                )
            }
        }
        true
    } catch (e: Exception) {
        println("Could not convert database file to Tachiyomi Json due to unknown exception")
        e.printStackTrace()
        false
    }
}
