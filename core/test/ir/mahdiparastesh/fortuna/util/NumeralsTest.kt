package ir.mahdiparastesh.fortuna.util

import java.io.File
import java.io.FileOutputStream

fun main() {
    val numeral = KharosthiNumeral()

    val w = StringBuilder()
    w.appendLine(numeral.output(-1))  //         1
    w.appendLine(numeral.output(2))  //          2
    w.appendLine(numeral.output(9))  //          3
    w.appendLine(numeral.output(99))  //         4
    w.appendLine(numeral.output(100))  //        5
    w.appendLine(numeral.output(190))  //        6
    w.appendLine(numeral.output(201))  //        7
    w.appendLine(numeral.output(999))  //        8
    w.appendLine(numeral.output(1000))  //       9
    w.appendLine(numeral.output(1001))  //       10
    w.appendLine(numeral.output(10002))  //      11
    w.appendLine(numeral.output(100003))  //     12
    w.appendLine(numeral.output(1000004))  //    13
    w.appendLine(numeral.output(2000004))  //    14
    w.appendLine(numeral.output(2001004))  //    15
    w.appendLine(numeral.output(1002001005))  // 16

    val file = File(System.getProperty("user.home") + "\\Desktop\\test.txt")
    FileOutputStream(file).use { it.write(w.toString().encodeToByteArray()) }
    ProcessBuilder(listOf("C:\\Program Files\\Notepad++\\notepad++.exe", file.path))
        .start()
}
