package org.elasticsearch.index.analysis;

import lc.lucene.filter.StopWordTokenFilter;
import lc.lucene.filter.SynonymTokenFilter;
import lc.lucene.filter.UselessCharFilter;
import lc.lucene.filter.WhitespaceTokenFilter;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;

public class LcTokenFilterFactory extends AbstractTokenFilterFactory {

    private LcTokenFilterType filterType;

    public LcTokenFilterFactory(IndexSettings indexSettings, String name, Settings settings, LcTokenFilterType filterType) {
        super(indexSettings, name, settings);
        this.filterType = filterType;
    }

    public static LcTokenFilterFactory getLcStopWordTokenFilterFactory(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        return new LcTokenFilterFactory(indexSettings, name, settings, LcTokenFilterType.LC_STOP_WORD);
    }

    public static LcTokenFilterFactory getLcSynonymTokenFilterFactory(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        return new LcTokenFilterFactory(indexSettings, name, settings, LcTokenFilterType.LC_SYNONYM);
    }

    public static LcTokenFilterFactory getLcUselessCharFilterFactory(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        return new LcTokenFilterFactory(indexSettings, name, settings, LcTokenFilterType.LC_USELESS_CHAR);
    }

    public static LcTokenFilterFactory getLcWhitespaceFilterFactory(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        return new LcTokenFilterFactory(indexSettings, name, settings, LcTokenFilterType.LC_WHITESPACE);
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        TokenFilter tokenFilter = null;
        switch (filterType) {
            case LC_STOP_WORD:
                tokenFilter = new StopWordTokenFilter(tokenStream);
                break;
            case LC_SYNONYM:
                tokenFilter = new SynonymTokenFilter(tokenStream);
                break;
            case LC_USELESS_CHAR:
                tokenFilter = new UselessCharFilter(tokenStream);
                break;
            case LC_WHITESPACE:
                tokenFilter = new WhitespaceTokenFilter(tokenStream);
                break;
        }
        return tokenFilter;
    }


}
