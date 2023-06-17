package com.mcal.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mcal.common.resources.DirectoryIcon
import com.mcal.common.resources.FileIcon
import com.mcal.common.ui.SmaliDialog
import com.mcal.common.ui.showSmaliCodeDialog
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

@Composable
fun App() {
    var dexPath by remember { mutableStateOf("C:\\Users\\timscriptov\\Desktop\\classes.dex") }


    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
//            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            TextField(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                value = dexPath,
                onValueChange = { newText -> dexPath = newText },
                placeholder = { Text("Enter apk path") },
                singleLine = true
            )

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
            Button(onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    if (dexPath.isNotEmpty()) {
                        val file = File(dexPath)
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
                Text("Show")
            }

            SmaliDialog()
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
                        if(classTree.tree.isDirectory(path)) {
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