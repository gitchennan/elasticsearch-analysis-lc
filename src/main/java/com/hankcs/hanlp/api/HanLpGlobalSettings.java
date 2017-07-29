package com.hankcs.hanlp.api;

import com.hankcs.hanlp.log.HanLpLogger;
import org.elasticsearch.common.io.PathUtils;
import org.elasticsearch.plugin.analysis.lc.LcAnalysisPlugin;

import java.io.File;
import java.nio.file.Path;
import java.util.Properties;

/**
 * 库的全局配置，既可以用代码修改，也可以通过hanlp.properties配置（按照 变量名=值 的形式）
 */
public class HanLpGlobalSettings {
    /**
     * 核心词典路径
     */
    public static String CoreDictionaryPath = "data/dictionary/CoreNatureDictionary.txt";
    /**
     * 核心词典词性转移矩阵路径
     */
    public static String CoreDictionaryTransformMatrixDictionaryPath = "data/dictionary/CoreNatureDictionary.tr.txt";
    /**
     * 用户自定义词典路径
     */
    public static String[] CustomDictionaryPath = new String[]{
            "data/dictionary/custom/CustomDictionary.txt",
            "data/dictionary/custom/LuDictionary.txt",
            "data/dictionary/extra/HanyuExtention.txt",
            "data/dictionary/extra/Pepole.txt"
    };
    /**
     * 2元语法词典路径
     */
    public static String BiGramDictionaryPath = "data/dictionary/CoreNatureDictionary.ngram.txt";

    /**
     * 停用词词典路径
     */
    public static String CoreStopWordDictionaryPath = "data/dictionary/stopwords.txt";
    /**
     * 同义词词典路径
     */
    public static String CoreSynonymDictionaryDictionaryPath = "data/dictionary/synonym/CoreSynonym.txt";
    /**
     * 人名词典路径
     */
    public static String PersonDictionaryPath = "data/dictionary/person/nr.txt";
    /**
     * 人名词典转移矩阵路径
     */
    public static String PersonDictionaryTrPath = "data/dictionary/person/nr.tr.txt";
    /**
     * 地名词典路径
     */
    public static String PlaceDictionaryPath = "data/dictionary/place/ns.txt";
    /**
     * 地名词典转移矩阵路径
     */
    public static String PlaceDictionaryTrPath = "data/dictionary/place/ns.tr.txt";
    /**
     * 地名词典路径
     */
    public static String OrganizationDictionaryPath = "data/dictionary/organization/nt.txt";
    /**
     * 地名词典转移矩阵路径
     */
    public static String OrganizationDictionaryTrPath = "data/dictionary/organization/nt.tr.txt";
    /**
     * 简繁转换词典根目录
     */
    public static String tcDictionaryRoot = "data/dictionary/tc/";
    /**
     * 声母韵母语调词典
     */
    public static String SYTDictionaryPath = "data/dictionary/pinyin/SYTDictionary.txt";

    /**
     * 拼音词典路径
     */
    public static String PinyinDictionaryPath = "data/dictionary/pinyin/pinyin.txt";

    /**
     * 音译人名词典
     */
    public static String TranslatedPersonDictionaryPath = "data/dictionary/person/nrf.txt";

    /**
     * 日本人名词典路径
     */
    public static String JapanesePersonDictionaryPath = "data/dictionary/person/nrj.txt";

    /**
     * 字符类型对应表
     */
//        public static String CharTypePath = "data/dictionary/other/CharType.bin";

    /**
     * 字符正规化表（全角转半角，繁体转简体）
     */
    public static String CharTablePath = "data/dictionary/other/CharTable.txt";

    /**
     * 词-词性-依存关系模型
     */
    public static String WordNatureModelPath = "data/model/dependency/WordNature.txt";

    /**
     * 最大熵-依存关系模型
     */
    public static String MaxEntModelPath = "data/model/dependency/MaxEntModel.txt";
    /**
     * 神经网络依存模型路径
     */
    public static String NNParserModelPath = "data/model/dependency/NNParserModel.txt";
    /**
     * CRF分词模型
     */
    public static String CRFSegmentModelPath = "data/model/segment/CRFSegmentModel.txt";
    /**
     * HMM分词模型
     */
//        public static String HMMSegmentModelPath = "data/model/segment/HMMSegmentModel.bin";
    /**
     * CRF依存模型
     */
    public static String CRFDependencyModelPath = "data/model/dependency/CRFDependencyModelMini.txt";
    /**
     * 分词结果是否展示词性
     */
    public static boolean ShowTermNature = true;
    /**
     * 是否执行字符正规化（繁体->简体，全角->半角，大写->小写），切换配置后必须删CustomDictionary.txt.bin缓存
     */
    public static boolean Normalization = false;

