import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import de.hdodenhof.circleimageview.CircleImageView
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

object ImageUtils {


    @Throws(IOException::class)
    fun convertImageToBase64(context: Context, image: Any?): String {
        return when (image) {
            is Bitmap -> {
                convertBitmapToBase64(image)
            }
            is Uri -> {
                convertUriToBase64(context, image)
            }
            else -> ""
        }
    }

    fun convertBitmapToBase64(bitmap: Bitmap): String {
        Log.e("convertImageToBase64","convertBitmapToBase64")
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageBytes = baos.toByteArray()
        return Base64.encodeToString(imageBytes, Base64.DEFAULT)
    }

    @Throws(IOException::class)
    fun convertUriToBase64(context: Context, uri: Uri): String {
        Log.e("convertImageToBase64","convertUriToBase64")
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val imageBitmap: Bitmap? = BitmapFactory.decodeStream(inputStream)
        return convertBitmapToBase64(imageBitmap!!)
    }
    fun compressBitmap(bitmap: Bitmap): ByteArray {
        Log.e("convertImageToBase64","compressBitmap")
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
        return stream.toByteArray()
    }
}
