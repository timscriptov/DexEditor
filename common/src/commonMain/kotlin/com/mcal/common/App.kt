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
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.dp
import com.mcal.common.utils.JaDXHelper
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
var showSmaliCode = mutableStateOf(true)
val openedFiles = mutableStateListOf<ClassItem>()

val javaCode = mutableStateOf("")
var smaliCode = mutableStateOf("")

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
                val smaliName = item.name
                val tabName = "$smaliName.smali"

                Button(
                    modifier = Modifier.fillMaxWidth().padding(start = 2.dp, bottom = 2.dp),
                    onClick = {
                        smaliCode.value = classTree!!.getSmali(item.classDef!!)
                        javaCode.value = JaDXHelper.smali2java(smaliCode.value)
                    }
                ) {
                    Text(tabName)
                }
            })
        }
        if (showSmaliCode.value) {
            TextField(
                modifier = Modifier.fillMaxSize().weight(1f),
                value = smaliCode.value,
                onValueChange = { newText -> smaliCode.value = newText },
                placeholder = { Text("") }
            )
        } else {
            TextField(
                modifier = Modifier.fillMaxSize().weight(1f),
                value = javaCode.value,
                onValueChange = { newText -> javaCode.value = newText },
                placeholder = { Text("") },
                enabled = false
            )
        }
        LazyRow(modifier = Modifier.fillMaxWidth()) {
            item {
                Button(
                    modifier = Modifier.fillMaxWidth().padding(start = 2.dp, bottom = 2.dp),
                    onClick = {
                        showSmaliCode.value = true
                    }
                ) {
                    Text("Smali")
                }
            }
            item {
                Button(
                    modifier = Modifier.fillMaxWidth().padding(start = 2.dp, bottom = 2.dp),
                    onClick = {
                        showSmaliCode.value = false
                    }
                ) {
                    Text("Java")
                }
            }
        }
    }
}

@Composable
fun PackageList() {
    Text(currenPath.value)

    if (currenPath.value.isNotEmpty()) {
        classTree?.let {
            BackItem(it)
        }
    }

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
            }
        ) {
            Text("Decode")
        }
    }
}

@Composable
fun FileItem(classTree: ClassTree, item: ClassItem) {
    Card(
        modifier = Modifier.fillMaxWidth()
            .padding(top = 2.dp, bottom = 2.dp)
            .selectable(
                selected = true,
                onClick = {
                    openedFiles.add(item)
                    smaliCode.value = classTree.getSmali(item.classDef!!)
                    javaCode.value = JaDXHelper.smali2java(smaliCode.value)
                }
            ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                bitmap = useResource("ic_file.png") { loadImageBitmap(it) },
                contentDescription = "File",
                tint = Color.Blue
            )
            Text(modifier = Modifier.weight(1f), text = item.name + ".smali")
        }
    }
}

@Composable
fun DirectoryItem(classTree: ClassTree, item: ClassItem) {
    Card(
        modifier = Modifier.fillMaxWidth()
            .padding(top = 2.dp, bottom = 2.dp)
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
                bitmap = useResource("ic_folder.png") { loadImageBitmap(it) },
                contentDescription = "Directory",
                tint = Color.Black
            )
            Text(modifier = Modifier.weight(1f), text = item.name)
        }
    }
}

@Composable
fun BackItem(classTree: ClassTree) {
    Card(
        modifier = Modifier.fillMaxWidth()
            .padding(top = 2.dp, bottom = 2.dp)
            .selectable(
                selected = true,
                onClick = {
                    val list = classTree.getList("../")
                    val curPath = classTree.tree.curPath
                    currenPath.value = curPath
                    if (classList.isNotEmpty()) {
                        classList.clear()
                    }
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
                modifier = Modifier.padding(end = 8.dp),
                bitmap = useResource("ic_folder.png") { loadImageBitmap(it) },
                contentDescription = "Directory",
                tint = Color.Black
            )
            Text(modifier = Modifier.weight(1f), text = "..")
        }
    }
}