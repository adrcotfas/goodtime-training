package goodtime.training.wod.timer.common

import java.io.*

object FileUtils {
    @Throws(IOException::class)
    fun copyFile(sourceFile: File, destFile: File) {
        if (!destFile.parentFile.exists()) {
            destFile.parentFile.mkdirs()
        }
        if (!destFile.exists()) {
            destFile.createNewFile()
        }
        FileInputStream(sourceFile).channel.use { source ->
            FileOutputStream(destFile).channel.use { destination ->
                destination.transferFrom(
                    source,
                    0,
                    source.size()
                )
            }
        }
    }

    @Throws(IOException::class)
    fun copy(inStream: InputStream, dst: File) {
        val outStream = FileOutputStream(dst)
        copy(inStream, outStream)
    }

    @Throws(IOException::class)
    private fun copy(stream: InputStream, out: OutputStream) {
        var numBytes: Int
        val buffer = ByteArray(1024)
        while (stream.read(buffer).also { numBytes = it } != -1) {
            out.write(buffer, 0, numBytes)
        }
        out.close()
    }

    @Throws(IOException::class)
    fun isSQLite3File(file: File): Boolean {
        val fis = FileInputStream(file)
        val header = "SQLite format 3".toByteArray()
        val buffer = ByteArray(header.size)
        val count = fis.read(buffer)
        return if (count < header.size) false else buffer.contentEquals(header)
    }
}