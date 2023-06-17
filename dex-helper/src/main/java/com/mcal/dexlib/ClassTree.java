package com.mcal.dexlib;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jf.baksmali.Adaptors.ClassDefinition;
import org.jf.baksmali.BaksmaliOptions;
import org.jf.baksmali.formatter.BaksmaliWriter;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.writer.builder.BuilderField;
import org.jf.dexlib2.writer.builder.BuilderMethod;
import org.jf.dexlib2.writer.builder.DexBuilder;
import org.jf.dexlib2.writer.io.MemoryDataStore;
import org.jf.dexlib2.writer.pool.DexPool;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClassTree {
    public Tree tree;
    public HashMap<String, ClassDef> classMap;
    public DexBackedDexFile dexFile;
    public ClassDef curClassDef;

    public int dep;
    public Stack<String> path;

    public String curFile;
    public List<ClassDef> classDefList = new ArrayList<>();
    @NotNull
    public File mFile;
    byte[] data;

    byte[] input;

    public ClassTree(@NotNull File file) throws Exception {
        this.mFile = file;

        initDex();
    }

    private void initDex() throws Exception {
        input = read(mFile);
        dexFile = DexBackedDexFile.fromInputStream(Opcodes.getDefault(), new ByteArrayInputStream(input));
        classDefList.addAll(dexFile.getClasses());
        initClassMap();
    }

    private void initClassMap() {
        if (classMap == null) {
            classMap = new HashMap<>();
        } else {
            classMap.clear();
        }

        for (int i = 0; i < classDefList.size(); i++) {
            ClassDef classDef = classDefList.get(i);
            String type = classDef.getType();
            type = type.substring(1, type.length() - 1);
            classMap.put(type, classDef);
        }

        tree = null;
        tree = new Tree(classMap);
    }

    public ArrayList<String> getList(@NotNull String dir) {
        if (dir.equals("/")) {
            initClassMap();
            return tree.list();
        }

        if (dir.equals("../")) {
            tree.pop();
            return tree.list();
        }

        if (!dir.endsWith("/")) {
            dir += "/";
        }

        tree.push(dir);

        return tree.list();
    }

    public void setCurrnetClass(@NotNull String className) {
        curClassDef = classMap.get(className);
    }

    public void removeClass(@NotNull String className) {
        {
            Iterator<String> classIterator = classMap.keySet().iterator();
            while (classIterator.hasNext()) {
                ClassDef classDef = classMap.get(classIterator.next());
                if (classDef != null) {
                    String type = classDef.getType();
                    type = type.substring(1, type.length() - 1);

                    if (tree.isDirectory(className)) {
                        if (type.startsWith(className)) {
                            classIterator.remove();
                        }
                    } else {
                        if (type.equals(className)) {
                            classIterator.remove();
                        }
                    }
                }
            }
        }
        {
            Iterator<ClassDef> classIterator = classDefList.iterator();
            while (classIterator.hasNext()) {
                ClassDef classDef = classIterator.next();
                String type = classDef.getType();
                type = type.substring(1, type.length() - 1);

                if (tree.isDirectory(className)) {
                    if (type.startsWith(className)) {
                        classIterator.remove();
                    }
                } else {
                    if (type.equals(className)) {
                        classIterator.remove();
                    }
                }
            }
        }
        initClassMap();
    }

    public void clearAll() {
        if (classMap != null) {
            classMap.clear();
        }
        classMap = null;
        path = null;
        dexFile = null;
        curClassDef = null;
        tree = null;
        curFile = null;
        System.gc();
    }

    public List<ClassDef> searchClass(@NotNull String content, @NotNull SearchProgress searchProgress) {
        List<ClassDef> result = new ArrayList<>();

        int total = classMap.size();
        int i = 0;

        for (String s : classMap.keySet()) {
            ClassDef classDef = classMap.get(s);
            if (classDef != null) {
                String type = classDef.getType();
                if (type.contains(content)) {
                    result.add(classDef);
                }
            }
            i++;
            searchProgress.onProgress(i, total);
        }
        return result;
    }


    public List<Method> searchMethod(@NotNull String content, @NotNull SearchProgress searchProgress) {
        List<Method> result = new ArrayList<>();

        int total = classMap.size();
        int i = 0;

        for (String s : classMap.keySet()) {
            ClassDef classDef = classMap.get(s);
            if (classDef != null) {
                for (Method method : classDef.getMethods()) {
                    String name = method.getName();
                    if (name.contains(content)) {
                        result.add(method);
                    }
                }
            }
            i++;
            searchProgress.onProgress(i, total);
        }
        return result;
    }

    public List<ClassDef> searchCode(@NotNull String content, @NotNull SearchProgress searchProgress) {
        List<ClassDef> result = new ArrayList<>();

        int total = classMap.size();
        int i = 0;

        for (String s : classMap.keySet()) {
            ClassDef classDef = classMap.get(s);
            if (classDef != null) {
                String type = classDef.getType();
                String code = getSmali(classDef);
                if (code.contains(content)) {
                    result.add(classDef);
                }
            }
            i++;
            searchProgress.onProgress(i, total);
        }
        return result;
    }


    public List<ClassDef> searchString(@NotNull String content, @NotNull SearchProgress searchProgress) {
        List<ClassDef> result = new ArrayList<>();

        int total = classMap.size();
        int i = 0;

        for (String s : classMap.keySet()) {
            ClassDef classDef = classMap.get(s);
            if (classDef != null) {
                String type = classDef.getType();
                String code = getSmali(classDef);

                Pattern p = Pattern.compile("(const-string\\s.([0-9]*),\\s\"(.*)\"\n)");
                Matcher m = p.matcher(code);
                while (m.find()) {
                    String str = m.group(3);
                    if (str != null && str.contains(content)) {
                        result.add(classDef);
                    }
                }
            }
            i++;
            searchProgress.onProgress(i, total);
        }
        return result;
    }

    public String getSmali(@NotNull ClassDef classDef) {
        String code = null;
        try {
            StringWriter stringWriter = new StringWriter();
            BaksmaliWriter writer = new BaksmaliWriter(stringWriter);
            ClassDefinition classDefinition = new ClassDefinition(new BaksmaliOptions(), classDef);
            classDefinition.writeTo(writer);
            writer.close();
            code = stringWriter.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return code;
    }

    public void saveClassDef(@NotNull ClassDef classDef) {
        DexPool dexpool = new DexPool(Opcodes.getDefault());
        dexpool.internClass(classDef);

        String type = classDef.getType();
        type = type.substring(1, type.length() - 1);
        classMap.remove(type);
        classMap.put(type, classDef);
    }

    public void saveDexFile(String destPath) {
        DexPool dexpool = new DexPool(Opcodes.getDefault());
        try {
            int index = 0;
            for (String type : classMap.keySet()) {
                ClassDef classDef = classMap.get(type);
                dexpool.internClass(classDef);
                index++;
            }

            MemoryDataStore memoryDataStore = new MemoryDataStore();
            dexpool.writeTo(memoryDataStore);
            byte[] result = Arrays.copyOf(memoryDataStore.getBuffer(), memoryDataStore.getSize());

            File file = new File(destPath);

            if (file.renameTo(file)) {
                file. delete();
                saveFile(result, destPath);
            }
            data = result;
//            isChanged = false;
//            isSaved = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void saveDexFile(@NotNull String destPath, @NotNull DexSaveProgress dexSaveProgress) {
        DexBuilder dexbuilder = new DexBuilder(Opcodes.getDefault());

        ArrayList<BuilderField> fields = new ArrayList<>();
        ArrayList<BuilderMethod> methods = new ArrayList<>();

        try {
            int index = 0;
            for (String type : classMap.keySet()) {
                ClassDef classDef = classMap.get(type);
                List<BuilderField> builderFields = new ArrayList<>();
                if (classDef != null) {
                    for (Field field : classDef.getFields()) {
                        try {
                            BuilderField builderField = dexbuilder.internField(field.getDefiningClass(), field.getName(), field.getType(), field.getAccessFlags(),
                                    field.getInitialValue(), field.getAnnotations(), null);
                            fields.add(builderField);
                        } catch (RuntimeException e) {
                            System.err.println(classDef.getType());
                            e.printStackTrace();
                        }
                    }
                    // Method
                    for (Method method : classDef.getMethods()) {
                        try {
                            BuilderMethod builderMethod = dexbuilder.internMethod(method.getDefiningClass(), method.getName(), method.getParameters(), method.getReturnType(),
                                    method.getAccessFlags(), method.getAnnotations(), null, method.getImplementation());
                            methods.add(builderMethod);
                        } catch (RuntimeException e) {
                            System.err.println(classDef.getType());
                            e.printStackTrace();
                        }
                    }
                    dexbuilder.internClassDef(classDef.getType(), classDef.getAccessFlags(),
                            classDef.getSuperclass(), classDef.getInterfaces(), classDef.getSourceFile(),
                            classDef.getAnnotations(), fields, methods);
                }
            }
            index++;
            dexSaveProgress.onMessage("Outputting file...");
            MemoryDataStore memoryDataStore = new MemoryDataStore();
            dexbuilder.writeTo(memoryDataStore);
            byte[] result = Arrays.copyOf(memoryDataStore.getBuffer(), memoryDataStore.getSize());

            File file = new File(destPath);

            if (file.renameTo(file)) {
                file.delete();
                saveFile(result, destPath);
            }
            data = result;
//            isChanged = false;
//            isSaved = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveFile(@NotNull byte[] bfile, @NotNull String filePath) throws Exception {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;

        File dir = new File(filePath);
        if (!dir.exists() && dir.isDirectory()) {//判断文件目录是否存在
            dir.mkdirs();
        }
        file = new File(filePath);
        fos = new FileOutputStream(file);
        bos = new BufferedOutputStream(fos);
        bos.write(bfile);
        if (bos != null) {
            bos.close();
        }
        if (fos != null) {
            fos.close();
        }
    }

    @NotNull
    public byte[] read(@NotNull File fileName) throws IOException {
        InputStream is = new FileInputStream(fileName);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[is.available()];
        int n = 0;
        while ((n = is.read(buffer)) != -1) {
            bos.write(buffer, 0, n);
        }
        bos.close();
        is.close();
        return bos.toByteArray();
    }

    public void writeDexFile(@NotNull final List<ClassDef> classes, @NotNull String outDex, final int API) throws IOException {
        Collections.sort(classes);
        DexFileFactory.writeDexFile(outDex, new DexFile() {
            @NotNull
            @Override
            public Set<? extends ClassDef> getClasses() {
                return new AbstractSet<>() {
                    @Override
                    public Iterator<ClassDef> iterator() {
                        return classes.iterator();
                    }

                    @Override
                    public int size() {
                        return classes.size();
                    }
                };
            }

            @NotNull
            @Override
            public Opcodes getOpcodes() {
                return Opcodes.forApi(API);
            }
        });
    }

    public interface DexSaveProgress {
        void onProgress(int progress, int total);

        void onMessage(String name);
    }

    public interface SearchProgress {
        void onProgress(int progress, int total);
    }

    public class Tree {
        private final List<Map<String, String>> node;
        private final Comparator<String> sortByType = (a, b) -> {
            if (isDirectory(a) && !isDirectory(b)) {
                return -1;
            }
            if (!isDirectory(a) && isDirectory(b)) {
                return 1;
            }
            return a.toLowerCase().compareTo(b.toLowerCase());
        };

        public Tree(HashMap<String, ClassDef> classMap) {
            if (path == null) {
                path = new Stack<>();
                dep = 0;
            }
            Set<String> names = classMap.keySet();

            node = new ArrayList<>();
            for (String name : names) {
                String[] token = name.split("/");
                String tmp = "";
                for (int i = 0, len = token.length; i < len; i++) {
                    String value = token[i];
                    if (i >= node.size()) {
                        Map<String, String> map = new HashMap<>();
                        if (classMap.containsKey(tmp + value)
                                && i + 1 == len) {
                            map.put(tmp + value, tmp);
                        } else {
                            map.put(tmp + value + "/", tmp);
                        }
                        node.add(map);
                        tmp += value + "/";
                    } else {
                        Map<String, String> map = node.get(i);
                        if (classMap.containsKey(tmp + value)
                                && i + 1 == len) {
                            map.put(tmp + value, tmp);
                        } else {
                            map.put(tmp + value + "/", tmp);
                        }
                        tmp += value + "/";
                    }
                }
            }
        }

        public ArrayList<String> list(String parent) {
            Map<String, String> map = null;
            ArrayList<String> str = new ArrayList<>();
            while (dep >= 0 && node.size() > 0) {
                map = node.get(dep);
                if (map != null) {
                    break;
                }
                pop();
            }
            if (map == null) {
                return str;
            }
            for (String key : map.keySet()) {
                if (parent.equals(map.get(key))) {
                    int index;
                    if (key.endsWith("/")) {
                        index = key.lastIndexOf("/", key.length() - 2);
                    } else {
                        index = key.lastIndexOf("/");
                    }
                    if (index != -1)
                        key = key.substring(index + 1);
                    str.add(key);
                }
            }
            str.sort(sortByType);
            return str;
        }

        public ArrayList<String> list() {
            return list(getCurPath());
        }

        private void push(String name) {
            dep++;
            path.push(name);
        }

        @Nullable
        private String pop() {
            if (dep > 0) {
                dep--;
                return path.pop();
            }
            return null;
        }

        public String getCurPath() {
            return join(path, "/");
        }

        public boolean isDirectory(@NotNull String name) {
            return name.endsWith("/");
        }

        @NotNull
        private String join(@NotNull Stack<String> stack, String d) {
            StringBuilder sb = new StringBuilder();
            for (String s : stack) {
                sb.append(s);
            }
            return sb.toString();
        }
    }
}
