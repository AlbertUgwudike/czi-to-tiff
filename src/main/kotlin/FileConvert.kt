import loci.common.services.DependencyException
import loci.common.services.ServiceException
import loci.common.services.ServiceFactory
import loci.formats.FormatException
import loci.formats.FormatTools
import loci.formats.ImageReader
import loci.formats.meta.IMetadata
import loci.formats.out.TiffWriter
import loci.formats.services.OMEXMLService
import java.io.IOException

/**
 * Example class for converting a file from one format to another, using
 * Bio-Formats version 4.2 or later.
 *
 * @author Melissa Linkert melissa at glencoesoftware.com
 */
class FileConvert
/**
 * Construct a new FileConvert to convert the specified input file.
 *
 * @param inputFile the file to be read
 * @param outputFile the file to be written
 */(
    /** The file to be read.  */
    private val inputFile: String,
    /** The file to be written.  */
    private val outputFile: String
) {
    /** The file format reader.  */
    private var reader: ImageReader? = null

    /** The file format writer.  */
    private var writer: TiffWriter? = null

    /** Do the actual work of converting the input file to the output file.  */
    fun convert() {
        // initialize the files
        val initializationSuccess = initialize()

        // if we could not initialize one of the files,
        // then it does not make sense to convert the planes
        if (initializationSuccess) {
            convertPlanes()
        }

        // close the files
        cleanup()
    }

    /**
     * Set up the file reader and writer, ensuring that the input file is
     * associated with the reader and the output file is associated with the
     * writer.
     *
     * @return true if the reader and writer were successfully set up, or false
     * if an error occurred
     */
    private fun initialize(): Boolean {
        var exception: Exception? = null
        try {
            // construct the object that stores OME-XML metadata
            val factory = ServiceFactory()
            val service = factory.getInstance(OMEXMLService::class.java)
            val omexml: IMetadata = service.createOMEXMLMetadata()

            // set up the reader and associate it with the input file
            reader = ImageReader()
            reader!!.metadataStore = omexml
            reader!!.setId(inputFile)

            // set up the writer and associate it with the output file
            //writer = ImageWriter()
            writer = TiffWriter();
            writer!!.setBigTiff(true)
            writer!!.metadataRetrieve = omexml
            writer!!.isInterleaved = reader!!.isInterleaved
            writer!!.setId(outputFile)
        } catch (e: FormatException) {
            exception = e
        } catch (e: IOException) {
            exception = e
        } catch (e: DependencyException) {
            exception = e
        } catch (e: ServiceException) {
            exception = e
        }
        if (exception != null) {
            System.err.println("Failed to initialize files.")
            exception.printStackTrace()
        }
        return exception == null
    }

    private fun convertPlanesSingleRGB(series: Int) {
        reader!!.series = series
        try {
            writer!!.series = series
        } catch (e: FormatException) {
            System.err.println("Failed to set writer's series #$series")
            e.printStackTrace()
            return
        }

        val buf_size = reader!!.sizeX * (reader!!.bitsPerPixel / 8) * 3;

        // val plane = ByteArray(FormatTools.getPlaneSize(reader))
        val chonkSize = 1000;
        val plane = ByteArray(buf_size * chonkSize)

        for (image in 0 until reader!!.imageCount) {
            val nChunks: Int = reader!!.sizeY / chonkSize;
            try {
                for (i in 0 until nChunks) {
                    val x = 0
                    val y = i * chonkSize;
                    val w = buf_size / 6; //six bytes per pixel                                                         ]
                    reader!!.openBytes(image, plane, x, y, w, chonkSize)
                    writer!!.saveBytes(image, plane, x, y, w, chonkSize)
                }

                val x = 0
                val y = nChunks * chonkSize;
                val w = buf_size / 6;
                val h = reader!!.sizeY % chonkSize;
                val lastPlane = ByteArray(buf_size * h)
                reader!!.openBytes(image, lastPlane, x, y, w, h)
                writer!!.saveBytes(image, lastPlane, x ,y, w, h)

            } catch (e: IOException) {
                System.err.println(
                    "Failed to convert image #" + image +
                            " in series #" + series
                )
                e.printStackTrace()
            } catch (e: FormatException) {
                System.err.println(
                    ("Failed to convert image #" + image +
                            " in series #" + series)
                )
                e.printStackTrace()
            }
        }
    }

    private fun convertPlanesTripleGray(series: Int) {
        reader!!.series = series
        try {
            writer!!.series = series
        } catch (e: FormatException) {
            System.err.println("Failed to set writer's series #$series")
            e.printStackTrace()
            return
        }

        val plane = ByteArray(FormatTools.getPlaneSize(reader))

        for (image in 0 until reader!!.imageCount) {
            try {
                reader!!.openBytes(image, plane)
                writer!!.saveBytes(image, plane)
            } catch (e: IOException) {
                System.err.println(
                    "Failed to convert image #" + image +
                            " in series #" + series
                )
                e.printStackTrace()
            } catch (e: FormatException) {
                System.err.println(
                    ("Failed to convert image #" + image +
                            " in series #" + series)
                )
                e.printStackTrace()
            }
        }
    }

    /** Save every plane in the input file to the output file.  */
    private fun convertPlanes() {
        for (series in 0 until reader!!.seriesCount) {
            // construct a buffer to hold one image's pixels
            println("#######################################################")
            println(reader!!.imageCount)
            println(reader!!.bitsPerPixel)
            println(reader!!.sizeC)
            println(reader!!.sizeT)
            println(reader!!.sizeX)
            println(reader!!.sizeY)
            println(reader!!.sizeZ)
            println(reader!!.rgbChannelCount)
            println("GetPlaneSz ${FormatTools.getPlaneSize(reader)}")
            val sz = reader!!.sizeX * reader!!.sizeY * (reader!!.bitsPerPixel / 8)
            val buf_size = reader!!.sizeX * (reader!!.bitsPerPixel / 8) * 3;
            println("calculated: $sz")
            println("Max ${Int.MAX_VALUE}")
            println("ImageCount ${reader!!.imageCount}")
            println("#######################################################")

            if (reader!!.imageCount == 1) {
                convertPlanesSingleRGB(series)
            } else {
                convertPlanesTripleGray(series)
            }
        }
    }

    /** Close the file reader and writer.  */
    private fun cleanup() {
        try {
            reader!!.close()
            writer!!.close()
        } catch (e: IOException) {
            System.err.println("Failed to cleanup reader and writer.")
            e.printStackTrace()
        }
    }

    companion object {
        /**
         * To convert a file on the command line:
         *
         * $ java FileConvert input-file.oib output-file.ome.tiff
         * @param args Input File and Output file.
         */
        @JvmStatic
        fun main(args: Array<String>) {
            val converter = FileConvert(args[0], args[1])
            converter.convert()
        }
    }
}