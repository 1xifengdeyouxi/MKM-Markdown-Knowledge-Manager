package com.mkm

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MkmApplication

fun main(args: Array<String>) {
    runApplication<MkmApplication>(*args)
}
