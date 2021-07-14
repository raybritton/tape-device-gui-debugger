package com.raybritton.td_debug

const val MAIN_TD_VERSION = "1.2.0"
val COMPAT_TD_VERSIONS = listOf("1.1.0", "1.2.0")

enum class Request(val cmd: UByte) {
    Step(101u),
    StepIgnoreBreakpoint(102u),
    SetBreakpoint(98u),
    ClearBreakpoint(99u),
    Dump(100u),
    RequestMemory(109u),
    InputChar(107u),
    InputString(116u),
}

enum class Response(val cmd: UByte) {
    Output(111u),
    Error(101u),
    Dump(100u),
    BreakpointHit(104u),
    Memory(109u),
    RequestChar(107u),
    RequestString(116u),
    Halted(102u),
    Crashed(99u),
}