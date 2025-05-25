package ir.mahdiparastesh.fortuna.util

import java.io.File
import java.io.FileOutputStream

fun main() {
    val numeral = KharosthiNumeral()
    val w = StringBuilder()
    for (subject in subjects)
        w.appendLine(String.format("%-15s", subject) + " " + numeral.output(subject))

    val file = File(System.getProperty("user.home") + "\\Desktop\\test.txt")
    FileOutputStream(file).use { it.write(w.toString().encodeToByteArray()) }
    ProcessBuilder(listOf("C:\\Program Files\\Notepad++\\notepad++.exe", file.path))
        .start()
}

val subjects: Array<Int> = arrayOf(
    -1,
    0,
    2,
    9,
    99,
    100,
    190,
    201,
    999,
    1000,
    1001,
    6404,
    10002,
    100003,
    1000004,
    2000014,
    2001004,
    1002001005,
)
