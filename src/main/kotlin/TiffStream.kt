import loci.common.RandomAccessInputStream
import loci.common.RandomAccessOutputStream


fun writeTiffStream(
    inputFile: String,
    outputFile: String,
    height: Int,
    width: Int
) {

    val inputStream = RandomAccessInputStream(inputFile)
    val outputStream = RandomAccessOutputStream(outputFile)

    outputStream.order(true)

    val ifd = IFD(
        entryCount = 18,
        entries = listOf(
            IFDEntry(tagId = TAG_NEW_SUBFILE_TYPE,              field = SHORT, data = 1),
            IFDEntry(tagId = TAG_IMAGE_WIDTH,                   field = SHORT, data = width),
            IFDEntry(tagId = TAG_IMAGE_LENGTH,                  field = SHORT, data = height),
            IFDEntry(tagId = TAG_BITS_PER_SAMPLE,               field = SHORT, data = 16),
            IFDEntry(tagId = TAG_COMPRESSION,                   field = SHORT, data = 1),
            IFDEntry(tagId = TAG_PHOTOMETRIC_INTERPRETATION,    field = SHORT, data = 2),
            IFDEntry(tagId = TAG_FILL_ORDER,                    field = SHORT, data = 1),
            IFDEntry(tagId = TAG_STRIP_OFFSETS,                 field = SHORT, data = 230),
            IFDEntry(tagId = TAG_SAMPLES_PER_PIXEL,             field = SHORT, data = 1),
            IFDEntry(tagId = TAG_ROWS_PER_STRIP,                field = SHORT, data = width),
            IFDEntry(tagId = TAG_STRIP_BYTE_COUNTS,             field = SHORT, data = 16),
            IFDEntry(tagId = TAG_X_RESOLUTION,                  field = RATIONAL, data = 1),
            IFDEntry(tagId = TAG_Y_RESOLUTION,                  field = RATIONAL, data = 1),
            IFDEntry(tagId = TAG_PLANAR_CONFIGURATION,          field = SHORT, data = 1),
            IFDEntry(tagId = TAG_RESOLUTION_UNIT,               field = SHORT, data = 2),
            IFDEntry(tagId = TAG_EXTRA_SAMPLES,                 field = SHORT, data = 1),
            IFDEntry(tagId = TAG_SAMPLE_FORMAT,                 field = SHORT, data = 11),
            IFDEntry(tagId = TAG_UKNOWN1,                       field = BYTE, data = 1)
        )
    )

    writeHeader(outputStream)
    println(outputStream.filePointer)
    val bitmapOffset = writeIFD(outputStream, ifd)
    println(bitmapOffset)
    println(outputStream.filePointer)
    // writeBitmap(outputStream, inputStream, bitmapOffset, height * width * 2)

    inputStream.close()
    outputStream.close()
}

fun writeHeader(outputStream: RandomAccessOutputStream) {
    // Header -- 8 bytes
    outputStream.seek(START)
    outputStream.writeBytes(LITTLE)
    outputStream.writeShort(ARB_ID)
    outputStream.writeInt(FIRST_IFD_OFFSET)
}

fun writeIFD(outputStream: RandomAccessOutputStream, ifd: IFD): Int {
    outputStream.seek(FIRST_IFD_OFFSET.toLong())

    // write entry count
    outputStream.writeShort(ifd.entryCount)

    // write IFD entries
    var dataPointer = getDataPointer(ifd)

    for (i in 0 until ifd.entryCount) {
        outputStream.writeShort(ifd.entries[i].tagId)
        outputStream.writeShort(ifd.entries[i].field)
        outputStream.writeInt(1)
        outputStream.writeInt(ifd.entries[i].data)
        // writeValue(outputStream, dataPointer, ifd.entries[i])
        // dataPointer += calculateSize(ifd.entries[i])
    }

    // write next IFD offset
    outputStream.writeInt(START.toInt())

    return dataPointer
}

