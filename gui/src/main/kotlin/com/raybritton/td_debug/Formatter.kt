package com.raybritton.td_debug

fun UShort.format(hex: Boolean): String {
    return if (hex) {
        this.toString(16).padStart(4, '0')
    } else {
        this.toString(10)
    }.padStart(5, ' ').uppercase()
}

fun UByte.formatWithChar(hex: Boolean): String {
    var output = if (hex) {
        this.toString(16).padStart(2, '0')
    } else {
        this.toString(10)
    }.padStart(3, ' ').uppercase()
    val letter = this.asLetter()
    if (letter != null) {
        output += " '$letter'"
    } else {
        output += "    "
    }
    return output
}

fun UByte.format(hex: Boolean): String {
    return if (hex) {
        toString(16).padStart(2, '0')
    } else {
        toString(10)
    }.padStart(3, ' ').uppercase()
}

fun UByte.asLetter(): Char? {
    if (this >= 32u && this <= 126u) {
        return Char(this.toInt())
    }
    return null
}

fun UByte.isLetter(): Boolean {
    return (this >= 32u && this <= 126u)
}

object MemoryFormatter {
    var addrFormat: AddrFormat = AddrFormat.HEX
    var memFormat: MemFormat = MemFormat.HEX
    var showLetters: Boolean = true

    fun formatMemory(start: UShort, mem: List<UByte>): String {
        val perRow = valuesPerRow()
        val builder = StringBuilder()
        var addr = start
        for (chunk in mem.chunked(perRow)) {
            builder.append(addrFormat.format(addr))
            builder.append(" ")
            for (value in chunk) {
                builder.append(memFormat.format(value))
                builder.append(" ")
            }
            builder.append("\n")
            addr = (addr + perRow.toUShort()).toUShort()
        }
        return builder.toString()
    }

    private fun valuesPerRow(): Int {
        val remaining = WIDTH - addrFormat.size
        return remaining / (memFormat.size() + 1)
    }

    enum class AddrFormat(val size: Int) {
        HEX(4),
        DEC(5);

        fun format(addr: UShort): String {
            return when (this) {
                HEX -> addr.toString(16).padStart(4, '0')
                DEC -> addr.toString(10).padStart(5, ' ')
            }
        }
    }

    enum class MemFormat(private val size: Int) {
        HEX(2),
        DEC(3),
        BIN(8);

        fun size(): Int {
            return if (showLetters) size + LETTER_SIZE else size
        }

        fun format(value: UByte): String {
            val num = when (this) {
                HEX -> value.toString(16).padStart(2, '0')
                DEC -> value.toString(10).padStart(3, ' ')
                BIN -> value.toString(2).padStart(8, '0')
            }
            return if (showLetters) {
                if (value.isLetter()) {
                    "$num '${value.asLetter()}'"
                } else {
                    "$num    "
                }
            } else {
                num
            }
        }
    }

    private const val LETTER_SIZE = 4// " 'x'"
    private const val WIDTH = 48 //based on the number of characters as the memoryPane is fixed width
}