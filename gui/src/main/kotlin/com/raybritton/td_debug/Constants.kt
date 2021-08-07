package com.raybritton.td_debug

const val MAIN_TD_VERSION = "1.2.0"
val COMPAT_TD_VERSIONS = listOf("1.1.0", "1.2.0")

enum class Request(letter: Char) {
    Step('e'),
    StepIgnoreBreakpoint('f'),
    SetBreakpoint('b'),
    ClearBreakpoint('c'),
    Dump('d'),
    RequestMemory('m'),
    InputChar('k'),
    InputString('t');

    val cmd = letter.code.toByte()
}

enum class Response(letter: Char) {
    Output('o'),
    Error('e'),
    Dump('d'),
    BreakpointHit('h'),
    Memory('m'),
    RequestChar('k'),
    RequestString('t'),
    Halted('f'),
    Crashed('c');

    val cmd = letter.code.toUByte()
}