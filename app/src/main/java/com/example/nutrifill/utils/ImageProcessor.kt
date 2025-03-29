class ImageProcessor {
    fun preprocessImage(imageBytes: ByteArray): ByteArray {
        return try {
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            
            // Check image quality
            if (bitmap.width < 480 || bitmap.height < 480) {
                throw ImageQualityException("Image resolution too low")
            }

            // Enhance image
            val matrix = ColorMatrix().apply {
                setSaturation(1.2f)  // Increase saturation
            }
            
            val enhancedBitmap = Bitmap.createBitmap(
                bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888
            )
            Canvas(enhancedBitmap).apply {
                val paint = Paint().apply {
                    colorFilter = ColorMatrixColorFilter(matrix)
                }
                drawBitmap(bitmap, 0f, 0f, paint)
            }

            // Convert back to bytes
            ByteArrayOutputStream().use { stream ->
                enhancedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                stream.toByteArray()
            }
        } catch (e: Exception) {
            throw ImageProcessingException("Failed to process image", e)
        }
    }
}