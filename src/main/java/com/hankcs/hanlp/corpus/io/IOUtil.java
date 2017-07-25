/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/9/8 23:04</create-date>
 *
 * <copyright file="Util.java" company="上海林原信息科技有限公司">
 * Copyright (c) 2003-2014, 上海林原信息科技有限公司. All Right Reserved, http://www.linrunsoft.com/
 * This source is subject to the LinrunSpace License. Please contact 上海林原信息科技有限公司 to get more information.
 * </copyright>
 */
//package com.hankcs.hanlp.corpus.io;
//
//
//import com.hankcs.hanlp.corpus.tag.Nature;
//import com.hankcs.hanlp.dictionary.WordAttribute;
//import com.hankcs.hanlp.log.HanLpLogger;
//import com.hankcs.hanlp.utility.TextUtility;
//
//import java.io.*;
//import java.util.Iterator;
//import java.util.LinkedList;
//import java.util.TreeMap;
//
///**
// * 一些常用的IO操作
// *
// * @author hankcs
// */
//public class IOUtil {

//
//    /**
//     * 反序列化对象
//     */
//    public static Object readObjectFrom(String path) {
//        ObjectInputStream ois = null;
//        try {
//            ois = new ObjectInputStream(IOUtil.newInputStream(path));
//            Object o = ois.readObject();
//            ois.close();
//            return o;
//        }
//        catch (Exception e) {
//            HanLpLogger.error(IOUtil.class, "在从" + path + "读取对象时发生异常" + e);
//        }
//
//        return null;
//    }

//    /**
//     * 一次性读入纯文本
//     *
//     * @param path
//     * @return
//     */
//    public static String readTxt(String path) {
//        if (path == null) return null;
//        try {
//            InputStream in = IOAdapter == null ? new FileInputStream(path) :
//                    IOAdapter.open(path);
//            byte[] fileContent = new byte[in.available()];
//            readBytesFromOtherInputStream(in, fileContent);
//            in.close();
//            return new String(fileContent, Charset.forName("UTF-8"));
//        }
//        catch (FileNotFoundException e) {
//            HanLpLogger.error(IOUtil.class, "找不到" + path + e);
//            return null;
//        }
//        catch (IOException e) {
//            HanLpLogger.error(IOUtil.class, "读取" + path + "发生IO异常" + e);
//            return null;
//        }
//    }

//    public static LinkedList<String[]> readCsv(String path) {
//        LinkedList<String[]> resultList = new LinkedList<String[]>();
//        LinkedList<String> lineList = readLineList(path);
//        for (String line : lineList) {
//            resultList.add(line.split(","));
//        }
//        return resultList;
//    }
//
//    public static String baseName(String path) {
//        if (CUSTOM_DICTIONARY_PATHS == null || CUSTOM_DICTIONARY_PATHS.length() == 0)
//            return "";
//        CUSTOM_DICTIONARY_PATHS = CUSTOM_DICTIONARY_PATHS.replaceAll("[/\\\\]+", "/");
//        int len = path.length(),
//                upCount = 0;
//        while (len > 0) {
//            //remove trailing separator
//            if (path.charAt(len - 1) == '/') {
//                len--;
//                if (len == 0)
//                    return "";
//            }
//            int lastInd = path.lastIndexOf('/', len - 1);
//            String fileName = path.substring(lastInd + 1, len);
//            if (fileName.equals(".")) {
//                len--;
//            }
//            else if (fileName.equals("..")) {
//                len -= 2;
//                upCount++;
//            }
//            else {
//                if (upCount == 0)
//                    return fileName;
//                upCount--;
//                len -= fileName.length();
//            }
//        }
//        return "";
//    }
//
//    private static byte[] readBytesFromFileInputStream(FileInputStream fis) throws IOException {
//        FileChannel channel = fis.getChannel();
//        int fileSize = (int) channel.size();
//        ByteBuffer byteBuffer = ByteBuffer.allocate(fileSize);
//        channel.read(byteBuffer);
//        byteBuffer.flip();
//        byte[] bytes = byteBuffer.array();
//        byteBuffer.clear();
//        channel.close();
//        fis.close();
//        return bytes;
//    }

