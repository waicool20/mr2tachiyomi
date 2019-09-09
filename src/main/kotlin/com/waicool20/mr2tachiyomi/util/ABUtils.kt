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
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files
import java.nio.file.Path
import kotlin.math.min

object ABUtils {
    class EncryptedBackupUnsupportedException : Exception("Only unencrypted backups are supported")

    private val logger = LoggerFactory.getLogger(javaClass)

    fun ab2tar(input: Path, output: Path) {
        logger.debug("Android Backup to Tar Archive, input file: ${input.toAbsolutePath()}")
        Files.newInputStream(input).use { i ->
            Files.newOutputStream(output).use { o ->
                processFile(i, o)
            }
        }
    }

    /**
     * Original C implementation over here: https://github.com/floe/helium_ab2tar/blob/master/ab2tar_cut.c
     */
    private fun processFile(input: InputStream, output: OutputStream) {
        val byteBuffer = ByteBuffer.allocate(65536).order(ByteOrder.LITTLE_ENDIAN)

        byteBuffer.putInputStreamAndFlip(input, 26)
        logger.debug("Magic: ${byteBuffer.getString(15).trim()}")
        logger.debug("Format: ${byteBuffer.getString(2).trim()}")
        logger.debug("Compression Flag: ${byteBuffer.getString(2).trim()}")
        val encryption = byteBuffer.getString(5).trim()
        logger.debug("Encryption: $encryption")
        if (encryption != "none") throw EncryptedBackupUnsupportedException()

        logger.debug("head? %02x %02x".format(byteBuffer.get(), byteBuffer.get()))

        var type = 0
        var size: Int
        var compl: Int

        val blockDebug = StringBuilder("blocks? ")
        var nextOffset = 0

        val blockDataBuffer = ByteBuffer.allocate(5).order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.clear()
        while (type == 0 && input.available() > 0) {
            blockDataBuffer.clear()
            blockDataBuffer.putInputStreamAndFlip(input, 5)
            type = blockDataBuffer.get().toInt()
            size = blockDataBuffer.short.toUShort().toInt()
            compl = blockDataBuffer.short.toUShort().toInt()

            if (size + compl != 65535) blockDebug.append("@")
            blockDebug.append("%02x (0x%04x) ".format(type, size))

            while (size > 0) {
                val bytes = min(byteBuffer.remaining(), size)
                byteBuffer.putInputStreamAndFlip(input, bytes)
                logger.debug("---- Next offset: $nextOffset")
                if (nextOffset > bytes) {
                    nextOffset -= bytes
                } else {
                    nextOffset = correctHeaders(byteBuffer, nextOffset) - bytes
                }

                val copyBytes = if (nextOffset < 0) {
                    (bytes + nextOffset).also { nextOffset = 0 }
                } else {
                    bytes
                }

                output.write(byteBuffer.array(), 0, copyBytes)
                byteBuffer.position(byteBuffer.position() + copyBytes)
                byteBuffer.compact()
                size -= bytes
            }
        }
        logger.debug(blockDebug.toString())

        byteBuffer.clear()
        byteBuffer.putInputStreamAndFlip(input, 4)
        logger.debug(
            "chksum? %02x %02x %02x %02x".format(
                byteBuffer.get(),
                byteBuffer.get(),
                byteBuffer.get(),
                byteBuffer.get()
            )
        )

        input.close()
        output.close()
    }

    /**
     * Original C implementation over here: https://github.com/floe/helium_ab2tar/blob/master/tar2ab_corr.c
     */
    private fun correctHeaders(input: ByteBuffer, startOffset: Int): Int {
        val byteBuffer = input.duplicate()
        var offset = startOffset
        while ((offset + 512) < byteBuffer.limit()) {
            byteBuffer.position(offset)
            logger.debug("File: ${byteBuffer.getString(TarHeaderOffsets.NAME_RANGE.last, true)}")

            val numberOfBlocks = if (byteBuffer[offset] > 0) {
                byteBuffer.position(offset + TarHeaderOffsets.SIZE + 1)
                val size = byteBuffer.getString(11).toLong(8)
                logger.debug("Size: $size | Offset: $offset | Buffer limit: ${byteBuffer.limit()}")
                byteBuffer.position(offset + TarHeaderOffsets.MTIME + 1)
                val time = byteBuffer.getString(11).toLong(8)
                byteBuffer.put(offset + TarHeaderOffsets.SIZE + 11, 0)
                byteBuffer.put(offset + TarHeaderOffsets.MTIME + 11, 0)

                byteBuffer.position(offset + TarHeaderOffsets.SIZE)
                byteBuffer.put("%011o".format(size).toByteArray())
                byteBuffer.position(offset + TarHeaderOffsets.MTIME)
                byteBuffer.put("%011o".format(time).toByteArray())
                if (size > 0) 1 + (size - 1) / 512 else 0
            } else 0
            logger.debug("Blocks: $numberOfBlocks | Stored size: ${numberOfBlocks * 512}")

            // Adjust checksum header
            byteBuffer.position(offset + TarHeaderOffsets.CHKSUM)
            byteBuffer.put("        ".toByteArray())

            var newCheckSum = 0
            for (i in 0 until 512) {
                newCheckSum += byteBuffer.get(offset + i)
            }
            byteBuffer.position(offset + TarHeaderOffsets.CHKSUM)
            byteBuffer.put("%06o\u0000 ".format(newCheckSum).toByteArray())

            offset += (numberOfBlocks.toInt() + 1) * 512
        }
        return offset
    }
}