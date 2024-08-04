package org.example


import FileConvert
import loci.common.DebugTools
import loci.formats.`in`.LOFReader
import kotlin.io.path.Path
import kotlin.io.path.appendText
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.relativeTo


//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {

    val parentFolder = "/Volumes/Undergrad/Vanessa Drevenakova/MICA/0.7MPa/Paramter testing (0.7MPa) Iba1CD68_10_07_2024"

    val folders = listOf(
        "2024_07_08_10_13_44--Parameter testing Iba1CD68 24_5_23-7",
        "2024_07_08_11_22_32--Parameter testing Iba1CD68 24_5_23-8",
        "2024_07_08_12_18_24--Parameter testing Iba1CD68 24_3_21_7",
        "2024_07_08_12_51_10--Parameter testing Iba1CD68 24_6_5_2",
        "2024_07_09_10_51_29--Parameter testing Iba1CD68 24_6_5_3",
        "2024_07_09_11_32_58--Parameter testing Iba1CD68 24_6_5_4",
        "2024_07_09_12_06_02--Parameter testing Iba1CD68 24_6_20_3",
        "2024_07_09_12_38_50--Parameter testing Iba1CD68 24_6_6_3",
        "2024_07_09_13_20_15--Parameter testing Iba1CD68 24_6_6_4",
        "2024_07_09_13_53_37--Parameter testing Iba1CD68 24_6_6_5",
        "2024_07_09_14_30_35--Parameter testing Iba1CD68 24_6_6_6",
        "2024_07_09_15_09_43--Parameter testing Iba1CD68 24_5_16_6",
    ).map { Path("$parentFolder/$it/Sequence 001") }

    DebugTools.setRootLevel("OFF")

    val fileNames = folders.flatMap { folder ->
        folder.listDirectoryEntries().filter { f ->
            val srcFile = f.toString()
            srcFile.endsWith(".lof") && !srcFile.contains("Mosaic")
        }
    }

    val N = fileNames.size

    println("Found $N files. Is that correct? ...")

    var count = 0;
    fileNames.forEach { f ->
        val srcFile = f.toString()
        println(srcFile)
        if (srcFile.endsWith(".lof")) {
            val destFile = srcFile.dropLast(4) + ".ome.tiff"
            FileConvert(srcFile, destFile).convert()
            count += 1;
            println("$count/$N Complete!")
        }
    }
}