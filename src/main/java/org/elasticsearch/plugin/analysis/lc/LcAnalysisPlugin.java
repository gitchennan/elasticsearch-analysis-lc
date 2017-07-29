package org.elasticsearch.plugin.analysis.lc;

import com.google.common.collect.Maps;
import lc.lucene.service.CustomDictionaryReloadService;
import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.util.concurrent.EsExecutors;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.index.analysis.*;
import org.elasticsearch.indices.analysis.AnalysisModule;
import org.elasticsearch.plugins.ActionPlugin;
import org.elasticsearch.plugins.AnalysisPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.rest.RestHandler;
import org.elasticsearch.script.ScriptService;
import org.elasticsearch.search.SearchRequestParsers;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.watcher.ResourceWatcherService;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class LcAnalysisPlugin extends Plugin implements AnalysisPlugin, ActionPlugin {

    private final Settings settings;

    private CustomDictionaryReloadService customDictionaryReloadService;

    public LcAnalysisPlugin(Settings settings) {
        this.settings = settings;
    }

    @Override
    public Collection<Object> createComponents(Client client, ClusterService clusterService, ThreadPool threadPool,
                                               ResourceWatcherService resourceWatcherService, ScriptService scriptService,
                                               SearchRequestParsers searchRequestParsers, NamedXContentRegistry xContentRegistry) {
        ThreadFactory threadFactory = EsExecutors.daemonThreadFactory(settings, "[lc_custom_dict_refresh]");
        ScheduledExecutorService lcCustomDictionaryRefresher = Executors.newSingleThreadScheduledExecutor(threadFactory);

        customDictionaryReloadService = new CustomDictionaryReloadService(client, lcCustomDictionaryRefresher);
        clusterService.addLocalNodeMasterListener(customDictionaryReloadService.buildLocalNodeMasterListener());

        return Collections.emptyList();
    }

    @Override
    public void close() throws IOException {
        ThreadPool.terminate(customDictionaryReloadService.getLcCustomDictionaryRefresher(), 0, TimeUnit.SECONDS);
    }

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
    public List<ActionHandler<? extends ActionRequest, ? extends ActionResponse>> getActions() {
        return Collections.singletonList(
                new ActionHandler<LcDictReloadRequest, LcDictReloadResponse>(
                        LcDictReloadAction.INSTANCE, LcTransportDictReloadAction.class));
    }

    @Override
    public List<Class<? extends RestHandler>> getRestHandlers() {
        return Collections.singletonList(LcRestAction.class);
    }
}