//    /**
//     * 将非FileInputStream的某InputStream中的全部数据读入到字节数组中
//     *
//     * @param is
//     * @return
//     * @throws IOException
//     */
//    public static byte[] readBytesFromOtherInputStream(InputStream is) throws IOException {
//        ByteArrayOutputStream data = new ByteArrayOutputStream();
//
//        int readBytes;
//        byte[] buffer = new byte[Math.max(is.available(), 4096)]; // 最低4KB的缓冲区
//
//        while ((readBytes = is.read(buffer, 0, buffer.length)) != -1) {
//            data.write(buffer, 0, readBytes);
//        }
//
//        data.flush();
//
//        return data.toByteArray();
//    }

//    /**
//     * 从InputStream读取指定长度的字节出来
//     *
//     * @param is          流
//     * @param targetArray output
//     * @return 实际读取了多少字节，返回0表示遇到了文件尾部
//     * @throws IOException
//     */
//    public static int readBytesFromOtherInputStream(InputStream is, byte[] targetArray) throws IOException {
//        assert targetArray != null;
//        assert targetArray.length > 0;
//        int len;
//        int off = 0;
//        while (off < targetArray.length && (len = is.read(targetArray, off, targetArray.length - off)) != -1) {
//            off += len;
//        }
//        return off;
//    }

//    public static LinkedList<String> readLineList(String path) {
//        LinkedList<String> result = new LinkedList<String>();
//        String txt = readTxt(path);
//        if (txt == null) return result;
//        StringTokenizer tokenizer = new StringTokenizer(txt, "\n");
//        while (tokenizer.hasMoreTokens()) {
//            result.add(tokenizer.nextToken());
//        }
//
//        return result;
//    }

//    /**
//     * 用省内存的方式读取大文件
//     */
//    public static LinkedList<String> readLineListWithLessMemory(String path) {
//        LinkedList<String> result = new LinkedList<String>();
//        String line = null;
//        try {
//            BufferedReader bw = new BufferedReader(new InputStreamReader(IOUtil.newInputStream(path), "UTF-8"));
//            while ((line = bw.readLine()) != null) {
//                result.add(line);
//            }
//            bw.close();
//        }
//        catch (Exception e) {
//            HanLpLogger.error(IOUtil.class, "加载" + path + "失败，" + e);
//        }
//
//        return result;
//    }


//    /**
//     * 获取文件所在目录的路径
//     *
//     * @param path
//     * @return
//     */
//    public static String dirname(String path) {
//        int index = path.lastIndexOf('/');
//        if (index == -1) return path;
//        return path.substring(0, index + 1);
//    }
//
//    public static LineIterator readLine(String path) {
//        return new LineIterator(path);
//    }

