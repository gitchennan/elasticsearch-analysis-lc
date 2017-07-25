package lc.lucene.analyzer;

import com.hankcs.hanlp.api.HanLP;
import com.hankcs.hanlp.seg.Segment;
import lc.lucene.tokenizer.HanLPTokenizer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;

import java.util.Set;

/**
 * NLP分析器
 *
 * @author Kenn
 */
public class HanLPNLPAnalyzer extends Analyzer {

    private boolean enablePorterStemming;
    private Set<String> filter;

    /**
     * @param filter               停用词
     * @param enablePorterStemming 是否分析词干（仅限英文）
     */
    public HanLPNLPAnalyzer(Set<String> filter, boolean enablePorterStemming) {
        this.filter = filter;
        this.enablePorterStemming = enablePorterStemming;
    }

    /**
     * @param enablePorterStemming 是否分析词干.进行单复数,时态的转换
     */
    public HanLPNLPAnalyzer(boolean enablePorterStemming) {
        this.enablePorterStemming = enablePorterStemming;
    }

    public HanLPNLPAnalyzer() {
        super();
    }

    /**
     * 重载Analyzer接口，构造分词组件
     */
    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Segment segment = HanLP.newViterbiSegment()
                .enableOffset(true)
                .enableAllNamedEntityRecognize(true)
                .enablePartOfSpeechTagging(true)
                .enableCustomDictionary(true)
                .enableNumberQuantifierRecognize(true);

        Tokenizer tokenizer = new HanLPTokenizer(segment, filter, enablePorterStemming);
        return new TokenStreamComponents(tokenizer);
    }

}