    static {
        // 自动读取配置
        Properties p = new Properties();
        try {
            String pluginDir = LcAnalysisPlugin.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            Path pluginConfigDir = PathUtils.get(new File(pluginDir).getParent(), "config").toAbsolutePath();

            String root = pluginConfigDir.toString();
            if (!pluginConfigDir.toFile().exists()) {
                root = "config/";
            }

            if (root.length() > 0 && !root.endsWith("/")) {
                root += "/";
            }

            CoreDictionaryPath = root + p.getProperty("CoreDictionaryPath", CoreDictionaryPath);
            CoreDictionaryTransformMatrixDictionaryPath = root + p.getProperty("CoreDictionaryTransformMatrixDictionaryPath", CoreDictionaryTransformMatrixDictionaryPath);
            BiGramDictionaryPath = root + p.getProperty("BiGramDictionaryPath", BiGramDictionaryPath);
            CoreStopWordDictionaryPath = root + p.getProperty("CoreStopWordDictionaryPath", CoreStopWordDictionaryPath);
            CoreSynonymDictionaryDictionaryPath = root + p.getProperty("CoreSynonymDictionaryDictionaryPath", CoreSynonymDictionaryDictionaryPath);
            PersonDictionaryPath = root + p.getProperty("PersonDictionaryPath", PersonDictionaryPath);
            PersonDictionaryTrPath = root + p.getProperty("PersonDictionaryTrPath", PersonDictionaryTrPath);

            String[] newCustomDictionaryPath = new String[CustomDictionaryPath.length];
            for (int idx = 0; idx < CustomDictionaryPath.length; idx ++) {
                newCustomDictionaryPath[idx] = root + CustomDictionaryPath[idx];
            }
            CustomDictionaryPath = newCustomDictionaryPath;

            tcDictionaryRoot = root + p.getProperty("tcDictionaryRoot", tcDictionaryRoot);
            if (!tcDictionaryRoot.endsWith("/")) tcDictionaryRoot += '/';
            SYTDictionaryPath = root + p.getProperty("SYTDictionaryPath", SYTDictionaryPath);
            PinyinDictionaryPath = root + p.getProperty("PinyinDictionaryPath", PinyinDictionaryPath);
            TranslatedPersonDictionaryPath = root + p.getProperty("TranslatedPersonDictionaryPath", TranslatedPersonDictionaryPath);
            JapanesePersonDictionaryPath = root + p.getProperty("JapanesePersonDictionaryPath", JapanesePersonDictionaryPath);
            PlaceDictionaryPath = root + p.getProperty("PlaceDictionaryPath", PlaceDictionaryPath);
            PlaceDictionaryTrPath = root + p.getProperty("PlaceDictionaryTrPath", PlaceDictionaryTrPath);
            OrganizationDictionaryPath = root + p.getProperty("OrganizationDictionaryPath", OrganizationDictionaryPath);
            OrganizationDictionaryTrPath = root + p.getProperty("OrganizationDictionaryTrPath", OrganizationDictionaryTrPath);
//                CharTypePath = root + p.getProperty("CharTypePath", CharTypePath);
            CharTablePath = root + p.getProperty("CharTablePath", CharTablePath);
            WordNatureModelPath = root + p.getProperty("WordNatureModelPath", WordNatureModelPath);
            MaxEntModelPath = root + p.getProperty("MaxEntModelPath", MaxEntModelPath);
            NNParserModelPath = root + p.getProperty("NNParserModelPath", NNParserModelPath);
            CRFSegmentModelPath = root + p.getProperty("CRFSegmentModelPath", CRFSegmentModelPath);
            CRFDependencyModelPath = root + p.getProperty("CRFDependencyModelPath", CRFDependencyModelPath);
//                HMMSegmentModelPath = root + p.getProperty("HMMSegmentModelPath", HMMSegmentModelPath);
            ShowTermNature = "true".equals(p.getProperty("ShowTermNature", "true"));
            Normalization = "true".equals(p.getProperty("Normalization", "false"));
        }
        catch (Exception e) {
            HanLpLogger.error(HanLP.class, "Failed to load hanLp settings", e);
        }
    }
}