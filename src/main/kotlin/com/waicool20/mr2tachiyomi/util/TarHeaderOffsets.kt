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

object TarHeaderOffsets {
    const val NAME = 0
    const val MODE = 100
    const val UID = 108
    const val GID = 116
    const val SIZE = 124
    const val MTIME = 136
    const val CHKSUM = 148
    const val TYPEFLAG = 156
    const val LINKNAME = 157
    const val MAGIC = 257
    const val VERSION = 263
    const val UNAME = 265
    const val GNAME = 297
    const val DEVMAJOR = 329
    const val DEVMINOR = 337
    const val PREFIX = 345

    val NAME_RANGE by lazy { NAME until MODE }
    val MODE_RANGE by lazy { MODE until UID }
    val UID_RANGE by lazy { UID until GID }
    val GID_RANGE by lazy { GID until SIZE }
    val SIZE_RANGE by lazy { SIZE until MTIME }
    val MTIME_RANGE by lazy { MTIME until CHKSUM }
    val CHKSUM_RANGE by lazy { CHKSUM until TYPEFLAG }
    val TYPEFLAG_RANGE by lazy { TYPEFLAG until LINKNAME }
    val LINKNAME_RANGE by lazy { LINKNAME until MAGIC }
    val MAGIC_RANGE by lazy { MAGIC until VERSION }
    val VERSION_RANGE by lazy { VERSION until UNAME }
    val UNAME_RANGE by lazy { UNAME until GNAME }
    val GNAME_RANGE by lazy { GNAME until DEVMAJOR }
    val DEVMAJOR_RANGE by lazy { DEVMAJOR until DEVMINOR }
    val DEVMINOR_RANGE by lazy { DEVMINOR until PREFIX }
    val PREFIX_RANGE by lazy { PREFIX until (PREFIX + 155) }
}