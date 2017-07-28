package lc.lucene.tokenizer;

import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import java.io.BufferedReader;
import java.io.IOException;

public class LcTokenizer extends Tokenizer {
    // 当前词
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    // 偏移量
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
    // 距离
    private final PositionIncrementAttribute positionAttr = addAttribute(PositionIncrementAttribute.class);
    // 词性
    private TypeAttribute typeAtt = addAttribute(TypeAttribute.class);

    private SegmentWrapper segment;

    public LcTokenizer(Segment segment) {
        super();
        this.segment = new SegmentWrapper(new BufferedReader(input), segment);
    }

    @Override
    final public boolean incrementToken() throws IOException {
        clearAttributes();

        int position = 1;
        Term term = segment.next();

        if (term != null) {
            positionAttr.setPositionIncrement(position);
            termAtt.setEmpty().append(term.word);
            offsetAtt.setOffset(correctOffset(term.offset), correctOffset(term.offset + term.word.length()));
            typeAtt.setType(term.nature == null ? "null" : term.nature.toString());
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        segment.reset(new BufferedReader(this.input));
    }

    @Override
    public void end() throws IOException {
        super.end();
    }
}