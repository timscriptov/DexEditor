package com.mcal.common.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mcal.common.ClassItem
import com.mcal.common.classList
import com.mcal.common.classTree
import com.mcal.common.currenPath
import com.mcal.dexlib.ClassTree
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

var smaliDialogState = mutableStateOf(false)
var smaliFilePath = mutableStateOf("")
var smaliCode = mutableStateOf("")

fun showSmaliCodeDialog(path: String, code: String) {
    smaliDialogState.value = true
    smaliFilePath.value = path
    smaliCode.value = code
}

@Composable
fun SmaliDialog() {
    DialogMaterial(
        dismissRequest = smaliDialogState,
        title = smaliFilePath.value,
        subTitle = "",
        content = {
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = smaliCode.value,
                onValueChange = { newText -> smaliCode.value = newText },
                placeholder = { Text("") }
            )
            Button(onClick = {
                CoroutineScope(Dispatchers.IO).launch {
//                   classTree?.saveDexFile()
                }
            }) {
                Text("Show")
            }
        })
}