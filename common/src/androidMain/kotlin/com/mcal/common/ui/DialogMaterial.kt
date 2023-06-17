package com.mcal.common.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
actual fun DialogMaterial(
    title: String?,
    subTitle: String?,
    dismissRequest: MutableState<Boolean>,
    content: @Composable () -> Unit
) {
    if (dismissRequest.value) {
        val currentContent by rememberUpdatedState(content)
        Dialog(
            onDismissRequest = {
                dismissRequest.value = false
            },
            content = {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        title?.let { Text(it, maxLines = 1) }
                        subTitle?.let { Text(it) }
                        currentContent()
                    }
                }
            })
    }
}