//    /**
//     * 方便读取按行读取大文件
//     */
//    public static class LineIterator implements Iterator<String> {
//        BufferedReader bw;
//        String line;
//
//        public LineIterator(String path) {
//            try {
//                bw = new BufferedReader(new InputStreamReader(IOUtil.newInputStream(path), "UTF-8"));
//                line = bw.readLine();
//            }
//            catch (FileNotFoundException e) {
//                HanLpLogger.error(IOUtil.class, "文件" + path + "不存在，接下来的调用会返回null\n" + TextUtility.exceptionToString(e));
//                bw = null;
//            }
//            catch (IOException e) {
//                HanLpLogger.error(IOUtil.class, "在读取过程中发生错误" + TextUtility.exceptionToString(e));
//                bw = null;
//            }
//        }
//
//        public void close() {
//            if (bw == null) return;
//            try {
//                bw.close();
//                bw = null;
//            }
//            catch (IOException e) {
//                HanLpLogger.error(IOUtil.class, "关闭文件失败" + TextUtility.exceptionToString(e));
//            }
//        }
//
//        @Override
//        public boolean hasNext() {
//            if (bw == null) return false;
//            if (line == null) {
//                try {
//                    bw.close();
//                    bw = null;
//                }
//                catch (IOException e) {
//                    HanLpLogger.error(IOUtil.class, "关闭文件失败" + TextUtility.exceptionToString(e));
//                }
//                return false;
//            }
//
//            return true;
//        }
//
//        @Override
//        public String next() {
//            String preLine = line;
//            try {
//                if (bw != null) {
//                    line = bw.readLine();
//                    if (line == null && bw != null) {
//                        try {
//                            bw.close();
//                            bw = null;
//                        }
//                        catch (IOException e) {
//                            HanLpLogger.error(IOUtil.class, "关闭文件失败" + TextUtility.exceptionToString(e));
//                        }
//                    }
//                }
//                else {
//                    line = null;
//                }
//            }
//            catch (IOException e) {
//                HanLpLogger.error(IOUtil.class, "在读取过程中发生错误" + TextUtility.exceptionToString(e));
//            }
//            return preLine;
//        }
//
//        @Override
//        public void remove() {
//            throw new UnsupportedOperationException("只读，不可写！");
//        }
//    }
//
//
//    /**
//     * 创建一个BufferedReader
//     *
//     * @throws FileNotFoundException
//     * @throws UnsupportedEncodingException
//     */
//    public static BufferedReader newBufferedReader(String path) throws IOException {
//        return new BufferedReader(new InputStreamReader(IOUtil.newInputStream(path), "UTF-8"));
//    }
//
//
//    /**
//     * 创建输入流（经过IO适配器创建）
//     *
//     * @throws IOException
//     */
//    public static InputStream newInputStream(String path) throws IOException {
//        if (IOAdapter == null) return new FileInputStream(path);
//        return IOAdapter.open(path);
//    }
//
//    /**
//     * 获取最后一个分隔符的后缀
//     */
//    public static String getSuffix(String name, String delimiter) {
//        return name.substring(name.lastIndexOf(delimiter) + 1);
//    }

//    /**
//     * 写数组，用制表符分割
//     *
//     * @throws IOException
//     */
//    public static void writeLine(BufferedWriter bw, String... params) throws IOException {
//        for (int i = 0; i < params.length - 1; i++) {
//            bw.write(params[i]);
//            bw.write('\t');
//        }
//        bw.write(params[params.length - 1]);
//    }

//    /**
//     * 加载词典，词典必须遵守HanLP核心词典格式
//     *
//     * @param pathArray 词典路径，可以有任意个
//     * @return 一个储存了词条的map
//     * @throws IOException 异常表示加载失败
//     */
//    public static TreeMap<String, WordAttribute> loadDictionary(String... pathArray) throws IOException {
//        TreeMap<String, WordAttribute> map = new TreeMap<String, WordAttribute>();
//        for (String path : pathArray) {
//            BufferedReader br = new BufferedReader(new InputStreamReader(IOUtil.newInputStream(path), "UTF-8"));
//            loadDictionary(br, map);
//        }
//
//        return map;
//    }
//
//    /**
//     * 将一个BufferedReader中的词条加载到词典
//     *
//     * @param br      源
//     * @param storage 储存位置
//     * @throws IOException 异常表示加载失败
//     */
//    public static void loadDictionary(BufferedReader br, TreeMap<String, WordAttribute> storage) throws IOException {
//        String line;
//        while ((line = br.readLine()) != null) {
//            String param[] = line.split("\\s");
//            int natureCount = (param.length - 1) / 2;
//            WordAttribute attribute = new WordAttribute(natureCount);
//            for (int i = 0; i < natureCount; ++i) {
//                attribute.nature[i] = Enum.valueOf(Nature.class, param[1 + 2 * i]);
//                attribute.frequency[i] = Integer.parseInt(param[2 + 2 * i]);
//                attribute.totalFrequency += attribute.frequency[i];
//            }
//            storage.put(param[0], attribute);
//        }
//        br.close();
//    }
//
//    public static void writeCustomNature(DataOutputStream out, LinkedHashSet<Nature> customNatureCollector) throws IOException {
//        if (customNatureCollector.size() == 0) return;
//        out.writeInt(-customNatureCollector.size());
//        for (Nature nature : customNatureCollector) {
//            TextUtility.writeString(nature.toString(), out);
//        }
//    }
//}