fun writeValue(outputStream: RandomAccessOutputStream, dataPointer: Int, ifdEntry: IFDEntry) {
    val curr = outputStream.filePointer
    outputStream.seek(dataPointer.toLong())

    if (ifdEntry.field == SHORT) {
        outputStream.writeShort(ifdEntry.data)
    } else if (ifdEntry.field == LONG) {
        outputStream.writeLong(ifdEntry.data.toLong())
    } else {
        outputStream.writeByte(ifdEntry.data)
    }

    outputStream.seek(curr)
}

fun writeBitmap(
    outputStream: RandomAccessOutputStream,
    inputStream: RandomAccessInputStream,
    offset: Int,
    nBytes: Int
) {
    outputStream.seek(offset.toLong())
    println("Running through $nBytes bytes!")
    val chunkSize = nBytes / 100
    repeat(100) {
        outputStream.writeBytes(inputStream.readNBytes(chunkSize).decodeToString())
        println("Chunk: $it")
    }
    outputStream.writeBytes(inputStream.readNBytes(nBytes % 100).decodeToString())
}

fun getDataPointer(ifd: IFD) = HEADER_SIZE + IFD.ENTRY_COUNT_SIZE + IFDEntry.SIZE * ifd.entryCount + IFD.NEXT_OFFSET_SIZE

fun calculateSize(entry: IFDEntry) = when (entry.field) {
    BYTE        -> 1
    ASCII       -> 1
    SHORT       -> 2
    LONG        -> 4
    RATIONAL    -> 8
    else -> error("INVALID FIELD TYPE")
}

data class IFD(
    val entryCount: Int,
    val entries: List<IFDEntry>
) {
    companion object {
        const val ENTRY_COUNT_SIZE = 2
        const val NEXT_OFFSET_SIZE = 4
    }
}

data class IFDEntry(
    val tagId: Int, // -- Short
    val field: Int, // -- Short
    val data: Int
) {
    companion object {
        const val SIZE = 12
    }
}

const val START = 0L
const val ARB_ID = 42
const val LITTLE = "II"
const val BIG = "MM"
const val FIRST_IFD_OFFSET = 8
const val HEADER_SIZE = 8

// -- Field Types
const val BYTE      = 1 // -- 8 bit unsigned integer
const val ASCII     = 2 // -- 8 bit null terminated string
const val SHORT     = 3 // -- 16 bit unsigned integer
const val LONG      = 4 // -- 32 bit unsigned integer
const val RATIONAL  = 5 // -- 2 32 bit unsigned integers

// -- Tag IDs
const val TAG_NEW_SUBFILE_TYPE              = 254 // SHORT
const val TAG_IMAGE_WIDTH                   = 256 // SHORT or LONG
const val TAG_IMAGE_LENGTH                  = 257 // SHORT or LONG
const val TAG_BITS_PER_SAMPLE               = 258 // SHORT
const val TAG_COMPRESSION                   = 259 // SHORT
const val TAG_PHOTOMETRIC_INTERPRETATION    = 262 // SHORT
const val TAG_FILL_ORDER                    = 266 // SHORT
const val TAG_STRIP_OFFSETS                 = 273 // SHORT or LONG
const val TAG_SAMPLES_PER_PIXEL             = 277 // SHORT
const val TAG_ROWS_PER_STRIP                = 278 // SHORT or LONG
const val TAG_STRIP_BYTE_COUNTS             = 279 // SHORT or LONG
const val TAG_X_RESOLUTION                  = 282 // RATIONAL
const val TAG_Y_RESOLUTION                  = 283 // RATIONAL
const val TAG_PLANAR_CONFIGURATION          = 284 // SHORT
const val TAG_RESOLUTION_UNIT               = 296 // SHORT
const val TAG_EXTRA_SAMPLES                 = 338 // SHORT
const val TAG_SAMPLE_FORMAT                 = 339 // SHORT
const val TAG_UKNOWN1                       = 700 // BYTE