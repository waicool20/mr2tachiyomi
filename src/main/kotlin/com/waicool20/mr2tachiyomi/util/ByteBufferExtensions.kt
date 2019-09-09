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

import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.charset.Charset

fun ByteBuffer.getString(length: Int,charset: Charset = Charsets.UTF_8): String {
    val limit = position() + length
    return duplicate().let {
        it.limit(limit)
        charset.decode(it).toString()
    }.also { position(limit) }
}

fun ByteBuffer.putInputStreamAndFlip(inputStream: InputStream, length: Int = remaining()) {
    putInputStream(inputStream, length)
    flip()
}

fun ByteBuffer.putInputStream(inputStream: InputStream, length: Int = remaining()) {
    if (length > 0) {
        if (hasArray()) {
            inputStream.read(array(), position(), length)
            position(position() + length)
        } else {
            val buffer = ByteArray(length)
            inputStream.read(buffer)
            put(buffer)
        }
    }
}