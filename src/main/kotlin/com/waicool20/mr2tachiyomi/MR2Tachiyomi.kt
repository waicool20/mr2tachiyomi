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

import com.waicool20.mr2tachiyomi.converters.Converter
import com.waicool20.mr2tachiyomi.converters.CsvConverter
import com.waicool20.mr2tachiyomi.converters.JsonConverter
import com.waicool20.mr2tachiyomi.util.ABUtils
import com.waicool20.mr2tachiyomi.util.TarHeaderOffsets
import com.waicool20.mr2tachiyomi.util.printHelp
import com.waicool20.mr2tachiyomi.util.toStringAndTrim
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.slf4j.LoggerFactory
import tornadofx.launch
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.system.exitProcess

private const val CMD = "java -jar mr2tachiyomi.jar"
private val options = Options().apply {
    addOption("i", "input", true, "input file to convert")
    addOption("o", "output", true, "output file")
    addOption("h", "help", false, "print help message")
}

private val logger = LoggerFactory.getLogger("Main")

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        launch<UserInterface>(args)
        exitProcess(0)
    }

    val input: Path
    val output: Path

    try {
        val command = DefaultParser().parse(options, args)
        if (command.hasOption('h')) {
            options.printHelp(CMD)
            return
        }

        input = Paths.get(command.getOptionValue('i') ?: "mangarock.db")
        output = Paths.get(command.getOptionValue('o') ?: "output.json")
    } catch (e: ParseException) {
        logger.error("Failed to parse args: " + e.message)
        options.printHelp(CMD)
        exitProcess(1)
    }
    MR2Tachiyomi.convert(input, output)
}

object MR2Tachiyomi {
    private val logger = LoggerFactory.getLogger(javaClass)
    val converters = listOf(JsonConverter, CsvConverter)

    fun convert(input: Path, output: Path): Converter.Result {
        val inputExtension = "$input".takeLastWhile { it != '.' }
        val outputExtension = "$output".takeLastWhile { it != '.' }
        return try {
            if (Files.notExists(input)) return Converter.Result.FailedWithException(FileNotFoundException("File $input not found!"))

            val database = when(inputExtension) {
                "db" -> input
                "ab" -> extractDbFromAb(input)
                else -> throw UnsupportedFileFormatException(inputExtension)
            }

            converters.find { it.fileExtensions.contains(outputExtension) }
                ?.convert(database, output)
                ?: throw UnsupportedFileFormatException(outputExtension)
        } catch (e: Exception) {
            logger.error("Could not convert database file to $outputExtension due to unknown exception", e)
            e.printStackTrace()
            Converter.Result.FailedWithException(e)
        }
    }

    private fun extractDbFromAb(input: Path): Path {
        val tarFile = input.resolveSibling("${input.toFile().nameWithoutExtension}.tar")
        val db = input.resolveSibling("mangarock.db")
        val buffer = ByteArray(512)

        ABUtils.ab2tar(input, tarFile)
        Files.newInputStream(tarFile).use { inputStream ->
            while (inputStream.available() > 0) {
                inputStream.read(buffer)
                val name = buffer.sliceArray(TarHeaderOffsets.NAME_RANGE).toStringAndTrim()
                if (name == "apps/com.notabasement.mangarock.android.lotus/db/mangarock.db") {
                    val size = buffer.sliceArray(TarHeaderOffsets.SIZE_RANGE).toStringAndTrim().toInt(8)
                    val blocks = if (size > 0) 1 + (size - 1) / 512 else 0
                    Files.newOutputStream(db).use { outputStream ->
                        repeat(blocks) {
                            inputStream.read(buffer)
                            outputStream.write(buffer)
                        }
                    }
                    break
                }
            }
        }
        return db
    }
}
