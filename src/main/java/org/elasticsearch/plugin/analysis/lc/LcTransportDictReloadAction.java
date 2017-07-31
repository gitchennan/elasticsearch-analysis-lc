package org.elasticsearch.plugin.analysis.lc;


import com.hankcs.hanlp.log.HanLpLogger;
import lc.lucene.service.CustomDictionaryReloadService;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.HandledTransportAction;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.tasks.Task;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

public class LcTransportDictReloadAction extends HandledTransportAction<LcDictReloadRequest, LcDictReloadResponse> {

    private CustomDictionaryReloadService reloadService;

    @Inject
    public LcTransportDictReloadAction(Settings settings, ThreadPool threadPool, ActionFilters actionFilters, CustomDictionaryReloadService reloadService,
                                       IndexNameExpressionResolver indexNameExpressionResolver, Client client, TransportService transportService) {
        super(settings, LcDictReloadAction.NAME, threadPool, transportService, actionFilters, indexNameExpressionResolver, LcDictReloadRequest::new);
        this.reloadService = reloadService;
    }

    @Override
    protected void doExecute(Task task, LcDictReloadRequest request, ActionListener<LcDictReloadResponse> listener) {
        try {
            String reloadResultMsg = reloadService.doPrivilegedReloadCustomDictionary();
            LcDictReloadResponse dictReloadResponse = new LcDictReloadResponse(RestStatus.OK, reloadResultMsg + ", task_id: " + task.getId());
            listener.onResponse(dictReloadResponse);

            HanLpLogger.info(this, dictReloadResponse.toString());
        }
        catch (Exception ex) {
            listener.onFailure(ex);
        }
    }

    @Override
    protected void doExecute(LcDictReloadRequest request, ActionListener<LcDictReloadResponse> listener) {
        throw new UnsupportedOperationException("task required");
    }
}
