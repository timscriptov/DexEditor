package com.mcal.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mcal.common.resources.DirectoryIcon
import com.mcal.common.resources.FileIcon
import com.mcal.common.ui.showSmaliCodeDialog
import com.mcal.common.ui.smaliCode
import com.mcal.dexlib.ClassTree
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jf.dexlib2.iface.ClassDef
import java.io.File

class ClassItem(val name: String, val classDef: ClassDef?)

var classTree: ClassTree? = null
val classList = mutableStateListOf<ClassItem>()
var currenPath = mutableStateOf("")
var dexPath = mutableStateOf("C:\\Users\\timscriptov\\Desktop\\classes.dex")

val openedFiles = mutableStateListOf<ClassItem>()

@Composable
fun App() {
    MaterialTheme {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier.weight(0.5f).padding(end = 8.dp)
            ) {
                InputFile()
                PackageList()
            }
            CodeEditor(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun CodeEditor(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
    ) {
        LazyRow(modifier = Modifier.fillMaxWidth()) {
            items(items = openedFiles, itemContent = { item ->
                val tabName = item.name + ".smali"
                Card(
                    modifier = Modifier
                        .padding(8.dp)
                        .selectable(
                            selected = true,
                            onClick = {
                                showSmaliCodeDialog(tabName, classTree!!.getSmali(item.classDef!!))
                            }
                        ),
                ) {
                    Text(modifier = Modifier.padding(8.dp), text = tabName)
                }
            })
        }
        TextField(
            modifier = Modifier.fillMaxSize().weight(1f),
            value = smaliCode.value,
            onValueChange = { newText -> smaliCode.value = newText },
            placeholder = { Text("") }
        )
    }
}

@Composable
fun PackageList() {
    Text(currenPath.value)
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        classList.forEach { item ->
            classTree?.let { classTree ->
                if (classTree.tree.isDirectory(item.name)) {
                    item {
                        DirectoryItem(classTree, item)
                    }
                } else {
                    item {
                        FileItem(classTree, item)
                    }
                }
            }
        }
    }
}

@Composable
fun InputFile(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
    ) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = dexPath.value,
            onValueChange = { newText -> dexPath.value = newText },
            placeholder = { Text("Enter dex file path") },
            singleLine = true
        )
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    if (dexPath.value.isNotEmpty()) {
                        val file = File(dexPath.value)
                        if (file.exists()) {
                            classTree = ClassTree(file).also { classTree ->
                                if (classList.isNotEmpty()) {
                                    classList.clear()
                                }
                                val list = classTree.getList("/")
                                val curPath = classTree.tree.curPath
                                currenPath.value = curPath
                                for (i in list.indices) {
                                    val path = list[i]
                                    if (classTree.tree.isDirectory(path)) {
                                        classList.add(ClassItem(path, null))
                                    } else {
                                        classList.add(ClassItem(path, classTree.classMap[curPath + path]))
                                    }
                                }
                            }
                        }
                    }
                }
            }) {
            Text("Decode")
        }
    }
}

@Composable
fun FileItem(classTree: ClassTree, item: ClassItem) {
    Card(
        modifier = Modifier.fillMaxWidth()
            .padding(8.dp)
            .selectable(
                selected = true,
                onClick = {
                    openedFiles.add(item)
                    showSmaliCodeDialog(item.name + ".smali", classTree.getSmali(item.classDef!!))
                }
            ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = FileIcon,
                contentDescription = "File",
                tint = Color.Black
            )
            Text(modifier = Modifier.weight(1f), text = item.name + ".smali")
        }
    }
}

@Composable
fun DirectoryItem(classTree: ClassTree, item: ClassItem) {
    Card(
        modifier = Modifier.fillMaxWidth()
            .padding(8.dp)
            .selectable(
                selected = true,
                onClick = {
                    if (classList.isNotEmpty()) {
                        classList.clear()
                    }
                    val name = item.name
                    val list = classTree.getList(name)

                    val curPath = classTree.tree.curPath
                    currenPath.value = curPath
                    for (i in list.indices) {
                        val path = list[i]
                        if (classTree.tree.isDirectory(path)) {
                            classList.add(ClassItem(path, null))
                        } else {
                            classList.add(ClassItem(path, classTree.classMap[curPath + path]))
                        }
                    }
                }
            ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = DirectoryIcon,
                contentDescription = "Directory",
                tint = Color.Black
            )
            Text(modifier = Modifier.weight(1f), text = item.name)
        }
    }
}