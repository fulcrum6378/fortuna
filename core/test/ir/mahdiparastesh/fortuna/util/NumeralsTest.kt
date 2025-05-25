package ir.mahdiparastesh.fortuna.util

import java.io.File
import java.io.FileOutputStream

fun main() {
    // FIXME OldPersianNumeral
    val numeral = HieroglyphNumeral()
    val w = StringBuilder()
    w.appendLine(numeral::class.java.simpleName)
    for (subject in subjects)
        w.appendLine(String.format("%-15s", subject) + " " + numeral.of(subject))

    val file = File(System.getProperty("user.home") + "\\Desktop\\test.txt")
    FileOutputStream(file).use { it.write(w.toString().encodeToByteArray()) }
    ProcessBuilder(listOf("C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe", file.path))
        .start()
}

val subjects: Array<Int> = arrayOf(
    -1,
    0,
    2,
    9,
    99,
    100,
    149,
    150,
    199,
    201,
    499,
    500,
    999,
    1000,
    1001,
    6404,
    10002,
    100003,
    1000004,
    2000014,
    2001004,
    4999999,
    5000000,
    1002001005,
)
