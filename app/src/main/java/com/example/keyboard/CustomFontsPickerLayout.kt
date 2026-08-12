package com.example.keyboard

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Typeface
import android.view.inputmethod.InputConnection
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CustomFont
import java.io.File

@Composable
fun CustomFontsPickerLayout(
    inputConnection: InputConnection?,
    customFonts: List<CustomFont>,
    selectedCustomFont: CustomFont?,
    onSelectCustomFont: (CustomFont?) -> Unit,
    onShareStickerBitmap: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    var stickerText by remember { mutableStateOf("") }

    fun renderStickerBitmap(text: String, font: CustomFont?): Bitmap? {
        if (text.isBlank() || font == null) return null
        return try {
            val file = File(font.filePath)
            val typeface = if (file.exists()) Typeface.createFromFile(file) else Typeface.DEFAULT
            val paint = Paint().apply {
                isAntiAlias = true
                textSize = 72f
                color = AndroidColor.BLACK
                this.typeface = typeface
            }
            val textWidth = paint.measureText(text).coerceAtLeast(100f)
            val textHeight = 100f
            val bitmap = Bitmap.createBitmap((textWidth + 40).toInt(), (textHeight + 40).toInt(), Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawText(text, 20f, 80f, paint)
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Text("Custom Installed TTF Fonts", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))

        if (customFonts.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                Text("No custom TTF fonts installed. Add them in app settings!", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            }
        } else {
            LazyRow(modifier = Modifier.fillMaxWidth()) {
                items(customFonts) { font ->
                    val isSelected = selectedCustomFont?.id == font.id
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                            .clickable { onSelectCustomFont(if (isSelected) null else font) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(font.name, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Custom Font Sticker Text Input & Live 60dp Preview Bar
        OutlinedTextField(
            value = stickerText,
            onValueChange = { stickerText = it },
            label = { Text("Type text to create PNG Sticker", fontSize = 12.sp) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (stickerText.isNotBlank() && selectedCustomFont != null) {
            val bitmap = renderStickerBitmap(stickerText, selectedCustomFont)
            if (bitmap != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Sticker Preview",
                            modifier = Modifier
                                .height(52.dp)
                                .weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onShareStickerBitmap(bitmap)
                            }
                        ) {
                            Text("Copy Sticker")
                        }
                    }
                }
            }
        }
    }
}
