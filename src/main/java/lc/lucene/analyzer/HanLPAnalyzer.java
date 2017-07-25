package lc.lucene.analyzer;

import com.hankcs.hanlp.api.HanLP;
import com.hankcs.hanlp.seg.Segment;
import lc.lucene.tokenizer.HanLPTokenizer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;

import java.util.Set;

public class HanLPAnalyzer extends Analyzer {
    private boolean enablePorterStemming;
    private Set<String> filter;

    /**
     * @param filter               停用词
     * @param enablePorterStemming 是否分析词干（仅限英文）
     */
    public HanLPAnalyzer(Set<String> filter, boolean enablePorterStemming) {
        this.filter = filter;
        this.enablePorterStemming = enablePorterStemming;
    }

    /**
     * @param enablePorterStemming 是否分析词干.进行单复数,时态的转换
     */
    public HanLPAnalyzer(boolean enablePorterStemming) {
        this.enablePorterStemming = enablePorterStemming;
    }

    public HanLPAnalyzer() {
        super();
    }

    /**
     * 重载Analyzer接口，构造分词组件
     */
    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Segment segment = HanLP.newViterbiSegment()
                .enableOffset(true)
                .enablePlaceRecognize(true)
                .enableOrganizationRecognize(true)
                .enableCustomDictionary(true)
                .enablePartOfSpeechTagging(true)
                .enableNumberQuantifierRecognize(true);

        Tokenizer tokenizer = new HanLPTokenizer(segment, filter, enablePorterStemming);
        return new TokenStreamComponents(tokenizer);
    }
}