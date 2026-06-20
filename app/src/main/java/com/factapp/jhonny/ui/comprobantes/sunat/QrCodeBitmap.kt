package com.factapp.jhonny.ui.comprobantes.sunat

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

object QrCodeBitmap {

    fun encode(content: String, sizePx: Int = 400): Bitmap? {
        if (content.isBlank()) return null
        return runCatching {
            val hints = mapOf(
                EncodeHintType.CHARACTER_SET to "UTF-8",
                EncodeHintType.MARGIN to 1,
            )
            val matrix = QRCodeWriter().encode(
                content,
                BarcodeFormat.QR_CODE,
                sizePx,
                sizePx,
                hints,
            )
            val w = matrix.width
            val h = matrix.height
            Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565).apply {
                for (x in 0 until w) {
                    for (y in 0 until h) {
                        setPixel(x, y, if (matrix.get(x, y)) Color.BLACK else Color.WHITE)
                    }
                }
            }
        }.getOrNull()
    }
}
