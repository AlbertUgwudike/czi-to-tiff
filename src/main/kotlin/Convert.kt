import loci.common.RandomAccessInputStream
import loci.common.RandomAccessOutputStream
import java.io.File
import kotlin.math.pow




fun lof2tiff_(inputFilename: String, outputFilename: String) {
    val inputStream = RandomAccessInputStream(inputFilename)
    val outputStream = RandomAccessOutputStream(outputFilename)

    inputStream.setEncoding("ISO-8859-1")
    inputStream.order(true)

    assert(inputStream.readInt() == 112)
    inputStream.readInt()
    assert(inputStream.readByte().toInt() == 42)

    val nChars = inputStream.readInt()
    val typeName = buildString { repeat(nChars) { append(inputStream.readChar()) } }

    assert(typeName == "LMS_Object_File")
    assert(inputStream.readByte().toInt() == 42)

    inputStream.readInt()
    assert(inputStream.readByte().toInt() == 42)

    inputStream.readInt()
    assert(inputStream.readByte().toInt() == 42)

    val memorySize: Long = inputStream.readLong()

    inputStream.skipBytes(memorySize)
    assert(inputStream.readInt() == 112)

    inputStream.readInt()
    assert(inputStream.readByte().toInt() == 42)

    val nXmlChars = inputStream.readInt()
    val xmlString = buildString { repeat(nXmlChars) { append(inputStream.readChar()) } }

    // val dimRegex = Regex(pattern = "<DimensionDescription .+NumberOfElements=\"([0-9]*)\"")
    val dimRegex = Regex(pattern = "NumberOfElements=\"(?<dim>[0-9]*)\"")
    val dimensions = dimRegex.findAll(xmlString).map { it.groups["dim"]?.value?.toInt() }.toList().filterNotNull()

    assert(dimensions.size == 2)

    println(dimRegex)

    writeTiffStream(
        inputFilename,
        outputFilename,
        dimensions[0],
        dimensions[1],
    )
}

fun testFile(inputFilename: String) {
    val inputStream = RandomAccessInputStream(inputFilename)

    val endian = inputStream.readNBytes(2).decodeToString()
    println(endian)

    inputStream.order(endian == "II")

    val magic = inputStream.readShort()
    println(magic)

    val firstOffset = inputStream.readInt()
    println("firstOffset ${firstOffset}")

    inputStream.seek(firstOffset.toLong())

    val entryCount = inputStream.readShort()
    println("entryCount ${entryCount}")

    for (i in 0 until entryCount) {
        val tagId = inputStream.readShort()
        val fieldId = inputStream.readShort()
        val count = inputStream.readInt()
        val offset = if (endian == "MM") {
            val out = inputStream.readShort()
            inputStream.readShort()
            out
        } else {
            inputStream.readInt()
        }
        println("TagId: $tagId, fieldId: $fieldId, count: $count, offset: $offset")
    }

    val nextIfd = inputStream.readInt()
    println("Next: $nextIfd")

}

fun lof2tiff(inputFilename: String, outputFilename: String) {
    FileConvert(inputFilename, outputFilename).convert()
}

fun main() {
    // val inputFilename = "/Volumes/Undergrad/Vanessa Drevenakova/MICA/1MPa/parameter testing Ib1CD68_1MPa_19_07_2024/2024_07_18_10_18_45--Parameter testing Iba1CD68 1Mpa 24_6_12_2/Sequence 001/24_6_12_2.1.lof"
    val outputFilename = "/Users/albert/projects/lof-to-tiff/test.tiff"
    val inputFilename = "/Users/albert/projects/lof-to-tiff/test.lof"

    // lof2tiff(inputFilename, outputFilename)

    FileConvert(inputFilename, outputFilename).convert()

//    val validFilename = "/Users/vaness/projects/czi-to-tiff/valid.tiff"
//
//    testFile(validFilename)
//    testFile(outputFilename)
}