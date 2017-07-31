package org.elasticsearch.plugin.analysis.lc;

import com.google.common.collect.Maps;
import lc.lucene.service.CustomDictionaryReloadService;
import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.common.component.LifecycleComponent;
import org.elasticsearch.index.analysis.*;
import org.elasticsearch.indices.analysis.AnalysisModule;
import org.elasticsearch.plugins.ActionPlugin;
import org.elasticsearch.plugins.AnalysisPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.rest.RestHandler;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class LcAnalysisPlugin extends Plugin implements AnalysisPlugin, ActionPlugin {

    public static final String PLUGIN_NAME = "analysis-lc";

    @Override
    public Map<String, AnalysisModule.AnalysisProvider<TokenizerFactory>> getTokenizers() {
        Map<String, AnalysisModule.AnalysisProvider<TokenizerFactory>> extra = Maps.newHashMap();

        extra.put("lc", LcTokenizerFactory::getLcTokenizerFactory);

        return extra;
    }

    @Override
    public Map<String, AnalysisModule.AnalysisProvider<AnalyzerProvider<? extends Analyzer>>> getAnalyzers() {
        Map<String, AnalysisModule.AnalysisProvider<AnalyzerProvider<? extends Analyzer>>> extra = Maps.newHashMap();

        extra.put("lc_index", LcAnalyzerProvider::getLcIndexAnalyzerProvider);
        extra.put("lc_search", LcAnalyzerProvider::getLcSearchAnalyzerProvider);

        return extra;
    }

    @Override
    public Map<String, AnalysisModule.AnalysisProvider<TokenFilterFactory>> getTokenFilters() {
        Map<String, AnalysisModule.AnalysisProvider<TokenFilterFactory>> extra = Maps.newHashMap();

        extra.put("lc_stopword", LcTokenFilterFactory::getLcStopWordTokenFilterFactory);
        extra.put("lc_synonym", LcTokenFilterFactory::getLcSynonymTokenFilterFactory);
        extra.put("lc_useless", LcTokenFilterFactory::getLcUselessCharFilterFactory);
        extra.put("lc_whitespace", LcTokenFilterFactory::getLcWhitespaceFilterFactory);

        return extra;
    }

    @Override
    public Collection<Class<? extends LifecycleComponent>> getGuiceServiceClasses() {
        return Collections.singletonList(CustomDictionaryReloadService.class);
    }

    @Override
    public List<ActionHandler<? extends ActionRequest, ? extends ActionResponse>> getActions() {
        return Collections.singletonList(new ActionHandler<>(LcDictReloadAction.INSTANCE, LcTransportDictReloadAction.class));
    }

    @Override
    public List<Class<? extends RestHandler>> getRestHandlers() {
        return Collections.singletonList(LcRestAction.class);
    }
}
