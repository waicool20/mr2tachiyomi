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

package com.waicool20.mr2tachiyomi.util

import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

object ABUtils {
    class EncryptedBackupUnsupportedException : Exception("Only unencrypted backups are supported")

    private val logger = LoggerFactory.getLogger(javaClass)

    fun ab2tar(input: Path): ByteArray {
        logger.debug("Android Backup to Tar Archive, input file: ${input.toAbsolutePath()}")
        return correctFile(cutFile(Files.readAllBytes(input)))
    }

    fun ab2tar(input: Path, output: Path) {
        Files.write(output, ab2tar(input), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
    }

    /**
     * Original C implementation over here: https://github.com/floe/helium_ab2tar/blob/master/ab2tar_cut.c
     */
    private fun cutFile(input: ByteArray): ByteArray {
        val buffer = ByteArray(65536)
        val inputBytes = ByteBuffer.wrap(input).order(ByteOrder.LITTLE_ENDIAN)

        inputBytes.get(buffer, 0, 24)
        logger.debug("Magic: ${String(buffer, 0, 15).trim()}")
        logger.debug("Format: ${String(buffer, 15, 2).trim()}")
        logger.debug("Compression Flag: ${String(buffer, 17, 2).trim()}")
        val encryption = String(buffer, 19, 4).trim()
        logger.debug("Encryption: $encryption")
        if (encryption != "none") throw EncryptedBackupUnsupportedException()

        logger.debug("head? %02x %02x".format(inputBytes.get(), inputBytes.get()))

        var type = 0
        var size: Int
        var compl: Int

        val cutFile = ByteArrayOutputStream()
        val blockDebug = StringBuilder("blocks? ")
        while (type == 0 && inputBytes.hasRemaining()) {
            type = inputBytes.get().toInt()
            size = inputBytes.short.toUShort().toInt()
            compl = inputBytes.short.toUShort().toInt()

            if (size + compl != 65535) blockDebug.append("@")
            blockDebug.append("%02x (0x%04x) ".format(type, size))

            repeat(size) {
                cutFile.write(inputBytes.get().toInt())
            }
        }
        logger.debug(blockDebug.toString())

        inputBytes.get(buffer, 0, 4)
        logger.debug("chksum? %02x %02x %02x %02x".format(buffer[0], buffer[1], buffer[2], buffer[3]))

        return cutFile.toByteArray()
    }

    /**
     * Original C implementation over here: https://github.com/floe/helium_ab2tar/blob/master/tar2ab_corr.c
     */
    private fun correctFile(input: ByteArray): ByteArray {
        val buffer = ByteArray(513)
        val inputBytes = ByteBuffer.wrap(input).order(ByteOrder.LITTLE_ENDIAN)

        val corrFile = ByteArrayOutputStream()
        while (inputBytes.hasRemaining()) {
            inputBytes.get(buffer, 0, 512)
            // Correct size and time in header
            val numberOfBlocks = if (buffer[0] > 0) {
                val size = String(buffer, TarHeaderOffsets.SIZE + 1, 11).toLong(8)
                val time = String(buffer, TarHeaderOffsets.MTIME + 1, 11).toLong(8)
                buffer[TarHeaderOffsets.SIZE + 11] = 0
                buffer[TarHeaderOffsets.MTIME + 11] = 0
                String.format("%011o", size).toByteArray().copyInto(buffer, TarHeaderOffsets.SIZE)
                String.format("%011o", time).toByteArray().copyInto(buffer, TarHeaderOffsets.MTIME)
                if (size > 0) 1 + (size - 1) / 512 else 0
            } else 0

            // Adjust checksum header
            for (i in 0 until 8) buffer[TarHeaderOffsets.CHKSUM + i] = ' '.toByte()
            String.format("%06o\u0000 ", buffer.sliceArray(0..512).sum()).toByteArray()
                .copyInto(buffer, TarHeaderOffsets.CHKSUM)

            corrFile.write(buffer, 0, 512)
            repeat(512 * numberOfBlocks.toInt()) {
                corrFile.write(inputBytes.get().toInt())
            }
        }
        return corrFile.toByteArray()
    }
}