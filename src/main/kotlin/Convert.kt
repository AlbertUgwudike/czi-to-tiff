import loci.common.RandomAccessInputStream
import loci.common.RandomAccessOutputStream

// LOF Format
// --

fun lof2tiff(inputFilename: String, outputFilename: String) {
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
    assert(inputStream.readByte().toInt() == 42)

    val nXmlChars = inputStream.readShort().toInt()
    inputStream.readShort()
    val xmlString = buildString { repeat(nXmlChars) { append(inputStream.readChar()) } }

    println("Done! $xmlString")
}

fun main() {
    val inputFilename = "/Users/albert/projects/czi-to-tiff/test.lof"
    val outputFilename = "/Users/albert/projects/czi-to-tiff/test_out.tiff"

    lof2tiff(inputFilename, outputFilename)
}