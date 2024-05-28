package mx.suma.drivers.utils

import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException


object FileImageFactory {
    val TAG = "FileImageFactory"

    fun getBase64Image(file: File): String {
        try {
            val inputStream = FileInputStream(file.absolutePath)

            val bytes: ByteArray
            val buffer = ByteArray(3 * 512)
            var bytesRead = 0

            val output = ByteArrayOutputStream()

            while (bytesRead != -1) {
                if (bytesRead != 0) {
                    output.write(buffer, 0, bytesRead)
                }
                bytesRead = inputStream.read(buffer)
            }

            bytes = output.toByteArray()

            return Base64.encodeToString(bytes, Base64.NO_WRAP)

        } catch (e: IOException) {
            e.printStackTrace()
        }

        return ""
    }
}