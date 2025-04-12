import kotlinx.coroutines.*
import loci.common.DebugTools
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.listDirectoryEntries


//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
//fun main() {
//
//    DebugTools.setRootLevel("OFF")
//
//    val folderRegex = Regex(pattern = "^.+--.+Iba1CD68 1Mpa \\d\\d_\\d_\\d{1,2}[_-]\\d\$")
//    val fileRegex = Regex(pattern = "^.+\\d\\d_\\d_\\d{1,2}[_-]\\d\\.\\d\\.lof\$")
//
//    val parentFolder = "/Volumes/Undergrad/Vanessa Drevenakova/MICA/1MPa/parameter testing Ib1CD68_1MPa_19_07_2024"
//
//    val folders = Path(parentFolder).listDirectoryEntries().filter {
//        folderRegex.matches(it.toString())
//    }
//
//    val sequenceFolders = folders.map { Path("$it/Sequence 001") }
//
//    val fileNames = sequenceFolders.flatMap { folder ->
//        folder.listDirectoryEntries().filter { fileRegex.matches(it.toString()) }
//    }
//
//    val N = fileNames.size
//
//    println("Found $N files. Is that correct? ...")
//
//    runBlocking {
//        coroutineScope {
//            for (batchIdx in 0 until 7) {
//                launch {
//                    println("Attempting")
//                    delay(3000)
//                    val start = batchIdx * 6
//                    for (fileIdx in start until start + 6) {
//                        process(fileNames[fileIdx])
//                    }
//                }
//            }
//        }
//    }
//}

fun process(f: Path) {
    val srcFile = f.toString()
    println(srcFile)
    val destFile = srcFile.dropLast(4) + ".ome.tiff"
    FileConvert(srcFile, destFile).convert()
    println("${f.fileName} Complete!")
}


fun main(args: Array<String>) {
    try {
        FileConvert(args[0], args[1]).convert()
    } catch (e: Exception) {
        println(e.toString())
    }
}

fun maain() {
    val other_file = "/Volumes/Undergrad/Vanessa Drevenakova/DAB/Vanessa Drevenakova/2024_06_07_13_34_00--Parameter testing DAB Iba1 - 24_3_21-6/Sequence 001/24_3_21-6.3.lof";
    val other_other_file = "/Volumes/Undergrad/Vanessa Drevenakova/DAB/Vanessa Drevenakova/2024_06_07_14_20_50--Parameter testing DAB Iba1 - 24_5_16-1/Sequence 001/24_5-16-1.2.lof"
    val inputFn = "/Volumes/Undergrad/Vanessa Drevenakova/MICA/0.4MPa/Iba1 CD68/2024_06_08_14_48_56--Parameter testing Iba1CD68 24_3_21-7/Sequence 001/24_3_21-7.1.lof";
    process(Path(inputFn));
}