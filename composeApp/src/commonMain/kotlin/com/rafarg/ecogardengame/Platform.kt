package com.rafarg.ecogardengame

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform