//package lc.lucene.filter;
//
//import org.apache.lucene.analysis.TokenFilter;
//import org.apache.lucene.analysis.TokenStream;
//import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
//
//import java.io.IOException;
//
//public class WhitespaceTokenFilter extends TokenFilter {
//
//    private CharTermAttribute termAtt = (CharTermAttribute) this.addAttribute(CharTermAttribute.class);
//
//    public WhitespaceTokenFilter(TokenStream input) {
//        super(input);
//    }
//
//    @Override
//    public boolean incrementToken() throws IOException {
//        while (this.input.incrementToken()) {
//            char[] text = this.termAtt.buffer();
//            int length = this.termAtt.length();
//
//            if (text == null || text.length == 0 || String.valueOf(text, 0, length).trim().length() == 0) {
//                continue;
//            }
//
//            this.termAtt.setEmpty();
//            this.termAtt.append(String.valueOf(text, 0, length));
//            return true;
//        }
//        return false;
//    }
//}
