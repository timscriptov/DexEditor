package com.mcal.common.resources

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

    val DirectoryIcon =
        makeIconFromXMLPath(pathData = arrayListOf("M160,800Q127,800 103.5,776.5Q80,753 80,720L80,240Q80,207 103.5,183.5Q127,160 160,160L400,160L480,240L800,240Q833,240 856.5,263.5Q880,287 880,320L880,720Q880,753 856.5,776.5Q833,800 800,800L160,800Z"))
    val FileIcon =
        makeIconFromXMLPath(pathData = arrayListOf("M240,880Q207,880 183.5,856.5Q160,833 160,800L160,160Q160,127 183.5,103.5Q207,80 240,80L560,80L800,320L800,800Q800,833 776.5,856.5Q753,880 720,880L240,880ZM520,360L720,360L520,160L520,360Z"))
    private fun makeIconFromXMLPath(
        pathData: List<String>,
        viewportWidth: Float = 24f,
        viewportHeight: Float = 24f,
        defaultWidth: Dp = 24.dp,
        defaultHeight: Dp = 24.dp,
        fillColor: Color = Color.White,
    ): ImageVector {
        val fillBrush = SolidColor(fillColor)
        val strokeBrush = SolidColor(fillColor)

        return ImageVector.Builder(
            defaultWidth = defaultWidth,
            defaultHeight = defaultHeight,
            viewportWidth = viewportWidth,
            viewportHeight = viewportHeight,
        ).run {
            pathData.forEach { path ->
                addPath(
                    pathData = addPathNodes(path),
                    name = "",
                    fill = fillBrush,
                    stroke = strokeBrush,
                )
            }

            build()
        }
    }
