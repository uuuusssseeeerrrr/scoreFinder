package com.score.image

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Image
import java.awt.Rectangle
import java.awt.RenderingHints
import java.awt.Robot
import java.awt.Toolkit
import java.awt.image.BufferedImage
import java.awt.image.ConvolveOp
import java.awt.image.Kernel
import java.io.File
import javax.imageio.ImageIO

object ImageCapture {
    private val appDataPath = System.getenv("LOCALAPPDATA") ?: System.getProperty("user.home")
    private val targetFolder by lazy {
        File(appDataPath, "aion2scf").apply {
            if (!exists()) mkdirs() // 폴더 생성을 초기화 시점에 포함 가능
        }
    }

    suspend fun titleCapture(): File {
        val titleImageFile = File(targetFolder, "title.png")
        val scoreRegion = Rectangle(0, 0, 200, 100)

        val robot = Robot()
        withContext(Dispatchers.IO) {
            val capture = robot.createScreenCapture(scoreRegion)
            ImageIO.write(capture, "png", titleImageFile)
        }

        return titleImageFile
    }

    suspend fun partyCapture(title: String): File {
        val partyImageFile = File(targetFolder, "partyMember.png")
        val windowMetrics = Toolkit.getDefaultToolkit().screenSize

        val scoreRegion = when {
            windowMetrics.width > 2200 && title == "성역" -> Rectangle(700, 300, 750, windowMetrics.height)
            windowMetrics.width > 2200 && title == "파티" -> Rectangle(800, 300, 700, windowMetrics.height)
            else -> Rectangle(1000, 0, 400, windowMetrics.height)
        }

        val robot = Robot()
        withContext(Dispatchers.IO) {
            val capture = robot.createScreenCapture(scoreRegion)

            // 1. 확대
            var processed = upscale(capture, 1.2)

            // 2. 샤프닝
            processed = sharpen(processed)

            ImageIO.write(processed, "png", partyImageFile)
        }

        return partyImageFile
    }

    // 배율 지정 가능
    fun upscale(image: BufferedImage, scale: Double): BufferedImage {
        val newWidth = (image.width * scale).toInt()
        val newHeight = (image.height * scale).toInt()

        val scaled = BufferedImage(newWidth, newHeight, image.type)
        val graphics = scaled.createGraphics()

        // 고품질 렌더링 설정
        graphics.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BICUBIC
        )
        graphics.setRenderingHint(
            RenderingHints.KEY_RENDERING,
            RenderingHints.VALUE_RENDER_QUALITY
        )
        graphics.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        )

        graphics.drawImage(
            image.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH),
            0, 0, null
        )

        graphics.dispose()
        return scaled
    }

    // 샤프닝 (선명도 증가)
    private fun sharpen(image: BufferedImage): BufferedImage {
        val kernel = floatArrayOf(
            0f, -1f, 0f,
            -1f, 5f, -1f,
            0f, -1f, 0f
        )

        val convolveOp = ConvolveOp(
            Kernel(3, 3, kernel),
            ConvolveOp.EDGE_NO_OP,
            null
        )

        return convolveOp.filter(image, null)
    }
}
