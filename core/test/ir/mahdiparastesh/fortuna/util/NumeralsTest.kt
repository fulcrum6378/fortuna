package ir.mahdiparastesh.fortuna.util

import java.io.File
import java.io.FileOutputStream

fun main() {
    val numeral = KharosthiNumeral()
    val w = StringBuilder()
    w.appendLine(numeral::class.java.simpleName)
    for (subject in subjects)
        w.appendLine(String.format("%-15s", subject) + " " + numeral.of(subject))

    val file = File(System.getProperty("user.home") + "\\Desktop\\numeral_test.txt")
    FileOutputStream(file).use { it.write(w.toString().encodeToByteArray()) }
    ProcessBuilder(
        listOf("C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe", file.path)
    ).start()
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
    2025,
    6404,
    // never try greater numbers on OldPersianNumeral; it'll make a mess!
    10002,
    100003,
    1000004,
    2000014,
    2001004,
    4999999,
    5000000,
    1002001005,
)
